package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.DOC_UPLOAD_ERROR_EMAIL_TEMPLATE_ID;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.EMAIL_TEST_SERVICE_OWNER_GMAIL_COM;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.NOTIFICATION_CONFIRMATION_ID;

@SuppressWarnings({"PMD.TooManyMethods"})
class NotificationServiceTest {
    private static final String TEST_TEMPLATE_API_KEY = "dummy template id";

    private static final String SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID = "af0b26b7-17b6-4643-bbdc-e296d11e7b0c";

    private static final String REFERENCE_STRING = "TEST_EMAIL_ALERT";

    private static final String TEST_EMAIL = "TEST@GMAIL.COM";

    @MockBean
    private NotificationService notificationService;

    private NotificationClient notificationClient;
    private NotificationsProperties notificationsProperties;

    private final ConcurrentHashMap<String, String> parameters = new ConcurrentHashMap<>();

    private SendEmailResponse inputSendEmailResponse;

    private TestData testData;

    @BeforeEach
    void before() throws NotificationClientException {
        parameters.put("firstname", "test");
        parameters.put("references", "123456789");
        inputSendEmailResponse = new SendEmailResponse("{\n"
                   + "  \"id\": \"f30b2148-b1a6-4c0d-8a10-50109c96dc2c\",\n"
                   + "  \"reference\": \"TEST_EMAIL_ALERT\",\n"
                   + "  \"template\": {\n"
                   + "    \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n"
                   + "    \"version\": \"3\",\n"
                   + "    \"uri\": \"TEST\"\n"
                   + "  },\n"
                   + "  \"content\": {\n"
                   + "    \"body\": \"Dear test, Please see your detail as 123456789. Regards, ET Team.\",\n"
                   + "    \"subject\": \"ET Test email created\",\n"
                   + "    \"from_email\": \"TEST@GMAIL.COM\"\n"
                   + "  }\n"
                   + "}\n");
        notificationClient = mock(NotificationClient.class);
        notificationsProperties = mock(NotificationsProperties.class);
        notificationService = new NotificationService(notificationClient, notificationsProperties);
        given(notificationClient.sendEmail(anyString(), anyString(), any(), anyString()))
            .willReturn(inputSendEmailResponse);
        given(notificationsProperties.getCySubmitCaseEmailTemplateId()).willReturn("1234_welsh");
        given(notificationsProperties.getSubmitCaseEmailTemplateId())
            .willReturn(SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID);
        given(notificationsProperties.getCitizenPortalLink()).willReturn(REFERENCE_STRING);
        testData = new TestData();
    }

    @SneakyThrows
    @Test
    void shouldSendEmailByMockingResponse() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
        assertThat(sendEmailResponse.getReference().isPresent()).isTrue();
        assertThat(sendEmailResponse.getReference().get()).isEqualTo(REFERENCE_STRING);
    }

    @SneakyThrows
    @Test
    void shouldRetrieveTemplateIdCorrectly() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
        assertThat(sendEmailResponse.getTemplateId()).isEqualTo(
            UUID.fromString("8835039a-3544-439b-a3da-882490d959eb"));
    }

    @SneakyThrows
    @Test
    void shouldRetrieveEmailIdCorrectly() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
        assertThat(sendEmailResponse.getFromEmail()).isNotEmpty();
    }

    @SneakyThrows
    @Test
    void shouldRetrieveNotificationIdCorrectly() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
        assertThat(sendEmailResponse.getNotificationId()).isEqualTo(
            UUID.fromString("8835039a-3544-439b-a3da-882490d959eb"));
    }


    @Test
    void ifTargetEmailIsNullWillThrowNotificationException() throws NotificationClientException {
        given(notificationClient.sendEmail(anyString(), nullable(String.class), any(), anyString()))
            .willThrow(new NotificationClientException("email_address is a required property"));
        assertThatThrownBy(
            () -> notificationService.sendEmail(TEST_TEMPLATE_API_KEY, null, parameters, REFERENCE_STRING))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("email_address is a required property");
    }


    @Test
    void ifTemplateIdIsNullWillThrowNotificationException() throws NotificationClientException {
        given(notificationClient.sendEmail(nullable(String.class), anyString(), any(), anyString()))
            .willThrow(new NotificationClientException("template_id is a required property"));
        assertThatThrownBy(
            () -> notificationService.sendEmail(null, TEST_EMAIL, parameters, REFERENCE_STRING))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("template_id is a required property");
    }

    @Test
    void ifTemplateNotFoundWillThrowNotificationException() throws NotificationClientException {

        given(notificationClient.sendEmail(anyString(), anyString(), any(), anyString()))
            .willThrow(new NotificationClientException("Template not found"));
        assertThatThrownBy(
            () -> notificationService.sendEmail(TEST_TEMPLATE_API_KEY, TEST_EMAIL, parameters, REFERENCE_STRING))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("Template not found");
    }

    @SneakyThrows
    private SendEmailResponse mockSendEmailResponse() {
        notificationClient = mock(NotificationClient.class);
        notificationService = new NotificationService(notificationClient, notificationsProperties);
        doReturn(inputSendEmailResponse).when(notificationClient).sendEmail(TEST_TEMPLATE_API_KEY,
                                                                            TEST_EMAIL, parameters, REFERENCE_STRING
        );
        return notificationService.sendEmail(TEST_TEMPLATE_API_KEY,
                                             TEST_EMAIL, parameters, REFERENCE_STRING
        );
    }

    @Test
    void shouldSendSubmitCaseConfirmationEmail() throws IOException {
        when(notificationService.sendSubmitCaseConfirmationEmail(
            testData.getExpectedDetails(),
            testData.getCaseData(),
            testData.getUserInfo(),
            new byte[0]
        )).thenReturn(testData.getSendEmailResponse());

        assertThat(notificationService.sendSubmitCaseConfirmationEmail(
            testData.getExpectedDetails(),
            testData.getCaseData(),
            testData.getUserInfo(),
            new byte[0]
        ).getNotificationId()).isEqualTo(NOTIFICATION_CONFIRMATION_ID);
    }

    /*
    @Test
    void shouldThrowNotificationExceptionWhenNotAbleToSendEmailBySendSubmitCaseConfirmationEmail()
        throws NotificationClientException {
        var testHashMap = new ConcurrentHashMap<String, String>();
        testHashMap.put("testKey", "testValue");
        when(notificationClient.sendEmail(
            SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID,
            testData.getCaseData().getClaimantType().getClaimantEmailAddress(),
            testHashMap,
            testData.getExpectedDetails().getId().toString())
        ).thenThrow(new NotificationException(new Exception("Error while trying to sending notification to client")));

        byte[] testPdfByteArray = "Any String you want".getBytes();

        NotificationException notificationException = assertThrows(NotificationException.class, () ->
            notificationService.sendSubmitCaseConfirmationEmail(
                testData.getExpectedDetails(),
                testData.getCaseData(),
                testData.getUserInfo(),
                testPdfByteArray));
        assertThat(notificationException.getMessage())
            .isEqualTo("java.lang.Exception: Error while trying to sending notification to client");
    }
*/

    @Test
    void shouldSendSubmitCaseConfirmationEmailInWelsh() throws NotificationClientException {
        inputSendEmailResponse = new SendEmailResponse("{\n"
                                                           + "  \"id\": \"f30b2148-b1a6-4c0d-8a10-50109c96dc2c\",\n"
                                                           + "  \"reference\": \"TEST_EMAIL_ALERT\",\n"
                                                           + "  \"template\": {\n"
                                                           + "    \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n"
                                                           + "    \"version\": \"3\",\n"
                                                           + "    \"uri\": \"TEST\"\n"
                                                           + "  },\n"
                                                           + "  \"content\": {\n"
                                                           + "    \"body\": \"Please click here. https://www.gov.uk/log-in-register-hmrc-online-services/123456722/?lng=cy.\",\n"
                                                           + "    \"subject\": \"ET Test email created\",\n"
                                                           + "    \"from_email\": \"TEST@GMAIL.COM\"\n"
                                                           + "  }\n"
                                                           + "}\n");
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);

        when(notificationClient.sendEmail(
            eq("1234_welsh"),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(inputSendEmailResponse);

        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(EtSyaConstants.WELSH_LANGUAGE);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getExpectedDetails(),
            testData.getCaseData(),
            testData.getUserInfo(),
            new byte[0]
        );
        assertThat(response.getBody()).isEqualTo(
            "Please click here. https://www.gov.uk/log-in-register-hmrc-online-services/123456722/?lng=cy.");
    }

    @Test
    void shouldSendSubmitCaseConfirmationEmailNull() {
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(null);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getExpectedDetails(),
            testData.getCaseData(),
            testData.getUserInfo(),
            new byte[0]
        );
        assertThat(response.getBody()).isEqualTo("Dear test, Please see your detail as 123456789. Regards, ET Team.");
    }


    @Test
    void shouldSuccessfullySendDocUploadErrorEmail()
        throws NotificationClientException {
        SendEmailResponse response =
            new SendEmailResponse("{\n"
                                      + "  \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n"
                                      + "  \"reference\": \"TEST_EMAIL_ALERT\",\n"
                                      + "  \"template\": {\n"
                                      + "    \"id\": \"af0b26b7-17b6-4643-bbdc-e29e11d37b0c\",\n"
                                      + "    \"version\": \"2\",\n"
                                      + "    \"uri\": \"TEST\"\n"
                                      + "  },\n"
                                      + "  \"content\": {\n"
                                      + "    \"body\": \"Dear Service owner, Please see the doc upload error details"
                                      + "  Regards, ET Team.\",\n"
                                      + "    \"subject\": \"ET Test Doc upload error alert email created\",\n"
                                      + "    \"from_email\": \"test.serviceowner@gmail.com\"\n"
                                      + "  }\n"
                                      + "}\n");

        when(notificationClient.sendEmail(any(), any(), any(), any())).thenReturn(response);

        byte[] testEt1PdfByteArray = "test et1 Pdf String".getBytes();
        byte[] testAcasPdfByteArray = "test Acas padf String".getBytes();

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails testCaseDetails = testData.getExpectedDetails();
        SendEmailResponse sendEmailResponse = notificationService
            .sendDocUploadErrorEmail(testCaseDetails, testEt1PdfByteArray, testAcasPdfByteArray);

        assertThat(sendEmailResponse.getFromEmail().isPresent()).isTrue();
        assertThat(sendEmailResponse.getFromEmail()).asString()
            .isEqualTo("Optional[" + EMAIL_TEST_SERVICE_OWNER_GMAIL_COM + "]");
        assertThat(sendEmailResponse.getTemplateId().toString())
            .isEqualTo(DOC_UPLOAD_ERROR_EMAIL_TEMPLATE_ID);
    }

    /*
    @Test
    void shouldThrowNotificationExceptionWhenNotAbleToSendDocUploadErrorEmail()
        throws NotificationClientException {
        byte[] testEt1PdfByteArray = "test et1 Pdf String".getBytes();
        byte[] testAcasPdfByteArray = "test Acas padf String".getBytes();
        var testHashMap = new ConcurrentHashMap<String, String>();
        testHashMap.put("testKey", "testValue");
        when(notificationClient.sendEmail(
            DOC_UPLOAD_ERROR_EMAIL_TEMPLATE_ID,
            testData.getCaseData().getClaimantType().getClaimantEmailAddress(),
            testHashMap,
            testData.getExpectedDetails().getId().toString()
        )).thenThrow(new NotificationException(
            new Exception("Error while trying to send doc upload error notification to service owner")));

        when(notificationsProperties.getSubmitCaseDocUploadErrorEmailTemplateId()).thenReturn(null);
        when(notificationsProperties.getEt1EcmDtsCoreTeamSlackNotificationEmail()).thenReturn(null);

        NotificationException notificationException = assertThrows(NotificationException.class, () ->
            notificationService.sendDocUploadErrorEmail(testData.getExpectedDetails(),
                                                        testEt1PdfByteArray,
                                                        testAcasPdfByteArray));
        assertThat(notificationException.getMessage())
            .isEqualTo("java.lang.Exception: Error while trying to send doc upload"
                       + " error notification to service owner");
    }
*/

}
