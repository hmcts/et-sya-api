package uk.gov.hmcts.reform.et.syaapi.config.notification;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Validated
@Data
public class NotificationsProperties {
    @NotEmpty
    private String govNotifyApiKey;

    @NotEmpty
    private String sampleEmailTemplate;
}
