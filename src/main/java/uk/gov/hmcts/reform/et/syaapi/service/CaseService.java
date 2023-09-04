package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
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
import uk.gov.hmcts.reform.et.syaapi.helper.JurisdictionCodesMapper;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocumentAcasResponse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfService;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfServiceException;
import uk.gov.hmcts.reform.et.syaapi.service.utils.GenericServiceUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.ecm.common.model.helper.TribunalOffice.getCaseTypeId;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ACAS_VISIBLE_DOCS;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.CLAIMANT_CORRESPONDENCE_DOCUMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.DEFAULT_TRIBUNAL_OFFICE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET1_ATTACHMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET1_ONLINE_SUBMISSION;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.JURISDICTION_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UNASSIGNED_OFFICE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.YES;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.INITIATE_CASE_DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.SUBMIT_CASE_DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_CASE_SUBMITTED;

/**
 * Provides read and write access to cases stored by ET.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public class CaseService {

    public static final String DOCUMENT_COLLECTION = "documentCollection";
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi ccdApiClient;
    private final IdamClient idamClient;
    private final PostcodeToOfficeService postcodeToOfficeService;
    private final AcasService acasService;
    private final CaseDocumentService caseDocumentService;
    private final NotificationService notificationService;
    private final PdfService pdfService;
    private final JurisdictionCodesMapper jurisdictionCodesMapper;
    private final CaseOfficeService caseOfficeService;
    private static final String ALL_CASES_QUERY = "{\"size\":10000,\"query\":{\"match_all\": {}}}";

    @Value("${caseWorkerUserName}")
    private transient String caseWorkerUserName;
    @Value("${caseWorkerPassword}")
    private transient String caseWorkerPassword;

    /**
     * Given a case id in the case request, this will retrieve the correct {@link CaseDetails}.
     *
     * @param caseId id of the case
     * @return the associated {@link CaseDetails} for the ID provided
     */
    @Retryable({FeignException.class, RuntimeException.class})
    public CaseDetails getUserCase(String authorization, String caseId) {
        return ccdApiClient.getCase(authorization, authTokenGenerator.generate(), caseId);
    }

    /**
     * Given a user derived from the authorisation token in the request,
     * this will get all cases {@link CaseDetails} for that user.
     *
     * @param authorization is used to get the {@link UserInfo} for the request
     * @return the associated {@link CaseDetails} for the ID provided
     */
    @Retryable({FeignException.class, RuntimeException.class})
    public List<CaseDetails> getAllUserCases(String authorization) {
        // Elasticsearch
        List<CaseDetails> scotlandCases = Optional.ofNullable(ccdApiClient.searchCases(
            authorization,
            authTokenGenerator.generate(),
            SCOTLAND_CASE_TYPE,
            ALL_CASES_QUERY).getCases()).orElse(Collections.emptyList());

        // Elasticsearch
        List<CaseDetails> englandCases = Optional.ofNullable(ccdApiClient.searchCases(
            authorization,
            authTokenGenerator.generate(),
            ENGLAND_CASE_TYPE,
            ALL_CASES_QUERY).getCases()).orElse(Collections.emptyList());

        return Stream.of(scotlandCases, englandCases).flatMap(Collection::stream).toList();
    }

    /**
     * Given a caseID, this will retrieve the correct {@link CaseDetails}.
     *
     * @param authorization is used to find the {@link UserInfo} for request
     * @param caseRequest   case data for request
     * @return the associated {@link CaseDetails} if the case is created
     */
    @Retryable({FeignException.class, RuntimeException.class})
    public CaseDetails createCase(String authorization,
                                  CaseRequest caseRequest) {
        String s2sToken = authTokenGenerator.generate();
        String userId = idamClient.getUserInfo(authorization).getUid();
        String eventTypeName = INITIATE_CASE_DRAFT.name();
        String caseType = getCaseType(caseRequest);
        Et1CaseData data = new EmployeeObjectMapper().getEmploymentCaseData(caseRequest.getCaseData());
        StartEventResponse ccdCase = ccdApiClient.startForCitizen(
            authorization,
            s2sToken,
            userId,
            JURISDICTION_ID,
            caseType,
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
            caseType,
            true,
            caseDataContent
        );
    }

    private String getCaseType(CaseRequest caseRequest) {
        try {
            return getCaseTypeId(
                postcodeToOfficeService.getTribunalOfficeFromPostcode(caseRequest.getPostCode())
                    .orElse(DEFAULT_TRIBUNAL_OFFICE).getOfficeName());
        } catch (InvalidPostcodeException e) {
            log.info("Failed to find tribunal office : {} ", e.getMessage());
            return getCaseTypeId(DEFAULT_TRIBUNAL_OFFICE.getOfficeName());
        }
    }

    /**
     * Will accept a {@link CaseRequest} trigger an event to update a give case in ET.
     *
     * @param authorization jwt of the user
     * @param caseRequest   case to be updated
     * @return the newly updated case wrapped in a {@link CaseDetails} object.
     */
    public CaseDetails updateCase(String authorization,
                                  CaseRequest caseRequest) {
        return triggerEvent(authorization, caseRequest.getCaseId(), CaseEvent.UPDATE_CASE_DRAFT,
                            caseRequest.getCaseTypeId(), caseRequest.getCaseData()
        );
    }

    /**
     * Given Case Request, triggers submit case events for the case. Before submitting case events
     * sets managing office (tribunal office), created PDF file for the case and saves PDF file.
     *
     * @param authorization is used to seek the {UserInfo} for request
     * @param caseRequest   is used to provide the caseId, caseTypeId and {@link CaseData} in JSON Format
     * @return the associated {@link CaseData} if the case is submitted
     */
    public CaseDetails submitCase(String authorization, CaseRequest caseRequest)
        throws PdfServiceException {
        // Assigning local office to case data
        CaseData caseData = caseOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(caseRequest);
        // Getting user info from IDAM
        UserInfo userInfo = idamClient.getUserInfo(authorization);
        // Submitting the case to CCD, receiving caseDetails and setting ethosCaseReference,
        // receiptDate, feeGroupReference with the received details.
        CaseDetails caseDetails = triggerEventForSubmitCase(authorization, caseRequest);
        setCaseDataWithSubmittedCaseDetails(caseDetails, caseData);
        // Create case pdf file(s). If the user selected language is Welsh, we also create Welsh pdf file
        // and add it to our pdf files list
        List<PdfDecodedMultipartFile> casePdfFiles =
            pdfService.convertCaseDataToPdfDecodedMultipartFile(caseData, userInfo);
        // Submit e-mail to the user with attached ET1 pdf file according to selected contact language
        // (Welsh or English)
        notificationService.sendSubmitCaseConfirmationEmail(caseRequest, caseData, userInfo, casePdfFiles);
        // Creating acas certificates for each respondent
        List<PdfDecodedMultipartFile> acasCertificates =
            pdfService.convertAcasCertificatesToPdfDecodedMultipartFiles(
                caseData, acasService.getAcasCertificatesByCaseData(caseData));
        // Uploading all documents to document store
        List<DocumentTypeItem> documentList = uploadAllDocuments(authorization, caseRequest, caseData, casePdfFiles,
                                                                 acasCertificates);
        // Setting caliamantPCqId and documentCollection to case details
        caseDetails.getData().put("ClaimantPcqId", caseData.getClaimantPcqId());
        caseDetails.getData().put(DOCUMENT_COLLECTION, documentList);
        // For determining the case is submitted via ET1
        caseDetails.getData().put(ET1_ONLINE_SUBMISSION, YES);

        // Updating case with ClaimantPcqId and Document collection
        triggerEvent(authorization, caseRequest.getCaseId(), UPDATE_CASE_SUBMITTED, caseDetails.getCaseTypeId(),
                     caseDetails.getData()
        );

        return caseDetails;
    }

    private List<DocumentTypeItem> uploadAllDocuments(String authorization,
                                                      CaseRequest caseRequest,
                                                      CaseData caseData,
                                                      List<PdfDecodedMultipartFile> casePdfFiles,
                                                      List<PdfDecodedMultipartFile> acasCertificates) {
        List<DocumentTypeItem> documentList = new ArrayList<>();
        try {
            documentList.addAll(caseDocumentService
                                    .uploadAllDocuments(authorization, caseRequest.getCaseTypeId(),
                                                        casePdfFiles, acasCertificates));

            if (!ObjectUtils.isEmpty(caseData.getClaimantRequests())
                && !ObjectUtils.isEmpty(caseData.getClaimantRequests().getClaimDescriptionDocument())) {
                documentList.add(caseDocumentService.createDocumentTypeItem(
                    ET1_ATTACHMENT,
                    caseData.getClaimantRequests().getClaimDescriptionDocument()
                ));
            }
        } catch (CaseDocumentException cde) {
            // Send upload error alert email to shared inbox
            notificationService.sendDocUploadErrorEmail(caseRequest, casePdfFiles, acasCertificates,
                                                        caseData.getClaimantRequests().getClaimDescriptionDocument());
            GenericServiceUtil.logException("Case Documents Upload error - Failed to complete case documents upload",
                                            caseData.getEthosCaseReference(), cde.getMessage(),
                                            this.getClass().getName(), "submitCase");
        }
        return documentList;
    }

    private static void setCaseDataWithSubmittedCaseDetails(CaseDetails caseDetails, CaseData caseData) {
        caseData.setEthosCaseReference(caseDetails.getData().get("ethosCaseReference") == null ? "" :
                                           caseDetails.getData().get("ethosCaseReference").toString());
        caseData.setReceiptDate(caseDetails.getData().get("receiptDate") == null ? "" :
                                    caseDetails.getData().get("receiptDate").toString());
        caseData.setFeeGroupReference(caseDetails.getData().get("feeGroupReference") == null ? "" :
                                          caseDetails.getData().get("feeGroupReference").toString());
    }

    /**
     * Given a caseId, initialization of trigger event to start and submit update for case.
     *
     * @param authorization is used to seek the {@link UserInfo} for request
     * @param caseId        used to retrieve get case details
     * @param caseType      is used to determine if the case is for ET_EnglandWales or ET_Scotland
     * @param eventName     is used to determine INITIATE_CASE_DRAFT or UPDATE_CASE_DRAFT
     * @param caseData      is used to provide the {@link Et1CaseData} in json format
     * @return the associated {@link CaseData} if the case is updated
     */
    public CaseDetails triggerEvent(String authorization, String caseId, CaseEvent eventName,
                                    String caseType, Map<String, Object> caseData) {
        ObjectMapper objectMapper = new ObjectMapper();
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        StartEventResponse startEventResponse = startUpdate(authorization, caseId, caseType, eventName);
        CaseData caseData1 = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseData);

        if (SUBMIT_CASE_DRAFT == eventName) {
            enrichCaseDataWithJurisdictionCodes(caseData1);
        }

        return submitUpdate(
            authorization,
            caseId,
            caseDetailsConverter.et1ToCaseDataContent(startEventResponse, caseData1),
            caseType
        );
    }

    /**
     * Given a caseId, submit the case into ECM.
     *
     * @param authorization is used to seek the {@link UserInfo} for request
     * @param caseRequest   is used to provide the caseId, caseTypeId and {@link CaseData} in JSON Format
     * @return the associated {@link CaseData} on submission
     */
    public CaseDetails triggerEventForSubmitCase(String authorization, CaseRequest caseRequest) {
        StartEventResponse startEventResponse = startUpdate(authorization, caseRequest.getCaseId(),
                                                            caseRequest.getCaseTypeId(), SUBMIT_CASE_DRAFT
        );
        CaseData caseData1 = EmployeeObjectMapper.mapRequestCaseDataToCaseData(
            startEventResponse.getCaseDetails().getData());
        enrichCaseDataWithJurisdictionCodes(caseData1);
        caseData1.setManagingOffice(caseRequest.getCaseData().get("managingOffice") == null ? UNASSIGNED_OFFICE :
                                        caseRequest.getCaseData().get("managingOffice").toString());
        caseData1.setReceiptDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        caseData1.setFeeGroupReference(caseRequest.getCaseId());
        caseData1.setPositionType("ET1 Online submission");
        ObjectMapper objectMapper = new ObjectMapper();
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        return submitUpdate(
            authorization,
            caseRequest.getCaseId(),
            caseDetailsConverter.caseDataContent(startEventResponse, caseData1),
            caseRequest.getCaseTypeId()
        );
    }

    /**
     * Given a caseId, start update for the case.
     *
     * @param authorization is used to seek the {@link UserInfo} for request
     * @param caseId        used to retrieve get case details
     * @param caseType      is used to determine if the case is for ET_EnglandWales or ET_Scotland
     * @param eventName     is used to determine INITIATE_CASE_DRAFT or UPDATE_CASE_DRAFT
     * @return startEventResponse associated case details updated
     */
    public StartEventResponse startUpdate(String authorization, String caseId,
                                          String caseType, CaseEvent eventName) {
        String s2sToken = authTokenGenerator.generate();
        UserInfo userInfo = idamClient.getUserInfo(authorization);

        return ccdApiClient.startEventForCitizen(
            authorization,
            s2sToken,
            userInfo.getUid(),
            JURISDICTION_ID,
            caseType,
            caseId,
            eventName.name()
        );
    }

    /**
     * Given a caseId, submit update for the case.
     *
     * @param authorization   is used to seek the {@link UserInfo} for request
     * @param caseId          used to retrieve get case details
     * @param caseDataContent provides overall content of the case
     * @param caseType        is used to determine if the case is for ET_EnglandWales or ET_Scotland
     */
    public CaseDetails submitUpdate(String authorization, String caseId,
                                    CaseDataContent caseDataContent, String caseType) {
        UserInfo userInfo = idamClient.getUserInfo(authorization);
        String s2sToken = authTokenGenerator.generate();
        return ccdApiClient.submitEventForCitizen(
            authorization,
            s2sToken,
            userInfo.getUid(),
            JURISDICTION_ID,
            caseType,
            caseId,
            true,
            caseDataContent
        );
    }

    /**
     * Given a datetime, this method will return a list of caseIds which have been modified since the datetime
     * provided.
     *
     * @param authorisation   used for IDAM authentication for the query
     * @param requestDateTime used as the query parameter
     * @return a list of caseIds
     */
    public List<Long> getLastModifiedCasesId(String authorisation, LocalDateTime requestDateTime) {
        BoolQueryBuilder boolQueryBuilder = boolQuery()
            .filter(new RangeQueryBuilder("last_modified").gte(requestDateTime));
        String query = new SearchSourceBuilder()
            .size(MAX_ES_SIZE)
            .query(boolQueryBuilder)
            .toString();
        return searchEnglandScotlandCases(authorisation, query)
            .stream()
            .map(CaseDetails::getId)
            .toList();
    }

    /**
     * Given a caseId, return a list of document IDs which are visible to ACAS.
     *
     * @param caseId 16 digit CCD id
     * @return a MultiValuedMap containing a list of document ids and timestamps
     */
    public MultiValuedMap<String, CaseDocumentAcasResponse> retrieveAcasDocuments(String caseId) {
        BoolQueryBuilder boolQueryBuilder = boolQuery()
            .filter(new TermsQueryBuilder("reference.keyword", caseId));
        String query = new SearchSourceBuilder()
            .size(MAX_ES_SIZE)
            .query(boolQueryBuilder)
            .toString();
        return getDocumentUuids(query);
    }

    private MultiValuedMap<String, CaseDocumentAcasResponse> getDocumentUuids(String query) {
        String authorisation = idamClient.getAccessToken(caseWorkerUserName, caseWorkerPassword);
        List<CaseData> caseDataList = searchAndReturnCaseDataList(authorisation, query);

        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();

        for (CaseData caseData : caseDataList) {
            documentTypeItemList.addAll(caseData.getDocumentCollection().stream()
                                            .filter(d -> ACAS_VISIBLE_DOCS.contains(defaultIfEmpty(
                                                d.getValue().getTypeOfDocument(),
                                                ""
                                            )))
                                            .toList());

            if (caseData.getClaimantRequests() != null
                && caseData.getClaimantRequests().getClaimDescriptionDocument() != null) {
                documentTypeItemList.add(caseDocumentService.createDocumentTypeItem("ET1 Attachment",
                                                    caseData.getClaimantRequests().getClaimDescriptionDocument()
                ));
            }
        }

        MultiValuedMap<String, CaseDocumentAcasResponse> documentIds = new ArrayListValuedHashMap<>();
        Pattern pattern = Pattern.compile(".{36}$");

        for (DocumentTypeItem documentTypeItem : documentTypeItemList) {
            Matcher matcher = pattern.matcher(documentTypeItem.getValue().getUploadedDocument().getDocumentUrl());
            if (matcher.find()) {
                CaseDocument caseDocument = caseDocumentService.getDocumentDetails(
                    authorisation, UUID.fromString(matcher.group())).getBody();
                if (caseDocument != null) {
                    CaseDocumentAcasResponse caseDocumentAcasResponse = CaseDocumentAcasResponse.builder()
                        .documentId(matcher.group())
                        .modifiedOn(caseDocument.getModifiedOn())
                        .build();
                    documentIds.put(documentTypeItem.getValue().getTypeOfDocument(), caseDocumentAcasResponse);
                }
            }
        }
        return documentIds;
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
            .size(MAX_ES_SIZE)
            .query(boolQueryBuilder)
            .toString();

        return searchEnglandScotlandCases(authorisation, query);
    }

    private List<CaseData> searchAndReturnCaseDataList(String authorisation, String query) {
        List<CaseDetails> searchResults = searchEnglandScotlandCases(authorisation, query);
        List<CaseData> caseDataList = new ArrayList<>();
        for (CaseDetails caseDetails : searchResults) {
            caseDataList.add(EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData()));
        }
        return caseDataList;
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
                                                             caseTypeId, query
        );
        if (searchResult != null && !isEmpty(searchResult.getCases())) {
            caseDetailsList.addAll(searchResult.getCases());
        }
        return caseDetailsList;
    }

    private void enrichCaseDataWithJurisdictionCodes(CaseData caseData) {
        List<JurCodesTypeItem> jurCodesTypeItems = jurisdictionCodesMapper.mapToJurCodes(caseData);
        caseData.setJurCodesCollection(jurCodesTypeItems);
    }

    void uploadTseSupportingDocument(CaseDetails caseDetails, UploadedDocumentType contactApplicationFile,
                                     String description) {
        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        List<DocumentTypeItem> docList = caseData.getDocumentCollection();

        if (docList == null) {
            docList = new ArrayList<>();
        }

        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(CLAIMANT_CORRESPONDENCE_DOCUMENT);
        documentType.setUploadedDocument(contactApplicationFile);
        documentType.setShortDescription(description);

        docList.add(DocumentTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(documentType)
                        .build());
        caseDetails.getData().put(DOCUMENT_COLLECTION, docList);
    }

    void uploadTseCyaAsPdf(
        String authorization,
        CaseDetails caseDetails,
        ClaimantTse claimantTse,
        String caseType
    ) throws DocumentGenerationException, CaseDocumentException {

        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        List<DocumentTypeItem> docList = caseData.getDocumentCollection();

        if (docList == null) {
            docList = new ArrayList<>();
        }
        PdfDecodedMultipartFile pdfDecodedMultipartFile = pdfService.convertClaimantTseIntoMultipartFile(claimantTse);
        docList.add(caseDocumentService.createDocumentTypeItem(
            authorization,
            caseType,
            CLAIMANT_CORRESPONDENCE_DOCUMENT,
            pdfDecodedMultipartFile
        ));

        caseDetails.getData().put(DOCUMENT_COLLECTION, docList);
    }

    void createResponsePdf(String authorization,
                           CaseData caseData,
                           RespondToApplicationRequest request,
                           String appType)
        throws DocumentGenerationException, CaseDocumentException {
        String description = "Response to " + appType;
        PdfDecodedMultipartFile multipartResponsePdf =
            pdfService.convertClaimantResponseIntoMultipartFile(request, description);

        var responsePdf = caseDocumentService.createDocumentTypeItem(
            authorization,
            request.getCaseTypeId(),
            CLAIMANT_CORRESPONDENCE_DOCUMENT,
            multipartResponsePdf
        );

        if (isEmpty(caseData.getDocumentCollection())) {
            caseData.setDocumentCollection(new ArrayList<>());
        }
        var docCollection = caseData.getDocumentCollection();
        docCollection.add(responsePdf);
    }
}
