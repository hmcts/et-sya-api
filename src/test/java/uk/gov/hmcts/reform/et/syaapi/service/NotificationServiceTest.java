package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ecm.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.SendEmailResponse;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceTest {

    private NotificationService notificationService;
    private NotificationClient notificationClient;

    @BeforeEach
    void setUp() {
        notificationClient = new NotificationClient(
            "et_test_api_key-002d2170-e381-4545-8251-5e87dab724e7-190d8b02-2bb8-4fc9-a471-5486b77782c0");
        notificationService = new NotificationService(notificationClient);
    }

    @Test
    void shouldSendEmail() {
        String templateId = "8835039a-3544-439b-a3da-882490d959eb";
        String targetEmail = "vinoth.kumarsrinivasan@HMCTS.NET";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("phone_number", "1234567890");
        String reference = "TEST EMAIL Alert";
        SendEmailResponse sendEmailResponse = notificationService.sendMail(templateId, targetEmail, parameters, reference);
        assertThat(sendEmailResponse.getReference().get()).isEqualTo(reference);
    }
}
