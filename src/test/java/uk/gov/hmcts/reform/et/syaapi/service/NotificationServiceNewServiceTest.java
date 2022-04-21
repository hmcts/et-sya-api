package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.SendEmailResponse;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationServiceNewServiceTest {

    private final String GOV_NOTIFY_API_KEY = "et_test_api_key-002d2170-e381-4545-8251-5e87dab724e7-190d8b02-2bb8-4fc9-a471-5486b77782c0";

    private final String SAMPLE_TEMPLATE_ID = "8835039a-3544-439b-a3da-882490d959eb";

    private final String REFERENCE = "TEST_EMAIL_Alert";

    private NotificationService notificationService;
    private NotificationClient notificationClient;

    @BeforeEach
    void setUp() {
        notificationClient = new NotificationClient(GOV_NOTIFY_API_KEY);
        notificationService = new NotificationService(notificationClient);
    }

    @Test
    void shouldSendEmailWithProperties() {
        String targetEmail = "vinoth.kumarsrinivasan@HMCTS.NET";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("references", "1234567890");
        parameters.put("firstname", "Vinothkumar");

        SendEmailResponse sendEmailResponse = notificationService.sendMail(
            SAMPLE_TEMPLATE_ID, targetEmail, parameters, REFERENCE);
        assertThat(sendEmailResponse.getReference().get()).isEqualTo(REFERENCE);
    }

    @Test
    void shouldFailSendingEmail() {
        String targetEmail = "vinoth1.kumarsrinivasan@HMCTS.NET";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("references", "1234567890");
        parameters.put("firstname", "Vinothkumar");
        String reference = "TEST EMAIL Alert";
        assertThatThrownBy( () -> {
                notificationService.sendMail(SAMPLE_TEMPLATE_ID, targetEmail, parameters, reference);
            }).isInstanceOf(NotificationException.class)
            .hasMessageContaining("send to this recipient using a team-only API key");
    }
}
