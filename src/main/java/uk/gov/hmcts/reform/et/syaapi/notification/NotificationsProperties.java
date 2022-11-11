package uk.gov.hmcts.reform.et.syaapi.notification;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;

/**
 * Holds gov-notify api key and templateId details.
 */

@Validated
@Data
public class NotificationsProperties {

    @Value("${notifications.govNotifyApiKey}")
    @NotEmpty
    private String govNotifyApiKey;

    @Value("${notifications.sampleEmailTemplateId}")
    @NotEmpty
    private String sampleEmailTemplateId;

    @Value("${notifications.sampleSubmitCaseEmailTemplateId}")
    @NotEmpty
    private String sampleSubmitCaseEmailTemplateId;

    @Value("${notifications.submitCaseEmailTemplateId}")
    @NotEmpty
    private String submitCaseEmailTemplateId;

    @Value("${notifications.cySubmitCaseEmailTemplateId}")
    @NotBlank
    private String cySubmitCaseEmailTemplateId;

    @Value("${notifications.citizenPortalLink}")
    @NotEmpty
    private String citizenPortalLink;
}
