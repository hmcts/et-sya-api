package uk.gov.hmcts.reform.et.syaapi.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_STRING;

class NotificationPropertiesTest {

    @Test
    void expectedNotificationPropertiesObject() {
        NotificationsProperties notificationsProperties = new NotificationsProperties();
        notificationsProperties.setGovNotifyApiKey(TEST_STRING);
        assertThat(notificationsProperties.getGovNotifyApiKey()).isEqualTo(TEST_STRING);

    }
}
