package uk.gov.hmcts.reform.et.syaapi.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_STRING;

class NotificationPropertiesTest {

    @Test
    void expectedNotificationPropertiesObject() {
        NotificationsProperties notificationsProperties = new NotificationsProperties();
        notificationsProperties.setGovNotifyApiKey(TEST_STRING);
        notificationsProperties.setSampleEmailTemplateId(TEST_STRING);
        assertThat(TEST_STRING).isEqualTo(notificationsProperties.getGovNotifyApiKey());
        assertThat(TEST_STRING).isEqualTo(notificationsProperties.getSampleEmailTemplateId());
    }
}
