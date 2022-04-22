package uk.gov.hmcts.reform.et.syaapi.notification;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.et.syaapi.config.notification.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationPropertiesTest {

    private static final String TEST_STRING = "TEST";

    @Test
    void expectedNotificationPropertiesObject() {
        NotificationsProperties notificationsProperties = new NotificationsProperties();
        notificationsProperties.setGovNotifyApiKey(TEST_STRING);
        notificationsProperties.setSampleEmailTemplateId(TEST_STRING);
        assertThat(TEST_STRING).isEqualTo(notificationsProperties.getGovNotifyApiKey());
        assertThat(TEST_STRING).isEqualTo(notificationsProperties.getSampleEmailTemplateId());
    }
}
