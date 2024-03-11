package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocumentAcasResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ACAS_HIDDEN_DOCS;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET1_ATTACHMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET3;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET3_ATTACHMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.POSSIBLE_DUPLICATED_DOCS;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;

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
                          "gte": "%s",
                          "boost": 1.0
                        }
                      }
                    }
                  ],
                  "boost": 1.0
                }
              }
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
    public MultiValuedMap<String, CaseDocumentAcasResponse> retrieveAcasDocuments(String caseId) {
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

    private MultiValuedMap<String, CaseDocumentAcasResponse> getDocumentUuids(String query) {
        String authorisation = idamClient.getAccessToken(caseWorkerUserName, caseWorkerPassword);
        List<CaseData> caseDataList = searchAndReturnCaseDataList(authorisation, query);

        MultiValuedMap<String, CaseDocumentAcasResponse> documentIds = new ArrayListValuedHashMap<>();
        for (CaseData caseData : caseDataList) {
            List<DocumentTypeItem> documentTypeItemList = getAllCaseDocuments(authorisation, documentIds, caseData);
            addDocumentsToMap(authorisation, documentIds, documentTypeItemList);
        }

        return documentIds;
    }

    private void addDocumentsToMap(String authorisation, MultiValuedMap<String, CaseDocumentAcasResponse> documentIds,
                                   List<DocumentTypeItem> documentTypeItemList) {
        for (DocumentTypeItem documentTypeItem : documentTypeItemList) {
            boolean duplicatedDoc = false;
            String documentType = getDocumentType(documentTypeItem);
            if (POSSIBLE_DUPLICATED_DOCS.contains(documentType)) {
                duplicatedDoc = isDuplicatedDoc(documentIds, documentTypeItem);
            }

            if (!duplicatedDoc) {
                documentIds.put(documentType, caseDocumentAcasResponseBuilder(documentTypeItem, authorisation, null));
            }
        }
    }

    private boolean isDuplicatedDoc(MultiValuedMap<String, CaseDocumentAcasResponse> documentIds,
                                    DocumentTypeItem documentTypeItem) {
        String documentUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentUrl();
        UUID uuid = caseDocumentService.getDocumentUuid(documentUrl);
        if (uuid != null) {
            List<CaseDocumentAcasResponse> caseDocList = documentIds.values().stream().toList();
            for (CaseDocumentAcasResponse caseDocumentAcasResponse: caseDocList) {
                if (uuid.toString().equals(defaultIfEmpty(caseDocumentAcasResponse.getDocumentId(), ""))) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<DocumentTypeItem> getAllCaseDocuments(String authorisation, MultiValuedMap<String,
        CaseDocumentAcasResponse> documentIds, CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getRespondentCollection())) {
            caseData.getRespondentCollection().forEach(respondent -> {
                List<DocumentTypeItem> respondentDocs = getRespondentDocs(respondent);
                for (DocumentTypeItem documentTypeItem : respondentDocs) {
                    String documentType = getDocumentType(documentTypeItem);
                    documentIds.put(documentType,
                        caseDocumentAcasResponseBuilder(documentTypeItem, authorisation, respondent.getId()));
                }
            });
        }

        List<DocumentTypeItem> documentTypeItemList = caseData.getDocumentCollection()
            .stream()
            .filter(d -> !isNullOrEmpty(getDocumentType(d)) && !ACAS_HIDDEN_DOCS.contains(getDocumentType(d)))
            .collect(toList());

        if (caseData.getClaimantRequests() != null
            && caseData.getClaimantRequests().getClaimDescriptionDocument() != null) {
            documentTypeItemList.add(caseDocumentService.createDocumentTypeItem(
                ET1_ATTACHMENT, caseData.getClaimantRequests().getClaimDescriptionDocument()
            ));
        }
        return documentTypeItemList;
    }

    private static String getDocumentType(DocumentTypeItem documentTypeItem) {
        return defaultIfEmpty(documentTypeItem.getValue().getDocumentType(),
                              defaultIfEmpty(documentTypeItem.getValue().getTypeOfDocument(), ""));
    }

    private List<DocumentTypeItem> getRespondentDocs(RespondentSumTypeItem respondent) {
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
        if (searchResult != null && CollectionUtils.isNotEmpty(searchResult.getCases())) {
            caseDetailsList.addAll(searchResult.getCases());
        }
        return caseDetailsList;
    }

}