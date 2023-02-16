package uk.gov.hmcts.reform.et.syaapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.service.notify.NotificationClient;

/**
 * Initialize configurations for the notification controller.
 */
@Configuration
public class NotificationsConfiguration {

    /**
     * Creates an entirely new {@link NotificationsProperties} object.
     * @return the new properties object
     */
    @Bean
    public NotificationsProperties notificationsProperties() {
        return new NotificationsProperties();
    }

    /**
     * Creates a new {@link NotificationClient} initialised with the api key.
     * @param notificationsProperties current properties in {@link NotificationsProperties} format
     * @return a new initialised {@link NotificationClient} object
     */
    @Bean
    public NotificationClient notificationClient(NotificationsProperties notificationsProperties) {
        return new NotificationClient(notificationsProperties.getGovNotifyApiKey());
    }

}
