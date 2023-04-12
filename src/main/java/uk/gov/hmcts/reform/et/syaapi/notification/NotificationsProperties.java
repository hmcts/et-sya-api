package uk.gov.hmcts.reform.et.syaapi.notification;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

/**
 * Holds gov-notify api key and templateId details.
 */

@Validated
@Data
@SuppressWarnings("PMD.TooManyFields")
public class NotificationsProperties {

    @Value("${notifications.govNotifyApiKey}")
    @NotBlank
    private String govNotifyApiKey;

    @Value("${notifications.sampleEmailTemplateId}")
    @NotBlank
    private String sampleEmailTemplateId;

    @Value("${notifications.sampleSubmitCaseEmailTemplateId}")
    @NotBlank
    private String sampleSubmitCaseEmailTemplateId;

    @Value("${notifications.submitCaseEmailTemplateId}")
    @NotBlank
    private String submitCaseEmailTemplateId;

    @Value("${notifications.cySubmitCaseEmailTemplateId}")
    @NotBlank
    private String cySubmitCaseEmailTemplateId;

    @Value("${notifications.citizenPortalLink}")
    @NotBlank
    private String citizenPortalLink;

    @Value("${notifications.exuiCaseDetailsLink}")
    @NotBlank
    private String exuiCaseDetailsLink;

    @Value("${notifications.applicationAcknowledgementYes}")
    @NotBlank
    private String claimantTseEmailYesTemplateId;

    @Value("${notifications.applicationAcknowledgementNo}")
    @NotBlank
    private String claimantTseEmailNoTemplateId;

    @Value("${notifications.applicationAcknowledgementTypeC}")
    @NotBlank
    private String claimantTseEmailTypeCTemplateId;

    @Value("${notifications.respondentCopyTypeA}")
    @NotBlank
    private String respondentTseEmailTypeATemplateId;

    @Value("${notifications.respondentCopyTypeB}")
    @NotBlank
    private String respondentTseEmailTypeBTemplateId;

    @Value("${notifications.tribunalAcknowledgement}")
    @NotBlank
    private String tribunalAcknowledgementTemplateId;

    @Value("${notifications.tribunalResponse}")
    @NotBlank
    private String tribunalResponseTemplateId;

    @Value("${notifications.claimantResponseNo}")
    @NotBlank
    private String claimantResponseNoTemplateId;

    @Value("${notifications.claimantResponseYes}")
    @NotBlank
    private String claimantResponseYesTemplateId;

    @Value("${notifications.respondentResponse}")
    @NotBlank
    private String respondentResponseTemplateId;

}
