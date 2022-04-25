package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.SendEmailResponse;

import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    private static final String GOV_NOTIFY_API_KEY =
        "et_test_api_key-002d2170-e381-4545-8251-5e87dab724e7-190d8b02-2bb8-4fc9-a471-5486b77782c0";

    private static final String SAMPLE_TEMPLATE_API_KEY = "8835039a-3544-439b-a3da-882490d959eb";

    private static final String REFERENCE_STRING = "TEST_EMAIL_ALERT";

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationClient notificationClient;

    private final ConcurrentHashMap<String, String> parameters = new ConcurrentHashMap<>();

    private SendEmailResponse inputSendEmailResponse;

    @BeforeEach
    void setUp() {
        notificationClient = new NotificationClient(GOV_NOTIFY_API_KEY);
        notificationService = new NotificationService(notificationClient);
        parameters.put("firstname", "test");
        parameters.put("references", "123456789");

        inputSendEmailResponse = new SendEmailResponse("{\n" +
                                                                        "  \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n" +
                                                                        "  \"reference\": \"TEST_EMAIL_ALERT\",\n" +
                                                                        "  \"template\": {\n" +
                                                                        "    \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n" +
                                                                        "    \"version\": \"3\",\n" +
                                                                        "    \"uri\": \"TEST\"\n" +
                                                                        "  },\n" +
                                                                        "  \"content\": {\n" +
                                                                        "    \"body\": \"Dear test, Please see your detail as 123456789. Regards, ET Team.\",\n" +
                                                                        "    \"subject\": \"ET Test email created\",\n" +
                                                                        "    \"fromEmail\": \"test@test.com\"\n" +
                                                                        "  }\n" +
                                                                        "}\n");
    }

    @Test
    void shouldSendEmailWithProperties() {
        String targetEmail = "vinoth.kumarsrinivasan@HMCTS.NET";
        SendEmailResponse sendEmailResponse = notificationService.sendMail(
            SAMPLE_TEMPLATE_API_KEY, targetEmail, parameters, REFERENCE_STRING);
        assertThat(sendEmailResponse.getReference().get()).isEqualTo(REFERENCE_STRING);
    }


    @Test
    void shouldFailSendingEmail() {
        String targetEmail = "vinoth1.kumarsrinivasan@HMCTS.NET";
        assertThatThrownBy(() -> {
            notificationService.sendMail(SAMPLE_TEMPLATE_API_KEY, targetEmail, parameters, REFERENCE_STRING);
        }).isInstanceOf(NotificationException.class)
        .hasMessageContaining("send to this recipient using a team-only API key");
    }

    @Test
    void testInvalidEmailId() {
        assertThatThrownBy(() -> {
            notificationService.sendMail(SAMPLE_TEMPLATE_API_KEY, null, parameters, REFERENCE_STRING);
        }).isInstanceOf(NotificationException.class)
            .hasMessageContaining("email_address is a required property");
    }


    @Test
    void testInvalidTemplateKeyId() {
        String targetEmail = "vinoth.kumarsrinivasan@HMCTS.NET";
        assertThatThrownBy(() -> {
            notificationService.sendMail(null, targetEmail, parameters, REFERENCE_STRING);
        }).isInstanceOf(NotificationException.class)
            .hasMessageContaining("template_id is a required property");
    }

    @Test
    void testInvalideReferenceString() {
        String targetEmail = "vinoth.kumarsrinivasan@HMCTS.NET";
        SendEmailResponse sendEmailResponse = notificationService.sendMail(
            SAMPLE_TEMPLATE_API_KEY, targetEmail, parameters, REFERENCE_STRING);
        assertThat(sendEmailResponse.getReference().get()).isEqualTo(REFERENCE_STRING);
    }
}
