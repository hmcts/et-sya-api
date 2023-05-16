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

    @Value("${notifications.submitCaseDocUploadErrorEmailTemplateId}")
    @NotBlank
    private String submitCaseDocUploadErrorEmailTemplateId;

    @Value("${notifications.et1EcmDtsCoreTeamSlackNotificationEmail}")
    @NotBlank
    private String et1EcmDtsCoreTeamSlackNotificationEmail;

    @Value("${notifications.et1ServiceOwnerNotificationEmail}")
    @NotBlank
    private String et1ServiceOwnerNotificationEmail;

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

    // TSE
    @Value("${notifications.tse.claimantResponseYes}")
    @NotBlank
    private String tseClaimantResponseYesTemplateId;

    @Value("${notifications.tse.claimantResponseNo}")
    @NotBlank
    private String tseClaimantResponseNoTemplateId;

    @Value("${notifications.tse.tribunalResponse}")
    @NotBlank
    private String tseTribunalResponseTemplateId;

    @Value("${notifications.tse.respondentResponse}")
    @NotBlank
    private String tseRespondentResponseTemplateId;

    // PSE
    @Value("${notifications.pse.claimantResponseYes}")
    @NotBlank
    private String pseClaimantResponseYesTemplateId;

    @Value("${notifications.pse.claimantResponseNo}")
    @NotBlank
    private String pseClaimantResponseNoTemplateId;

    @Value("${notifications.pse.tribunalResponse}")
    @NotBlank
    private String pseTribunalResponseTemplateId;

    @Value("${notifications.pse.respondentResponse}")
    @NotBlank
    private String pseRespondentResponseTemplateId;
}
