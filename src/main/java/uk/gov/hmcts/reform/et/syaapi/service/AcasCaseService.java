package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocumentAcasResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ACAS_HIDDEN_DOCS;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET1_ATTACHMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET3;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET3_ATTACHMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.NO;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.YES;
import static uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper.convertCaseDataMapToCaseDataObject;

/**
 * Provides read access to ACAS when calling certain APIs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"PMD.TooManyMethods"})
public class AcasCaseService {

    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi ccdApiClient;
    private final IdamClient idamClient;
    private final CaseDocumentService caseDocumentService;
    private final TaskExecutor taskExecutor;

    @Value("${caseWorkerUserName}")
    private String caseWorkerUserName;
    @Value("${caseWorkerPassword}")
    private String caseWorkerPassword;

    /**
     * Given a datetime, this method will return a list of caseIds which have been modified since the datetime
     * provided.
     *
     * @param authorisation   used for IDAM authentication for the query
     * @param requestDateTime used as the query parameter
     * @return a list of caseIds
     */
    public List<Long> getLastModifiedCasesId(String authorisation, LocalDateTime requestDateTime) {
        String query = """
            {
              "size": %d,
              "query": {
                "bool": {
                  "filter": [
                    {
                      "range": {
                        "last_modified": {
                          "gte": "%s"
                        }
                      }
                    }
                  ],
                  "must_not": [
                    {
                      "term": {
                        "data.migratedFromEcm": "Yes"
                      }
                    }
                  ]
                }
              },
              "_source": [
                "reference"
              ]
            }
            """.formatted(MAX_ES_SIZE, requestDateTime.toString());
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
    public List<CaseDocumentAcasResponse> retrieveAcasDocuments(String caseId) {
        String query = """
            {
              "size": %d,
              "query": {
                "bool": {
                  "filter": [
                    {
                      "terms": {
                        "reference.keyword": [%s],
                        "boost": 1.0
                      }
                    }
                  ],
                  "boost": 1.0
                }
              }
            }
            """.formatted(MAX_ES_SIZE, caseId);

        return getDocumentUuids(query);
    }

    private List<CaseDocumentAcasResponse> getDocumentUuids(String query) {
        String authorisation = idamClient.getAccessToken(caseWorkerUserName, caseWorkerPassword);
        List<CaseData> caseDataList = searchAndReturnCaseDataList(authorisation, query);

        List<CaseDocumentAcasResponse> documents = new ArrayList<>();

        caseDataList.stream()
            .map(caseData -> getAllCaseDocuments(authorisation, documents, caseData))
            .forEach(documentTypeItemList -> addDocumentsToMap(authorisation, documents, documentTypeItemList));

        return documents;
    }

    private void addDocumentsToMap(String authorisation, List<CaseDocumentAcasResponse> documents,
                                   List<DocumentTypeItem> documentTypeItemList) {
        documentTypeItemList.stream()
            .filter(documentTypeItem -> !isDuplicatedDoc(documents, documentTypeItem))
            .forEach(documentTypeItem -> documents.add(caseDocumentAcasResponseBuilder(
                documentTypeItem,
                authorisation,
                null
            )));
    }

    private boolean isDuplicatedDoc(List<CaseDocumentAcasResponse> documents, DocumentTypeItem documentTypeItem) {
        String documentUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentUrl();
        UUID uuid = caseDocumentService.getDocumentUuid(documentUrl);
        if (uuid == null) {
            return false;
        }
        return documents.stream()
            .anyMatch(caseDocumentAcasResponse -> uuid.toString()
            .equals(defaultIfEmpty(caseDocumentAcasResponse.getDocumentId(), "")));
    }

    private List<DocumentTypeItem> getAllCaseDocuments(String authorisation, List<CaseDocumentAcasResponse> documents,
                                                       CaseData caseData) {
        retrieveRespondentDocuments(authorisation, documents, caseData);

        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>(getDocumentCollectionDocs(caseData));

        if (caseData.getClaimantRequests() != null
            && caseData.getClaimantRequests().getClaimDescriptionDocument() != null) {
            documentTypeItemList.add(caseDocumentService.createDocumentTypeItem(
                ET1_ATTACHMENT, caseData.getClaimantRequests().getClaimDescriptionDocument()
            ));
        }
        return documentTypeItemList;
    }

    private static List<DocumentTypeItem> getDocumentCollectionDocs(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
            return new ArrayList<>();
        }

        return caseData.getDocumentCollection().stream()
            .filter(d -> !isNullOrEmpty(getDocumentType(d)) && !ACAS_HIDDEN_DOCS.contains(getDocumentType(d)))
            .toList();
    }

    private void retrieveRespondentDocuments(String authorisation, List<CaseDocumentAcasResponse> documents,
                                             CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return;
        }

        caseData.getRespondentCollection().forEach(respondent -> {
            List<DocumentTypeItem> respondentDocs = getSingleRespondentDocs(respondent);
            respondentDocs.stream()
                .map(documentTypeItem ->
                         caseDocumentAcasResponseBuilder(documentTypeItem, authorisation, respondent.getId()))
                .forEach(documents::add);
        });
    }

    private static String getDocumentType(DocumentTypeItem documentTypeItem) {
        return defaultIfEmpty(documentTypeItem.getValue().getDocumentType(),
                              defaultIfEmpty(documentTypeItem.getValue().getTypeOfDocument(), ""));
    }

    private List<DocumentTypeItem> getSingleRespondentDocs(RespondentSumTypeItem respondent) {
        List<DocumentTypeItem> respondentDocs = new ArrayList<>();
        RespondentSumType respondentValue = respondent.getValue();
        if (respondentValue.getEt3Form() != null) {
            respondentDocs.add(
                caseDocumentService.createDocumentTypeItem(ET3, respondentValue.getEt3Form()));
        }
        if (respondentValue.getEt3ResponseRespondentSupportDocument() != null) {
            respondentDocs.add(
                caseDocumentService.createDocumentTypeItem(
                    ET3_ATTACHMENT, respondentValue.getEt3ResponseRespondentSupportDocument()));
        }
        if (CollectionUtils.isNotEmpty(respondentValue.getEt3ResponseContestClaimDocument())) {
            respondentValue.getEt3ResponseContestClaimDocument().forEach(documentTypeItem ->
                 respondentDocs.add(caseDocumentService.createDocumentTypeItem(ET3_ATTACHMENT,
                       documentTypeItem.getValue().getUploadedDocument())));
        }

        return respondentDocs;
    }

    private CaseDocumentAcasResponse caseDocumentAcasResponseBuilder(DocumentTypeItem documentTypeItem,
                                                                     String authorisation, String respondent) {

        UUID uuid = caseDocumentService.getDocumentUuid(documentTypeItem.getValue()
                                                            .getUploadedDocument().getDocumentUrl());

        if (uuid == null) {
            return CaseDocumentAcasResponse.builder().build();
        }
        CaseDocument caseDocument = caseDocumentService.getDocumentDetails(authorisation, uuid).getBody();
        return CaseDocumentAcasResponse.builder()
            .documentType(getDocumentType(documentTypeItem))
            .documentId(uuid.toString())
            .modifiedOn(caseDocument == null
                            ? "No date found"
                            : defaultIfEmpty(caseDocument.getModifiedOn(), "No date found"))
            .respondent(respondent)
            .build();
    }

    /**
     * Given a list of caseIds, this method will return a list of case details.
     *
     * @param authorisation used for IDAM authentication for the query
     * @param caseIds       used as the query parameter
     * @return a list of case details
     */
    public List<CaseDetails> getCaseData(String authorisation, List<String> caseIds) {
        String query = """
            {
              "size": %d,
              "query": {
                "bool": {
                  "filter": [
                    {
                      "terms": {
                        "reference.keyword": %s,
                        "boost": 1.0
                      }
                    }
                  ],
                  "boost": 1.0
                }
              }
            }
            """.formatted(MAX_ES_SIZE, caseIds);
        return searchEnglandScotlandCases(authorisation, query);
    }

    private List<CaseData> searchAndReturnCaseDataList(String authorisation, String query) {
        List<CaseDetails> searchResults = searchEnglandScotlandCases(authorisation, query);
        List<CaseData> caseDataList = new ArrayList<>();
        for (CaseDetails caseDetails : searchResults) {
            caseDataList.add(convertCaseDataMapToCaseDataObject(caseDetails.getData()));
        }
        return caseDataList;
    }

    /**
     * Searches for cases in both England and Scotland case types based on the provided query. Note that this method
     * is a synchronous operation that combines results from both case types to optimize performance.
     * @param authorisation used for IDAM authentication for the query
     * @param query the query string to search for cases
     * @return a list of case details that match the query from both England and Scotland case types
     */
    private List<CaseDetails> searchEnglandScotlandCases(String authorisation, String query) {
        CompletableFuture<List<CaseDetails>> englandSearchFuture = CompletableFuture.supplyAsync(() ->
                        searchCaseType(authorisation, ENGLAND_CASE_TYPE, query),
                taskExecutor
        );

        CompletableFuture<List<CaseDetails>> scotlandSearchFuture = CompletableFuture.supplyAsync(() ->
                        searchCaseType(authorisation, SCOTLAND_CASE_TYPE, query),
                taskExecutor
        );

        return englandSearchFuture.thenCombine(scotlandSearchFuture,
                (englandResults, scotlandResults) -> Stream.concat(englandResults.stream(), scotlandResults.stream())
                .toList())
                .join();
    }

    private List<CaseDetails> searchCaseType(String authorisation, String caseTypeId, String query) {
        List<CaseDetails> caseDetailsList = new ArrayList<>();
        SearchResult searchResult = ccdApiClient.searchCases(authorisation, authTokenGenerator.generate(),
                                                             caseTypeId, query
        );
        if (searchResult != null && CollectionUtils.isNotEmpty(searchResult.getCases())) {
            log.info("ACAS Search result for case type {} is {}", caseTypeId, searchResult.getCases().size());
            caseDetailsList.addAll(searchResult.getCases());
        }
        return caseDetailsList;
    }

    /**
     * Given a caseId, this method will vet and accept the case with static data.
     *
     * @param caseId 16 digit CCD id
     * @return the case details
     */
    public CaseDetails vetAndAcceptCase(String caseId) {
        String authorization = idamClient.getAccessToken(caseWorkerUserName, caseWorkerPassword);
        CaseDetails caseDetails = ccdApiClient.getCase(authorization, authTokenGenerator.generate(), caseId);
        if (ObjectUtils.isEmpty(caseDetails)) {
            throw new IllegalArgumentException("Case not found");
        }
        String caseTypeId = caseDetails.getCaseTypeId();
        StartEventResponse startEventResponse = startCaseUpdate(caseId, authorization, caseTypeId, "et1Vetting");
        CaseData caseData = convertCaseDataMapToCaseDataObject(startEventResponse.getCaseDetails().getData());
        setVettingData(caseData);
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(new ObjectMapper());
        submitCaseUpdate(caseId, authorization, caseTypeId, caseDetailsConverter, startEventResponse, caseData);

        startEventResponse = startCaseUpdate(caseId, authorization, caseTypeId, "preAcceptanceCase");
        caseData = convertCaseDataMapToCaseDataObject(startEventResponse.getCaseDetails().getData());
        setAcceptanceData(caseData);
        return submitCaseUpdate(caseId, authorization, caseTypeId, caseDetailsConverter, startEventResponse, caseData);
    }

    private CaseDetails submitCaseUpdate(String caseId, String authorization, String caseTypeId,
                                         CaseDetailsConverter caseDetailsConverter,
                                         StartEventResponse startEventResponse, CaseData caseData) {
        String s2sToken = authTokenGenerator.generate();
        UserInfo userInfo = idamClient.getUserInfo(authorization);
        return ccdApiClient.submitEventForCaseWorker(
            authorization,
            s2sToken,
            userInfo.getUid(),
            EMPLOYMENT,
            caseTypeId,
            caseId,
            true,
            caseDetailsConverter.caseDataContent(startEventResponse, caseData)
        );
    }

    private StartEventResponse startCaseUpdate(String caseId, String authorization, String caseTypeId,
                                               String eventId) {
        String s2sToken = authTokenGenerator.generate();
        UserInfo userInfo = idamClient.getUserInfo(authorization);
        return ccdApiClient.startEventForCaseWorker(
            authorization,
            s2sToken,
            userInfo.getUid(),
            EMPLOYMENT,
            caseTypeId,
            caseId,
            eventId
        );
    }

    private void setAcceptanceData(CaseData caseData) {
        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setCaseAccepted(YES);
        casePreAcceptType.setDateAccepted(LocalDate.now().toString());
        caseData.setPreAcceptCase(casePreAcceptType);
    }

    private static void setVettingData(CaseData caseData) {
        caseData.setAreTheseCodesCorrect(YES);
        caseData.setEt1GovOrMajorQuestion(NO);
        caseData.setEt1ReasonableAdjustmentsQuestion(NO);
        caseData.setEt1SuggestHearingVenue(NO);
        caseData.setEt1VettingAcasCertExemptYesOrNo1(YES);
        caseData.setEt1VettingCanServeClaimYesOrNo(YES);
        caseData.setEt1VideoHearingQuestion(YES);
        caseData.setIsLocationCorrect(YES);
    }

}
