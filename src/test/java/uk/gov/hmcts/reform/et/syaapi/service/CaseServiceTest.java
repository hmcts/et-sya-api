package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.apache.commons.collections4.MultiValuedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.constants.JurisdictionCodesConstants;
import uk.gov.hmcts.reform.et.syaapi.helper.JurisdictionCodesMapper;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocumentAcasResponse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfService;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfServiceException;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.SendEmailResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.DocumentCategoryConstants.ET1_PDF_DOC_CATEGORY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET1_ONLINE_SUBMISSION;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_CASE_DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper.mapRequestCaseDataToCaseData;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.SUBMIT_CASE_DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.USER_ID;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.AvoidDuplicateLiterals", "PMD.TooManyFields"})
class CaseServiceTest {

    @Mock
    private PostcodeToOfficeService postcodeToOfficeService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi ccdApiClient;
    @Mock
    private IdamClient idamClient;
    @Mock
    private JurisdictionCodesMapper jurisdictionCodesMapper;
    @Mock
    private PdfService pdfService;
    @Mock
    private AcasService acasService;
    @Mock
    private CaseDocumentService caseDocumentService;
    @Mock
    private DocumentGenerationService documentGenerationService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CaseOfficeService assignCaseToLocalOfficeService;
    @Spy
    private NotificationsProperties notificationsProperties;
    @InjectMocks
    private CaseService caseService;
    private SendEmailResponse sendEmailResponse;
    private final CaseTestData caseTestData;
    public static final String TEST = "test";
    private static final byte[] TSE_PDF_BYTES = TEST.getBytes();
    private static final String TSE_PDF_NAME = "contact_about_something_else.pdf";
    private static final String PDF_FILE_TIKA_CONTENT_TYPE = "application/pdf";
    private static final String TSE_PDF_DESCRIPTION = "Test description";

    private static final String ALL_CASES_QUERY = "{\"size\":10000,\"query\":{\"match_all\": {}}}";
    private final PdfDecodedMultipartFile tsePdfMultipartFileMock = new PdfDecodedMultipartFile(
        TSE_PDF_BYTES,
        TSE_PDF_NAME,
        PDF_FILE_TIKA_CONTENT_TYPE,
        TSE_PDF_DESCRIPTION
    );

    CaseServiceTest() {
        caseTestData = new CaseTestData();
    }

    @BeforeEach
    void setUpForSubmitCaseTests(TestInfo testInfo) {
        if (!testInfo.getDisplayName().startsWith("submitCase")) {
            return;
        }
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserInfo(
            null,
            USER_ID,
            TEST_NAME,
            caseTestData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            caseTestData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));

        caseTestData.getCaseRequest().setCaseId("1668421480426211");
        when(ccdApiClient.submitEventForCitizen(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(USER_ID),
            eq(EtSyaConstants.JURISDICTION_ID),
            eq(EtSyaConstants.SCOTLAND_CASE_TYPE),
            any(String.class),
            eq(true),
            any(CaseDataContent.class)
        )).thenReturn(caseTestData.getExpectedDetails());

        when(ccdApiClient.startEventForCitizen(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(USER_ID),
            eq(EtSyaConstants.JURISDICTION_ID),
            eq(EtSyaConstants.SCOTLAND_CASE_TYPE),
            any(String.class),
            any(String.class)
        )).thenReturn(caseTestData.getStartEventResponse());

        PdfDecodedMultipartFile pdfDecodedMultipartFile =
            new PdfDecodedMultipartFile(
                new byte[0],
                TEST,
                TEST,
                TEST
            );

        when(pdfService.convertAcasCertificatesToPdfDecodedMultipartFiles(any(), any()))
            .thenReturn(List.of(pdfDecodedMultipartFile));

        when(pdfService.convertCaseDataToPdfDecodedMultipartFile(any(), any()))
            .thenReturn(List.of(pdfDecodedMultipartFile));

        when(acasService.getAcasCertificatesByCaseData(any())).thenReturn(List.of());

        when(assignCaseToLocalOfficeService.convertCaseRequestToCaseDataWithTribunalOffice(any()))
            .thenReturn(caseTestData.getCaseData());
        sendEmailResponse
            = new SendEmailResponse("""
                                        {
                                          "id": "8835039a-3544-439b-a3da-882490d959eb",
                                          "reference": "TEST_EMAIL_ALERT",
                                          "template": {
                                            "id": "8835039a-3544-439b-a3da-882490d959eb",
                                            "version": "3",
                                            "uri": "TEST"
                                          },
                                          "content": {
                                            "body": "Dear test, Please see your detail as 123456789. Regards, ET Team.",
                                            "subject": "ET Test email created",
                                            "from_email": "TEST@GMAIL.COM"
                                          }
                                        }
                                        """);
        when(notificationService.sendSubmitCaseConfirmationEmail(any(), any(), any(), any()))
            .thenReturn(sendEmailResponse);
    }

    @Test
    void shouldGetUserCase() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.getCase(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            caseTestData.getCaseRequest().getCaseId()
        )).thenReturn(caseTestData.getExpectedDetails());

        CaseRequest caseRequest = CaseRequest.builder()
            .caseId(caseTestData.getCaseRequest().getCaseId()).build();

        CaseDetails caseDetails = caseService.getUserCase(TEST_SERVICE_AUTH_TOKEN, caseRequest.getCaseId());

        assertEquals(caseTestData.getExpectedDetails(), caseDetails);
    }

    @Test
    void shouldGetAllUserCases() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            ALL_CASES_QUERY
        )).thenReturn(caseTestData.requestCaseDataListSearchResult());

        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            ALL_CASES_QUERY
        )).thenReturn(SearchResult.builder().build());

        List<CaseDetails> caseDetails = caseService.getAllUserCases(TEST_SERVICE_AUTH_TOKEN);

        assertEquals(caseTestData.getRequestCaseDataList(), caseDetails);
    }

    @Test
    void shouldGetAllUserCasesDifferentCaseType() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            ALL_CASES_QUERY
        )).thenReturn(caseTestData.getSearchResultRequestCaseDataListScotland());

        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            ALL_CASES_QUERY
        )).thenReturn(caseTestData.getSearchResultRequestCaseDataListEngland());

        List<CaseDetails> caseDetails = caseService.getAllUserCases(TEST_SERVICE_AUTH_TOKEN);

        assertThat(caseTestData.getExpectedCaseDataListCombined())
            .hasSize(caseDetails.size()).hasSameElementsAs(caseDetails);
    }

    @Test
    void shouldCreateNewDraftCaseInCcd() {

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserInfo(
            null,
            USER_ID,
            TEST_NAME,
            caseTestData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            caseTestData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));
        when(ccdApiClient.startForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            EtSyaConstants.DRAFT_EVENT_TYPE
        )).thenReturn(
            caseTestData.getStartEventResponse());

        when(ccdApiClient.submitForCitizen(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(USER_ID),
            eq(EtSyaConstants.JURISDICTION_ID),
            eq(EtSyaConstants.ENGLAND_CASE_TYPE),
            eq(true),
            any(CaseDataContent.class)
        )).thenReturn(caseTestData.getExpectedDetails());

        CaseRequest caseRequest = caseTestData.getCaseRequest();

        CaseDetails caseDetails = caseService.createCase(
            TEST_SERVICE_AUTH_TOKEN,
            caseRequest
        );

        assertEquals(caseTestData.getExpectedDetails(), caseDetails);
    }

    @Test
    void shouldStartUpdateCaseInCcd() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserInfo(
            null,
            USER_ID,
            TEST_NAME,
            caseTestData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            caseTestData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));

        when(ccdApiClient.startEventForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            caseTestData.getCaseRequest().getCaseId(),
            String.valueOf(UPDATE_CASE_DRAFT)
        )).thenReturn(
            caseTestData.getStartEventResponse());

        StartEventResponse eventResponse = caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            caseTestData.getCaseRequest().getCaseId(),
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            UPDATE_CASE_DRAFT
        );

        assertEquals(EtSyaConstants.SCOTLAND_CASE_TYPE, eventResponse.getCaseDetails().getCaseTypeId());
    }

    @Test
    void shouldSubmitUpdateCaseInCcd() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserInfo(
            null,
            USER_ID,
            TEST_NAME,
            caseTestData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            caseTestData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));
        when(ccdApiClient.submitEventForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            caseTestData.getCaseRequest().getCaseId(),
            true,
            caseTestData.getUpdateCaseDataContent()
        )).thenReturn(caseTestData.getExpectedDetails());

        CaseDetails caseDetails = caseService.submitUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            caseTestData.getCaseRequest().getCaseId(),
            caseTestData.getUpdateCaseDataContent(),
            EtSyaConstants.SCOTLAND_CASE_TYPE
        );

        assertEquals(caseDetails, caseTestData.getExpectedDetails());
    }

    @SneakyThrows
    @Test
    void submitCaseShouldAddSupportingDocumentToDocumentCollection() {
        when(caseDocumentService.uploadAllDocuments(any(), any(), any(), any(), any()))
            .thenReturn(List.of(createDocumentTypeItem("Other")));

        CaseDetails caseDetails = caseService.submitCase(
            TEST_SERVICE_AUTH_TOKEN,
            caseTestData.getCaseRequest()
        );

        assertEquals(1, ((ArrayList<?>)caseDetails.getData().get("documentCollection")).size());
        List<?> docCollection = (List<?>) caseDetails.getData().get("documentCollection");

        assertEquals("DocumentType(typeOfDocument="
             + "Other, uploadedDocument=UploadedDocumentType(documentBinaryUrl=http://document.url/2333482f-1eb9-44f1"
             + "-9b78-f5d8f0c74b15/binary, documentFilename=filename, documentUrl=http://document.binary"
             + ".url/2333482f-1eb9-44f1-9b78-f5d8f0c74b15, categoryId=C11, uploadTimestamp=null), "
             + "ownerDocument=null, creationDate=null, "
             + "shortDescription=null, topLevelDocuments=null, startingClaimDocuments=null, "
             + "responseClaimDocuments=null, initialConsiderationDocuments=null, "
             + "caseManagementDocuments=null, withdrawalSettledDocuments=null, hearingsDocuments=null, "
             + "judgmentAndReasonsDocuments=null, reconsiderationDocuments=null, miscDocuments=null, "
             + "documentType=null, dateOfCorrespondence=null)",
                     ((DocumentTypeItem) docCollection.get(0)).getValue().toString());
    }

    @Test
    void submitCaseShouldSendErrorEmail() throws PdfServiceException, CaseDocumentException {
        when(caseDocumentService.uploadAllDocuments(any(), any(), any(), any(), any()))
            .thenThrow(new CaseDocumentException("Failed to upload documents"));

        when(notificationService.sendDocUploadErrorEmail(any(), any(), any(), any()))
            .thenReturn(sendEmailResponse);

        caseService.submitCase(
            TEST_SERVICE_AUTH_TOKEN,
            caseTestData.getCaseRequest()
        );

        verify(notificationService, times(1))
            .sendDocUploadErrorEmail(any(), any(), any(), any());
    }

    @SneakyThrows
    @Test
    void submitCaseShouldSetEt1OnlineSubmission() {
        CaseDetails caseDetails = caseService.submitCase(
            TEST_SERVICE_AUTH_TOKEN,
            caseTestData.getCaseRequest()
        );

        assertEquals(YES, caseDetails.getData().get(ET1_ONLINE_SUBMISSION));
    }

    private DocumentTypeItem createDocumentTypeItem(String typeOfDocument) {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentFilename("filename");
        uploadedDocumentType.setDocumentUrl("http://document.binary.url/2333482f-1eb9-44f1-9b78-f5d8f0c74b15");
        uploadedDocumentType.setDocumentBinaryUrl("http://document.url/2333482f-1eb9-44f1-9b78-f5d8f0c74b15/binary");
        uploadedDocumentType.setCategoryId(ET1_PDF_DOC_CATEGORY);
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(UUID.randomUUID().toString());

        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(typeOfDocument);
        documentType.setUploadedDocument(uploadedDocumentType);

        documentTypeItem.setValue(documentType);
        return documentTypeItem;
    }

    @Test
    void shouldLastModifiedCasesIdWhenCaseFoundThenCaseId() {
        LocalDateTime requestDateTime =
            LocalDateTime.parse("2022-09-01T12:34:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(1)
            .cases(caseTestData.getRequestCaseDataListEngland())
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(2)
            .cases(caseTestData.getRequestCaseDataListScotland())
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            generateCaseDataEsQueryWithDate(requestDateTime)
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            generateCaseDataEsQueryWithDate(requestDateTime)
        )).thenReturn(scotlandSearchResult);

        assertThat(caseService.getLastModifiedCasesId(TEST_SERVICE_AUTH_TOKEN, requestDateTime))
            .hasSize(3)
            .isEqualTo(List.of(1_646_225_213_651_598L, 1_646_225_213_651_533L, 1_646_225_213_651_512L));
    }

    @Test
    void shouldLastModifiedCasesIdWhenNoCaseFoundThenEmpty() {
        LocalDateTime requestDateTime =
            LocalDateTime.parse("2022-09-01T12:34:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            generateCaseDataEsQueryWithDate(requestDateTime)
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            generateCaseDataEsQueryWithDate(requestDateTime)
        )).thenReturn(scotlandSearchResult);

        assertThat(caseService.getLastModifiedCasesId(TEST_SERVICE_AUTH_TOKEN, requestDateTime))
            .isEmpty();
    }

    private String generateCaseDataEsQueryWithDate(LocalDateTime requestDateTime) {
        return """
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
    }

    @Test
    void shouldReturnCaseData() {
        List<String> caseIds = List.of("1646225213651598", "1646225213651533", "1646225213651512");
        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(1)
            .cases(caseTestData.getRequestCaseDataListEngland())
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(2)
            .cases(caseTestData.getRequestCaseDataListScotland())
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE, generateCaseDataEsQuery(caseIds)
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE, generateCaseDataEsQuery(caseIds)
        )).thenReturn(scotlandSearchResult);

        List<CaseDetails> caseDetailsList = caseService.getCaseData(TEST_SERVICE_AUTH_TOKEN, caseIds);
        assertThat(caseDetailsList)
            .hasSize(3)
            .isEqualTo(caseTestData.getExpectedCaseDataListCombined());
    }

    @Test
    void shouldReturnCaseDataNoCasesFound() {
        List<String> caseIds = List.of("1646225213651598", "1646225213651533");
        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE, generateCaseDataEsQuery(caseIds)
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE, generateCaseDataEsQuery(caseIds)
        )).thenReturn(scotlandSearchResult);

        List<CaseDetails> caseDetailsList = caseService.getCaseData(TEST_SERVICE_AUTH_TOKEN, caseIds);
        assertThat(caseDetailsList).isEmpty();
    }

    private String generateCaseDataEsQuery(List<String> caseIds) {
        return """
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
    }

    @Nested
    class UploadTseSupportingDocument {
        @Test
        void setsShortDescriptionCorrectly() {
            CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
            String actual = "withdraw";
            caseService.uploadTseSupportingDocument(caseDetails, new UploadedDocumentType(), actual);

            CaseData caseData = mapRequestCaseDataToCaseData(caseDetails.getData());
            String expected = caseData.getDocumentCollection().get(0).getValue().getShortDescription();
            assertThat(actual).isEqualTo(expected);
        }
    }

    @Test
    void shouldInvokeCaseEnrichmentWithJurCodesInSubmitEvent() {
        List<JurCodesTypeItem> expectedItems = mockJurCodesTypeItems();
        caseTestData.getStartEventResponse().setEventId(SUBMIT_CASE_DRAFT);
        CaseData caseData = mapRequestCaseDataToCaseData(caseTestData.getCaseDataWithClaimTypes()
                                                                                  .getCaseData());
        caseData.setJurCodesCollection(expectedItems);
        caseData.setFeeGroupReference(CASE_ID);

        CaseDataContent expectedEnrichedData = CaseDataContent.builder()
            .event(Event.builder().id(SUBMIT_CASE_DRAFT).build())
            .eventToken(caseTestData.getStartEventResponse().getToken())
            .data(caseData)
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserInfo(
            null,
            USER_ID,
            TEST_NAME,
            TestConstants.TEST_FIRST_NAME,
            TestConstants.TEST_SURNAME,
            null
        ));

        when(ccdApiClient.startEventForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            CASE_ID,
            SUBMIT_CASE_DRAFT
        )).thenReturn(caseTestData.getStartEventResponse());

        lenient().when(ccdApiClient.submitEventForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            CASE_ID,
            true,
            expectedEnrichedData
        )).thenReturn(caseTestData.getExpectedDetails());

        when(jurisdictionCodesMapper.mapToJurCodes(any())).thenReturn(expectedItems);

        caseService.triggerEventForSubmitCase(
            TEST_SERVICE_AUTH_TOKEN,
            CaseRequest.builder()
                .caseId(CASE_ID)
                .caseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .caseData(new HashMap<>())
                .build()
        );

        verify(ccdApiClient).submitEventForCitizen(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyBoolean(),
            any()
        );
    }

    @Test
    void shouldInvokeClaimantTsePdf()
        throws DocumentGenerationException {
        when(pdfService.convertClaimantTseIntoMultipartFile(any(), any(), any(), anyString())).thenReturn(
            tsePdfMultipartFileMock);

        assertDoesNotThrow(() ->
                               caseService.uploadTseCyaAsPdf(
                                   TEST_SERVICE_AUTH_TOKEN,
                                   caseTestData.getCaseDetails(),
                                   caseTestData.getClaimantTse(),
                                   "TEST"
                               )
        );
    }

    @SneakyThrows
    @Test
    void givenPdfServiceErrorProducesDocumentGenerationException() {
        when(pdfService.convertClaimantTseIntoMultipartFile(any(), any(), any(), anyString())).thenThrow(
            new DocumentGenerationException(TEST));

        assertThrows(DocumentGenerationException.class, () -> caseService.uploadTseCyaAsPdf(
            "", caseTestData.getCaseDetails(), caseTestData.getClaimantTse(), ""));
    }

    private List<JurCodesTypeItem> mockJurCodesTypeItems() {
        JurCodesTypeItem item = new JurCodesTypeItem();
        JurCodesType type = new JurCodesType();
        type.setJuridictionCodesList(JurisdictionCodesConstants.BOC);
        item.setValue(type);
        return List.of(item);
    }

    @Test
    void retrieveAcasDocuments() {
        when(idamClient.getAccessToken(any(), any())).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        String caseId = "1646225213651598";
        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(1)
            .cases(caseTestData.getRequestCaseDataListEnglandAcas())
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE, generateCaseDataEsQuery(Collections.singletonList(caseId))
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE, generateCaseDataEsQuery(Collections.singletonList(caseId))
        )).thenReturn(scotlandSearchResult);
        when(caseDocumentService.createDocumentTypeItem(any(), any())).thenReturn(
            createDocumentTypeItem("ET1 Attachment"));
        when(caseDocumentService.getDocumentDetails(anyString(), any())).thenReturn(getDocumentDetails());
        MultiValuedMap<String, CaseDocumentAcasResponse> documents = caseService.retrieveAcasDocuments(caseId);
        assertNotNull(documents);
        assertThat(documents.size()).isEqualTo(3);
    }

    private ResponseEntity<CaseDocument> getDocumentDetails() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<>(
            CaseDocument.builder()
                .size("size").mimeType("mimeType").hashToken("token").createdOn("createdOn").createdBy("createdBy")
                .lastModifiedBy("lastModifiedBy").modifiedOn("modifiedOn").ttl("ttl")
                .metadata(Map.of("test", "test"))
                .originalDocumentName("docName.txt").classification("PUBLIC")
                .links(Map.of("self", Map.of("href", "TestURL.com"))).build(),
            headers,
            HttpStatus.OK
        );
    }
}
