package uk.gov.hmcts.reform.et.syaapi.config.notification;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
public class NotificationsProperties {

    @Value("${notifications.govNotifyApiKey}")
    private String govNotifyApiKey;

    @Value("${notifications.sampleEmailTemplateId}")
    private String sampleEmailTemplateId;
}
