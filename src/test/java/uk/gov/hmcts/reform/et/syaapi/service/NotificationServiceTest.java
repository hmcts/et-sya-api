package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

@SuppressWarnings({"PMD.TooManyMethods"})
class NotificationServiceTest {
    private static final String TEST_GOV_NOTIFY_API_KEY =
        "test_test_key-002d2170-e381-4545-8251-5e87dab724e7-190d8b02-2bb8-4fc9-a471-5486b77782c0";
    private static final String TEST_TEMPLATE_API_KEY = "9230039a-3544-439b-a3da-882490d923eb";
    private static final String REFERENCE_STRING = "TEST_EMAIL_ALERT";

    private static final String TEST_EMAIL = "TEST@GMAIL.COM";

    @MockBean
    private NotificationService notificationService;

    @Mock
    private NotificationClient notificationClient;

    private final ConcurrentHashMap<String, String> parameters = new ConcurrentHashMap<>();

    private SendEmailResponse inputSendEmailResponse;

    @BeforeEach
    void before() {
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
    }

    @SneakyThrows
    @Test
    void shouldSendEmailByMockingResponse() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
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
        assertThat(sendEmailResponse.getFromEmail()).isEqualTo(Optional.ofNullable(TEST_EMAIL));
    }

    @SneakyThrows
    @Test
    void shouldRetrieveNotificationIdCorrectly() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
        assertThat(sendEmailResponse.getNotificationId()).isEqualTo(
            UUID.fromString("8835039a-3544-439b-a3da-882490d959eb"));
    }


    @Test
    void shouldReturnInValidEmailId() {
        initializeClientService();
        assertThatThrownBy(() -> {
            notificationService.sendEMail(TEST_TEMPLATE_API_KEY, null, parameters, REFERENCE_STRING);
        }).isInstanceOf(NotificationException.class)
            .hasMessageContaining("email_address is a required property");
    }


    @Test
    void shouldReturnTemplateIdRequired() {
        initializeClientService();
        assertThatThrownBy(() -> {
            notificationService.sendEMail(null, TEST_EMAIL, parameters, REFERENCE_STRING);
        }).isInstanceOf(NotificationException.class)
            .hasMessageContaining("template_id is a required property");
    }

    @Test
    void shouldReturnTemplateIdNotFound() {
        initializeClientService();

        assertThatThrownBy(() -> {
            notificationService.sendEMail(TEST_TEMPLATE_API_KEY, TEST_EMAIL, parameters, REFERENCE_STRING);
        }).isInstanceOf(NotificationException.class)
            .hasMessageContaining("Template not found");
    }

    private void initializeClientService() {
        notificationClient = new NotificationClient(TEST_GOV_NOTIFY_API_KEY);
        notificationService = new NotificationService(notificationClient);
    }

    @SneakyThrows
    private SendEmailResponse mockSendEmailResponse() {
        notificationClient = Mockito.mock(NotificationClient.class);
        notificationService = new NotificationService(notificationClient);

        doReturn(inputSendEmailResponse).when(notificationClient).sendEmail(TEST_TEMPLATE_API_KEY,
                                                                            TEST_EMAIL, parameters, REFERENCE_STRING);
        return notificationService.sendEMail(TEST_TEMPLATE_API_KEY,
                                             TEST_EMAIL, parameters, REFERENCE_STRING);
    }
}
