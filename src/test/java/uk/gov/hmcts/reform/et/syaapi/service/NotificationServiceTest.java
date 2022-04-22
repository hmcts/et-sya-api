package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.SendEmailResponse;

import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationServiceTest {
    private static final String GOV_NOTIFY_API_KEY =
        "et_test_api_key-002d2170-e381-4545-8251-5e87dab724e7-190d8b02-2bb8-4fc9-a471-5486b77782c0";

    private static final String SAMPLE_TEMPLATE_API_KEY = "8835039a-3544-439b-a3da-882490d959eb";

    private static final String REFERENCE_STRING = "TEST_EMAIL_Alert";

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        NotificationClient notificationClient = new NotificationClient(GOV_NOTIFY_API_KEY);
        notificationService = new NotificationService(notificationClient);
    }

    @Test
    void shouldSendEmailWithProperties() {
        String targetEmail = "vinoth.kumarsrinivasan@HMCTS.NET";
        ConcurrentHashMap<String, String> parameters = new ConcurrentHashMap<>();
        parameters.put("firstname", "test");
        parameters.put("references", "123456789");
        SendEmailResponse sendEmailResponse = notificationService.sendMail(
            SAMPLE_TEMPLATE_API_KEY, targetEmail, parameters, REFERENCE_STRING);
        assertThat(sendEmailResponse.getReference().get()).isEqualTo(REFERENCE_STRING);
    }

    @Test
    void shouldFailSendingEmail() {
        String targetEmail = "vinoth1.kumarsrinivasan@HMCTS.NET";
        ConcurrentHashMap<String, String> parameters = new ConcurrentHashMap<>();
        parameters.put("firstname", "test");
        parameters.put("references", "123456789");
        assertThatThrownBy(() -> {
            notificationService.sendMail(SAMPLE_TEMPLATE_API_KEY, targetEmail, parameters, REFERENCE_STRING);
        }).isInstanceOf(NotificationException.class)
        .hasMessageContaining("send to this recipient using a team-only API key");
    }
}
