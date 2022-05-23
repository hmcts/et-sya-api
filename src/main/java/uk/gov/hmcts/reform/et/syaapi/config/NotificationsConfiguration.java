package uk.gov.hmcts.reform.et.syaapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.service.notify.NotificationClient;

@Configuration
public class NotificationsConfiguration {

    @Bean
    public NotificationsProperties notificationsProperties() {
        return new NotificationsProperties();
    }

    @Bean
    public NotificationClient notificationClient(NotificationsProperties notificationsProperties) {
        return new NotificationClient(notificationsProperties.getGovNotifyApiKey());
    }

}
