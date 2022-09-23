package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfService;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfServiceException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.CASE_FIELD_MANAGING_OFFICE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.DEFAULT_TRIBUNAL_OFFICE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.JURISDICTION_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.INITIATE_CASE_DRAFT;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.ExcessiveImports"})
public class CaseService {

    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi ccdApiClient;
    private final IdamClient idamClient;
    private final PostcodeToOfficeService postcodeToOfficeService;
    private final AcasService acasService;
    private final CaseDocumentService caseDocumentService;
    private final NotificationService notificationService;
    private final PdfService pdfService;
    private final NotificationsProperties notificationsProperties;

    /**
     * Given a case id in the case request, this will retrieve the correct {@link CaseDetails}.
     *
     * @param caseRequest contains case id get the {@link CaseDetails} for
     * @return the associated {@link CaseDetails} for the ID provided
     */
    @Retryable({FeignException.class, RuntimeException.class})
    public CaseDetails getUserCase(String authorization, CaseRequest caseRequest) {
        return ccdApiClient.getCase(authorization, authTokenGenerator.generate(), caseRequest.getCaseId());
    }

    /**
     * Given a user derived from the authorisation token in the request,
     * this will get all cases {@link CaseDetails} for that user.
     *
     * @param authorization is used to get the {@link UserDetails} for the request
     * @return the associated {@link CaseDetails} for the ID provided
     */
    @Retryable({FeignException.class, RuntimeException.class})
    public List<CaseDetails> getAllUserCases(String authorization) {
        UserDetails userDetails = idamClient.getUserDetails(authorization);

        List<CaseDetails> scotlandCases = ccdApiClient.searchForCitizen(
            authorization, authTokenGenerator.generate(),
            userDetails.getId(), JURISDICTION_ID, SCOTLAND_CASE_TYPE, Collections.emptyMap());

        List<CaseDetails> englandCases = ccdApiClient.searchForCitizen(
            authorization, authTokenGenerator.generate(),
            userDetails.getId(), JURISDICTION_ID, ENGLAND_CASE_TYPE, Collections.emptyMap());

        return Stream.of(scotlandCases, englandCases).flatMap(Collection::stream).collect(toList());
    }

    /**
     * Given a caseID, this will retrieve the correct {@link CaseDetails}.
     *
     * @param authorization is used to find the {@link UserDetails} for request
     * @param caseRequest  case data for request
     * @return the associated {@link CaseDetails} if the case is created
     */
    @Retryable({FeignException.class, RuntimeException.class})
    public CaseDetails createCase(String authorization,
                                  CaseRequest caseRequest) {
        String s2sToken = authTokenGenerator.generate();
        String userId = idamClient.getUserDetails(authorization).getId();
        String eventTypeName = INITIATE_CASE_DRAFT.name();
        Et1CaseData data = new EmployeeObjectMapper().getEmploymentCaseData(caseRequest.getCaseData());
        StartEventResponse ccdCase = ccdApiClient.startForCitizen(
            authorization,
            s2sToken,
            userId,
            JURISDICTION_ID,
            getCaseTypeByCaseTypeId(caseRequest.getCaseTypeId()),
            eventTypeName
        );


        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(eventTypeName).build())
            .eventToken(ccdCase.getToken())
            .data(data)
            .build();

        return ccdApiClient.submitForCitizen(
            authorization,
            s2sToken,
            userId,
            JURISDICTION_ID,
            getCaseTypeByCaseTypeId(caseRequest.getCaseTypeId()),
            true,
            caseDataContent
        );
    }

    private String getCaseTypeByCaseTypeId(String caseTypeId) {
        if (StringUtils.hasText(caseTypeId)) {
            return caseTypeId;
        } else {
            return ENGLAND_CASE_TYPE;
        }
    }

    private TribunalOffice getTribunalOfficeByCaseTypeId(String caseTypeId) {
        return postcodeToOfficeService.getTribunalOfficeByCaseTypeId(caseTypeId).orElse(DEFAULT_TRIBUNAL_OFFICE);
    }

    public CaseDetails updateCase(String authorization,
                                  CaseRequest caseRequest) {
        return triggerEvent(authorization, caseRequest.getCaseId(), CaseEvent.UPDATE_CASE_DRAFT,
                            getCaseTypeByCaseTypeId(caseRequest.getCaseTypeId()), caseRequest.getCaseData());
    }

    private CaseData convertCaseRequestToCaseDataWithTribunalOffice(CaseRequest caseRequest) {
        caseRequest.getCaseData().put(CASE_FIELD_MANAGING_OFFICE,
                                      getTribunalOfficeByCaseTypeId(
                                          getCaseTypeByCaseTypeId(caseRequest.getCaseTypeId())).getOfficeName());
        return EmployeeObjectMapper.mapCaseRequestToCaseData(caseRequest.getCaseData());
    }

    /**
     * Given Case Request, triggers submit case events for the case. Before submitting case events
     * sets managing office (tribunal office), created PDF file for the case and saves PDF file.
     *
     * @param authorization is used to seek the {UserDetails} for request
     * @param caseRequest is used to provide the caseId, caseTypeId and {@link CaseData} in JSON Format
     * @return the associated {@link CaseData} if the case is submitted
     */
    public CaseDetails submitCase(String authorization,
                                  CaseRequest caseRequest)
        throws PdfServiceException, CaseDocumentException, AcasException, InvalidAcasNumbersException {

        caseRequest.getCaseData().put("receiptDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        CaseData caseData = convertCaseRequestToCaseDataWithTribunalOffice(caseRequest);
        caseData.setReceiptDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        List<PdfDecodedMultipartFile> acasCertificates = pdfService.convertAcasCertificatesToPdfDecodedMultipartFiles(
            caseData, acasService.getAcasCertificatesByCaseData(caseData));

        CaseDetails caseDetails = triggerEvent(authorization, caseRequest.getCaseId(), CaseEvent.SUBMIT_CASE_DRAFT,
                                               getCaseTypeByCaseTypeId(
                                                   caseRequest.getCaseTypeId()), caseRequest.getCaseData());
        caseData.setEthosCaseReference(caseDetails.getData().get("ethosCaseReference") == null ? "" :
            caseDetails.getData().get("ethosCaseReference").toString());
        caseData.setReceiptDate(caseDetails.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        PdfDecodedMultipartFile casePdfFile =
            pdfService.convertCaseDataToPdfDecodedMultipartFile(caseData);
        caseDetails.getData().put("documentCollection",
                                  caseDocumentService
                                      .uploadAllDocuments(authorization,
                                                          getCaseTypeByCaseTypeId(caseRequest.getCaseTypeId()),
                                                          casePdfFile,
                                                          acasCertificates));
        notificationService
            .sendSubmitCaseConfirmationEmail(
                notificationsProperties.getSubmitCaseEmailTemplateId(),
                caseData.getClaimantType().getClaimantEmailAddress(),
                caseRequest.getCaseId(),
                caseData.getClaimantIndType().getClaimantTitle() == null ? "" :
                    caseData.getClaimantIndType().getClaimantTitle(),
                caseData.getClaimantIndType().getClaimantLastName(),
                caseDetails.getId() == null ? "case id not found" : caseDetails.getId().toString(),
                notificationsProperties.getCitizenPortalLink());
        return caseDetails;
    }

    /**
     * Given a caseId, initialization of trigger event to start and submit update for case.
     *
     * @param authorization is used to seek the {@link UserDetails} for request
     * @param caseId used to retrieve get case details
     * @param caseType is used to determine if the case is for ET_EnglandWales or ET_Scotland
     * @param eventName is used to determine INITIATE_CASE_DRAFT or UPDATE_CASE_DRAFT
     * @param caseData is used to provide the {@link Et1CaseData} in json format
     * @return the associated {@link CaseData} if the case is updated
     */
    public CaseDetails triggerEvent(String authorization, String caseId, CaseEvent eventName,
                                    String caseType, Map<String, Object> caseData) {
        ObjectMapper objectMapper = new ObjectMapper();
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        EmployeeObjectMapper employeeObjectMapper = new EmployeeObjectMapper();
        StartEventResponse startEventResponse = startUpdate(authorization, caseId, caseType, eventName);
        return submitUpdate(authorization, caseId,
                            caseDetailsConverter.caseDataContent(startEventResponse,
                                                                 employeeObjectMapper.getEmploymentCaseData(caseData)),
                            caseType);
    }

    /**
     * Given a caseId, start update for the case.
     *
     * @param authorization is used to seek the {@link UserDetails} for request
     * @param caseId used to retrieve get case details
     * @param caseType is used to determine if the case is for ET_EnglandWales or ET_Scotland
     * @param eventName is used to determine INITIATE_CASE_DRAFT or UPDATE_CASE_DRAFT
     * @return startEventResponse associated case details updated
     */
    public StartEventResponse startUpdate(String authorization, String caseId,
                                          String caseType, CaseEvent eventName) {
        String s2sToken = authTokenGenerator.generate();
        UserDetails userDetails = idamClient.getUserDetails(authorization);

        return ccdApiClient.startEventForCitizen(
            authorization,
            s2sToken,
            userDetails.getId(),
            JURISDICTION_ID,
            caseType,
            caseId,
            eventName.name()
        );
    }

    /**
     * Given a caseId, submit update for the case.
     *
     * @param authorization is used to seek the {@link UserDetails} for request
     * @param caseId used to retrieve get case details
     * @param caseDataContent provides overall content of the case
     * @param caseType is used to determine if the case is for ET_EnglandWales or ET_Scotland
     */
    public CaseDetails submitUpdate(String authorization, String caseId,
                                    CaseDataContent caseDataContent, String caseType) {
        UserDetails userDetails = idamClient.getUserDetails(authorization);
        String s2sToken = authTokenGenerator.generate();
        return ccdApiClient.submitEventForCitizen(
            authorization,
            s2sToken,
            userDetails.getId(),
            JURISDICTION_ID,
            caseType,
            caseId,
            true,
            caseDataContent
        );
    }

    /** Given a datetime, this method will return a list of caseIds which have been modified since the datetime
     * provided.
     * @param authorisation used for IDAM authentication for the query
     * @param requestDateTime used as the query parameter
     * @return a list of caseIds
     */
    public List<Long> getLastModifiedCasesId(String authorisation, LocalDateTime requestDateTime) {
        BoolQueryBuilder boolQueryBuilder = boolQuery()
            .filter(new RangeQueryBuilder("last_modified").gte(requestDateTime));
        String query = new SearchSourceBuilder()
            .query(boolQueryBuilder)
            .toString();
        return searchEnglandScotlandCases(authorisation, query)
            .stream()
            .map(CaseDetails::getId)
            .collect(toList());
    }

    /**
     * Given a list of caseIds, this method will return a list of case details.
     *
     * @param authorisation used for IDAM authentication for the query
     * @param caseIds       used as the query parameter
     * @return a list of case details
     */
    public List<CaseDetails> getCaseData(String authorisation, List<String> caseIds) {
        BoolQueryBuilder boolQueryBuilder = boolQuery()
            .filter(new TermsQueryBuilder("reference.keyword", caseIds));
        String query = new SearchSourceBuilder()
            .query(boolQueryBuilder)
            .toString();

        return searchEnglandScotlandCases(authorisation, query);
    }

    private List<CaseDetails> searchEnglandScotlandCases(String authorisation, String query) {
        List<CaseDetails> caseDetailsList = new ArrayList<>();
        caseDetailsList.addAll(searchCaseType(authorisation, ENGLAND_CASE_TYPE, query));
        caseDetailsList.addAll(searchCaseType(authorisation, SCOTLAND_CASE_TYPE, query));
        return caseDetailsList;
    }

    private List<CaseDetails> searchCaseType(String authorisation, String caseTypeId, String query) {
        List<CaseDetails> caseDetailsList = new ArrayList<>();
        SearchResult searchResult = ccdApiClient.searchCases(authorisation, authTokenGenerator.generate(),
                                                             caseTypeId, query);
        if (searchResult != null && !CollectionUtils.isEmpty(searchResult.getCases())) {
            caseDetailsList.addAll(searchResult.getCases());
        }
        return caseDetailsList;
    }
}

