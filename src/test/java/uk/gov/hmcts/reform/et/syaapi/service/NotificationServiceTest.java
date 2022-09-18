package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.CITIZEN_PORTAL_LINK;

@SuppressWarnings({"PMD.TooManyMethods"})
class NotificationServiceTest {
    private static final String TEST_TEMPLATE_API_KEY = "dummy template id";

    private static final String SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID = "af0b26b7-17b6-4643-bbdc-e296d11e7b0c";

    private static final String REFERENCE_STRING = "TEST_EMAIL_ALERT";

    private static final String TEST_EMAIL = "TEST@GMAIL.COM";

    private static final String SUBMIT_CASE_CONFIRMATION_TEST_EMAIL = "mehmet@tdmehmet.com";
    private static final String SUBMIT_CASE_CONFIRMATION_TITLE = "Mr.";
    private static final String SUBMIT_CASE_CONFIRMATION_LAST_NAME = "Smith";
    private static final String SUBMIT_CASE_CONFIRMATION_CASE_NUMBER = "1234567/22";

    @MockBean
    private NotificationService notificationService;

    private NotificationClient notificationClient;

    private final ConcurrentHashMap<String, String> parameters = new ConcurrentHashMap<>();

    private SendEmailResponse inputSendEmailResponse;

    @BeforeEach
    void before() throws NotificationClientException {
        parameters.put("firstname", "test");
        parameters.put("references", "123456789");
        inputSendEmailResponse = new SendEmailResponse("{\n"
                   + "  \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n"
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
        notificationService = new NotificationService(notificationClient);
        given(notificationClient.sendEmail(anyString(), anyString(), any(), anyString()))
            .willReturn(inputSendEmailResponse);
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
        notificationService = new NotificationService(notificationClient);
        doReturn(inputSendEmailResponse).when(notificationClient).sendEmail(TEST_TEMPLATE_API_KEY,
                                                                            TEST_EMAIL, parameters, REFERENCE_STRING);
        return notificationService.sendEmail(TEST_TEMPLATE_API_KEY,
                                             TEST_EMAIL, parameters, REFERENCE_STRING);
    }

    @Test
    void shouldSendSubmitCaseConfirmationEmail() {
        NotificationsProperties notificationsProperties = new NotificationsProperties();
        notificationsProperties
            .setGovNotifyApiKey("et1live-21beac2b-d979-4771-bbb5-e34b62cba543-6ce0eb4b-98ee-45a1-8b0c-5345f3754423");
        // et1test-21beac2b-d979-4771-bbb5-e34b62cba543-caf93a67-ba77-4b35-8715-1ef73636ab41
        notificationsProperties.setSubmitCaseEmailTemplateId("4f4b378e-238a-46ed-ae1c-26b8038192f0");
        notificationsProperties.setCitizenPortalLink("https://www.gov.uk/log-in-register-hmrc-online-services");
        NotificationClient notificationClient1 = new NotificationClient(notificationsProperties.getGovNotifyApiKey());
        notificationService = new NotificationService(notificationClient1);
        SendEmailResponse sendEmailResponse = notificationService.sendSubmitCaseConfirmationEmail(
            notificationsProperties.getSubmitCaseEmailTemplateId(),
            SUBMIT_CASE_CONFIRMATION_TEST_EMAIL,
            REFERENCE_STRING,
            SUBMIT_CASE_CONFIRMATION_TITLE,
            SUBMIT_CASE_CONFIRMATION_LAST_NAME,
            SUBMIT_CASE_CONFIRMATION_CASE_NUMBER,
            notificationsProperties.getCitizenPortalLink());
        assertThat(sendEmailResponse.getNotificationId()).isNotNull();
    }

    @Test
    void shouldThrowNotificationExceptionWhenNotAbleToSendEmailBySendSubmitCaseConfirmationEmail()
        throws NotificationClientException {
        ConcurrentHashMap<String, String> submitCaseParameters = new ConcurrentHashMap<>();
        submitCaseParameters.put("title", SUBMIT_CASE_CONFIRMATION_TITLE);
        submitCaseParameters.put("lastName", SUBMIT_CASE_CONFIRMATION_LAST_NAME);
        submitCaseParameters.put("caseNumber", SUBMIT_CASE_CONFIRMATION_CASE_NUMBER);
        submitCaseParameters.put("citizenPortalLink", CITIZEN_PORTAL_LINK);
        when(notificationClient.sendEmail(
            SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID,
            SUBMIT_CASE_CONFIRMATION_TEST_EMAIL,
            submitCaseParameters,
            REFERENCE_STRING
        )).thenThrow(new NotificationException(new Exception("Error while trying to sending notification to client")));
        NotificationException notificationException = assertThrows(NotificationException.class, () ->
                     notificationService.sendSubmitCaseConfirmationEmail(
                         SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID,
                         SUBMIT_CASE_CONFIRMATION_TEST_EMAIL,
                         REFERENCE_STRING,
                         SUBMIT_CASE_CONFIRMATION_TITLE,
                         SUBMIT_CASE_CONFIRMATION_LAST_NAME,
                         SUBMIT_CASE_CONFIRMATION_CASE_NUMBER,
                         CITIZEN_PORTAL_LINK));
        assertThat(notificationException.getMessage())
            .isEqualTo("java.lang.Exception: Error while trying to sending notification to client");
    }
}
