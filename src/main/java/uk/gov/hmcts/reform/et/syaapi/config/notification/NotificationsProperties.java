package uk.gov.hmcts.reform.et.syaapi.config.notification;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Validated
@Data
public class NotificationsProperties {

    @Value("${notifications.govNotifyApiKey}")
    private String govNotifyApiKey;

    @Value("${notifications.sampleEmailTemplateId}")
    private String sampleEmailTemplateId;
}
