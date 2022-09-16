package uk.gov.hmcts.reform.et.syaapi.notification;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

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

    @Value("4f4b378e-238a-46ed-ae1c-26b8038192f0")
    @NotEmpty
    private String submitCaseEmailTemplateId;

    @Value("https://www.gov.uk/log-in-register-hmrc-online-services")
    @NotEmpty
    private String citizenPortalLink;
}
