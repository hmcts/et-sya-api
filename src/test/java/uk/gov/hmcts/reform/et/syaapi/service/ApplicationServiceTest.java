package uk.gov.hmcts.reform.et.syaapi.service;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.ViewAnApplicationRequest;
import uk.gov.service.notify.NotificationClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@SuppressWarnings({"PMD.SingularField", "PMD.TooManyMethods"})
class ApplicationServiceTest {

    public static final String RESPONDENT_LIST =
        "Test Respondent Organisation -1-, Mehmet Tahir Dede, Abuzer Kadayif, Kate Winslet, Jeniffer Lopez";
    public static final String NOT_SET = "Not set";
    public static final String CASE_REF = "123456/2022";
    public static final String CLAIMANT = "Michael Jackson";
    public static final String CASE_ID = "1646225213651590";
    @MockBean
    private CaseService caseService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private CaseDocumentService caseDocumentService;
    @MockBean
    private ApplicationService applicationService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    private final TestData testData;

    ApplicationServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() throws CaseDocumentException, DocumentGenerationException {
        caseService = mock(CaseService.class);
        notificationService = mock(NotificationService.class);
        caseDocumentService = mock(CaseDocumentService.class);
        caseDetailsConverter = mock(CaseDetailsConverter.class);

        applicationService = new ApplicationService(
            caseService,
            notificationService,
            caseDocumentService,
            caseDetailsConverter
        );

        when(caseService.getUserCase(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getClaimantApplicationRequest().getCaseId()
        )).thenReturn(testData.getCaseDetailsWithData());

        doNothing().when(caseService).uploadTseSupportingDocument(any(), any());
        doNothing().when(caseService).uploadTseCyaAsPdf(any(), any(), any(), any());

        when(caseService.triggerEvent(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(testData.getClaimantApplicationRequest().getCaseId()),
            eq(CaseEvent.SUBMIT_CLAIMANT_TSE),
            eq(testData.getClaimantApplicationRequest().getCaseTypeId()),
            any()
        )).thenReturn(testData.getCaseDetailsWithData());

        ResponseEntity<ByteArrayResource> responseEntity =
            new ResponseEntity<>(HttpStatus.OK);
        when(caseDocumentService.downloadDocument(eq(TEST_SERVICE_AUTH_TOKEN), any())).thenReturn(responseEntity);

    }

    @Test
    void shouldSendClaimantEmailWithCorrectParameters() throws NotificationClientException {
        applicationService.submitApplication(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getClaimantApplicationRequest()
        );
        verify(notificationService, times(1)).sendAcknowledgementEmailToClaimant(
            any(),
            eq(CLAIMANT),
            eq(CASE_REF),
            eq(RESPONDENT_LIST),
            eq(NOT_SET),
            eq(CASE_ID),
            any()
        );
    }

    @Test
    void shouldSendRespondentEmailWithCorrectParameters() throws NotificationClientException {
        byte[] bytes = "Sample".getBytes();
        ResponseEntity<ByteArrayResource> responseEntity =
            new ResponseEntity<>(new ByteArrayResource(bytes), HttpStatus.OK);

        when(caseDocumentService.downloadDocument(eq(TEST_SERVICE_AUTH_TOKEN), any())).thenReturn(responseEntity);

        applicationService.submitApplication(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getClaimantApplicationRequest()
        );

        verify(notificationService, times(1)).sendAcknowledgementEmailToRespondents(
            any(),
            eq(CLAIMANT),
            eq(CASE_REF),
            eq(RESPONDENT_LIST),
            eq(NOT_SET),
            eq(CASE_ID),
            any(),
            any()
        );
    }

    @Test
    void shouldSendRespondentEmailWithNoSupportingDocument() throws NotificationClientException {
        applicationService.submitApplication(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getClaimantApplicationRequest()
        );

        verify(notificationService, times(1)).sendAcknowledgementEmailToRespondents(
            any(),
            eq(CLAIMANT),
            eq(CASE_REF),
            eq(RESPONDENT_LIST),
            eq(NOT_SET),
            eq(CASE_ID),
            eq(null),
            any()
        );
    }

    @Test
    void shouldSendTribunalEmailWithCorrectParameters() throws NotificationClientException {
        applicationService.submitApplication(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getClaimantApplicationRequest()
        );

        verify(notificationService, times(1)).sendAcknowledgementEmailToTribunal(
            any(),
            eq(CLAIMANT),
            eq(CASE_REF),
            eq(RESPONDENT_LIST),
            eq(NOT_SET),
            eq(CASE_ID),
            any()
        );
    }

    @Test
    void shouldSubmitResponseToApplication() {
        RespondToApplicationRequest testRequest = testData.getRespondToApplicationRequest();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getUpdateCaseEventResponse());

        applicationService.respondToApplication(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest
        );

        verify(caseDetailsConverter, times(1)).caseDataContent(
            any(),
            any()
        );
    }

    @Test
    void shouldSubmitResponseToApplicationAndSendCopy() throws CaseDocumentException, DocumentGenerationException {
        RespondToApplicationRequest testRequest = testData.getRespondToApplicationRequest();
        testRequest.getResponse().setCopyToOtherParty("Yes");

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getUpdateCaseEventResponse());

        applicationService.respondToApplication(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest
        );

        verify(caseService, times(1)).createResponsePdf(
            eq(TEST_SERVICE_AUTH_TOKEN),
            any(),
            eq(testRequest),
            any()
        );

        verify(caseDetailsConverter, times(1)).caseDataContent(
            any(),
            any()
        );
    }

    @Test
    void shouldNotSubmitResponseToApplication() {
        RespondToApplicationRequest testRequest = testData.getRespondToApplicationRequest();
        testRequest.setApplicationId("12");
        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getUpdateCaseEventResponse());

        assertThrows(
            IllegalArgumentException.class,
            () -> applicationService.respondToApplication(
                TEST_SERVICE_AUTH_TOKEN,
                testRequest
            )
        );

        verify(caseDetailsConverter, times(0)).caseDataContent(
            any(),
            any()
        );
    }

    @Test
    void shouldThrowErrorWhenSavingApplication() {
        RespondToApplicationRequest testRequest = testData.getRespondToApplicationRequest();
        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getUpdateCaseEventResponse());

        when(caseService.submitUpdate(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(testRequest.getCaseId()),
            any(),
            any()
        )).thenThrow(new JSONException("Could not save response"));

        assertThrows(
            JSONException.class,
            () -> applicationService.respondToApplication(
                TEST_SERVICE_AUTH_TOKEN,
                testRequest
            )
        );
    }

    @Test
    void shouldSendResponseEmailsToClaimantWithCorrectParameters() {
        RespondToApplicationRequest testRequest = testData.getRespondToApplicationRequest();
        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getUpdateCaseEventResponse());

        applicationService.respondToApplication(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest
        );

        verify(notificationService, times(1)).sendResponseEmailToClaimant(
            any(),
            eq(CLAIMANT),
            eq(CASE_ID),
            eq(""),
            eq(NOT_SET),
            eq("12345"),
            eq("Amend details"),
            eq("No")
        );
    }

    @Test
    void shouldMarkApplicationAsViewed() {
        ViewAnApplicationRequest testRequest = testData.getViewAnApplicationRequest();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getUpdateCaseEventResponse());

        applicationService.markApplicationAsViewed(TEST_SERVICE_AUTH_TOKEN, testRequest);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        String actualState
            = argumentCaptor.getValue().getGenericTseApplicationCollection().get(0).getValue().getApplicationState();
        assertThat(actualState).isEqualTo("viewed");
    }
}
