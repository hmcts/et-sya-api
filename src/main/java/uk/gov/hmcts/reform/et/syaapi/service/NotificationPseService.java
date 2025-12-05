package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.et.syaapi.service.NotificationService.CoreEmailDetails;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.tika.utils.StringUtils.isBlank;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.NOT_SET;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_EXUI_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_HEARING_DATE_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_SHORTTEXT_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE_PARAM_WITHOUT_FWDSLASH;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationPseService {
    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;
    private final NotificationService notificationService;

    private static final String NO_CLAIMANT_EMAIL_FOUND =
        "No claimant email found - Application response acknowledgment not being sent";
    private static final String NO_RESPONDENT_EMAIL_ADDRESS_ASSOCIATED =
        "Respondent does not have an email address associated with their account";

    /**
     * Sends response notification email to tribunal.
     * @param caseData caseData
     * @param caseId case id
     */
    void sendResponseNotificationEmailToTribunal(CaseData caseData, String caseId) {
        Map<String, Object> tribunalParameters = new ConcurrentHashMap<>();
        NotificationsHelper.addCommonParameters(tribunalParameters, caseData, caseId);

        tribunalParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET)
        );

        tribunalParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            notificationsProperties.getExuiCaseDetailsLink() + caseId
        );

        notificationService.sendTribunalEmail(
            caseData,
            caseId,
            tribunalParameters,
            notificationsProperties.getPseTribunalResponseTemplateId()
        );
    }

    /**
     * Sends response notification email to respondent(s).
     * @param caseData caseData
     * @param caseId case id
     * @param copyToOtherParty copy to other party
     * @param isClaimantPseResponse if the response is from claimant
     * @param respondentIdamId respondent idam id
     */
    void sendResponseNotificationEmailToRespondent(
        CaseData caseData,
        String caseId,
        String copyToOtherParty,
        boolean isClaimantPseResponse,
        String respondentIdamId
    ) {
        // don't send email to respondents if this is a claimant PSE response and they opted out
        if (isClaimantPseResponse && (NO.equals(copyToOtherParty) || copyToOtherParty == null)) {
            log.info("Acknowledgement email not sent to respondents");
            return;
        }

        Map<String, Object> respondentParameters = new ConcurrentHashMap<>();
        NotificationsHelper.addCommonParameters(respondentParameters, caseData, caseId);
        respondentParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET)
        );

        if (isClaimantPseResponse) {
            String emailTemplate = notificationsProperties.getPseRespondentResponseTemplateId();
            notificationService.sendRespondentEmails(caseData, caseId, respondentParameters, emailTemplate);
        } else {
            // respondent PSE response
            if (NO.equals(copyToOtherParty)) {
                // only the current respondent gets the email
                String emailTemplate = notificationsProperties.getPseClaimantResponseNoTemplateId();
                RespondentSumTypeItem respondent = getRespondent(caseData, respondentIdamId);
                sendEmailToSingleCitizenRespondent(caseId, respondent, respondentParameters, emailTemplate);
            } else {
                // send email to all the respondents
                caseData.getRespondentCollection()
                    .forEach(resp -> {
                        sendEmailWhenYesCopyToEachRespondent(
                            caseData, caseId, respondentIdamId, respondentParameters, resp);
                    });
            }
        }
    }

    private RespondentSumTypeItem getRespondent(CaseData caseData, String userIdamId) {
        return caseData.getRespondentCollection().stream()
            .filter(r -> userIdamId.equals(r.getValue().getIdamId()))
            .findFirst()
            .orElse(null);
    }

    private void sendEmailToSingleCitizenRespondent(
        String caseId,
        RespondentSumTypeItem respondent,
        Map<String, Object> respondentParameters,
        String emailTemplate
    ) {
        String emailAddress = getRespondentEmail(respondent);
        if (isBlank(emailAddress)) {
            log.info(NO_RESPONDENT_EMAIL_ADDRESS_ASSOCIATED);
            return;
        }

        boolean isWelsh = notificationService.isWelshLanguage(respondent);
        String linkToCase = notificationService.getRespondentPortalLink(caseId, respondent.getId(), isWelsh);
        respondentParameters.put(SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY, linkToCase);

        notificationService.sendEmailToRespondent(emailAddress, emailTemplate, respondentParameters, caseId);
    }

    private String getRespondentEmail(RespondentSumTypeItem respondentSumTypeItem) {
        if (respondentSumTypeItem == null || respondentSumTypeItem.getValue() == null) {
            return null;
        }

        RespondentSumType respondent = respondentSumTypeItem.getValue();

        String responseEmail = respondent.getResponseRespondentEmail();
        if (StringUtils.isNotBlank(responseEmail)) {
            return responseEmail;
        }

        String respondentEmail = respondent.getRespondentEmail();
        if (StringUtils.isNotBlank(respondentEmail)) {
            return respondentEmail;
        }

        return null;
    }

    private void sendEmailWhenYesCopyToEachRespondent(
        CaseData caseData,
        String caseId,
        String respondentIdamId,
        Map<String, Object> respondentParameters,
        RespondentSumTypeItem resp
    ) {
        // respondent is current citizen user who submitted the response
        if (!isNullOrEmpty(respondentIdamId) && respondentIdamId.equals(resp.getValue().getIdamId())) {
            String emailToCurrentUserTemplate = notificationsProperties.getPseRespondentResponseTemplateId();
            sendEmailToSingleCitizenRespondent(
                caseId,
                resp,
                respondentParameters,
                emailToCurrentUserTemplate
            );
            return;
        }

        // other respondents
        // check if respondent is represented
        RepresentedTypeR representative = NotificationsHelper.getRespondentRepresentative(caseData, resp.getValue());
        boolean isRepresented = representative != null;

        // get email to send to
        String emailToSend = getEmailToSend(isRepresented, representative, resp);
        if (isNullOrEmpty(emailToSend)) {
            log.info("Respondent does not not have an email address associated with their account");
            return;
        }

        // get link to case
        boolean isWelsh = notificationService.isWelshLanguage(resp);
        String linkToCase = isRepresented
            ? notificationService.getRespondentRepPortalLink(caseId)
            : notificationService.getRespondentPortalLink(caseId, resp.getId(), isWelsh);
        respondentParameters.put(SEND_EMAIL_PARAMS_EXUI_LINK_KEY, linkToCase);

        // send email to other respondent
        String emailToOtherRespondentTemplate = notificationsProperties.getPseClaimantResponseYesTemplateId();
        notificationService.sendEmailToRespondent(
            emailToSend,
            emailToOtherRespondentTemplate,
            respondentParameters,
            caseId
        );
    }

    private String getEmailToSend(boolean isRepresented, RepresentedTypeR representative, RespondentSumTypeItem resp) {
        if (isRepresented) {
            // get legal rep email if respondent is represented
            return StringUtils.isNotBlank(representative.getRepresentativeEmailAddress())
                ? representative.getRepresentativeEmailAddress()
                : null;
        } else {
            // if not represented, get respondent email if online
            return isNullOrEmpty(resp.getValue().getIdamId())
                ? null
                : getRespondentEmail(resp);
        }
    }

    /**
     * Sends response notification email to claimant.
     * @param caseData caseData
     * @param caseId case id
     * @param copyToOtherParty copy to other party
     * @param isClaimantPseResponse if the response is from claimant
     */
    void sendResponseNotificationEmailToClaimant(
        CaseData caseData,
        String caseId,
        String copyToOtherParty,
        boolean isClaimantPseResponse
    ) {
        // don't send email to claimant if this is a respondent PSE response and they opted out
        if (!isClaimantPseResponse && (NO.equals(copyToOtherParty) || copyToOtherParty == null)) {
            log.info("Acknowledgement email not sent to claimants");
            return;
        }

        String claimantEmail = getClaimantEmail(caseData, isClaimantPseResponse);
        if (isBlank(claimantEmail)) {
            log.info("Acknowledgement email not sent to claimants");
            return;
        }

        String emailToClaimantTemplate = getEmailToClaimantTemplate(isClaimantPseResponse, copyToOtherParty);

        Map<String, Object> claimantParameters = new ConcurrentHashMap<>();
        NotificationsHelper.addCommonParameters(claimantParameters, caseData, caseId);

        claimantParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET)
        );
        claimantParameters.put(
            SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY,
            notificationsProperties.getCitizenPortalLink() + caseId
        );
        claimantParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            notificationsProperties.getCitizenPortalLink() + caseId
        );

        try {
            notificationClient.sendEmail(
                emailToClaimantTemplate,
                claimantEmail,
                claimantParameters,
                caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }

    private static String getClaimantEmail(CaseData caseData, boolean isClaimantPseResponse) {
        if (isClaimantPseResponse && caseData.getClaimantType() != null) {
            return caseData.getClaimantType().getClaimantEmailAddress();
        } else {
            return NotificationsHelper.isRepresentedClaimantWithMyHmctsCase(caseData)
                ? caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress()
                : caseData.getClaimantType().getClaimantEmailAddress();
        }
    }

    private String getEmailToClaimantTemplate(boolean isClaimantPseResponse, String copyToOtherParty) {
        if (isClaimantPseResponse) {
            return NO.equals(copyToOtherParty)
                ? notificationsProperties.getPseClaimantResponseNoTemplateId()
                : notificationsProperties.getPseClaimantResponseYesTemplateId();
        } else {
            return notificationsProperties.getPseRespondentResponseTemplateId();
        }
    }

    /**
     * Sends stored notification email to claimant.
     * @param details CoreEmailDetails
     * @param shortText short text
     */
    void sendNotificationStoredEmailToClaimant(CoreEmailDetails details, String shortText) {
        String emailAddress = details.caseData().getClaimantType().getClaimantEmailAddress();
        if (isBlank(emailAddress)) {
            log.info(NO_CLAIMANT_EMAIL_FOUND);
            return;
        }

        sendNotificationStoredEmail(
            notificationsProperties.getClaimantTseEmailStoredTemplateId(),
            details,
            shortText,
            emailAddress,
            notificationsProperties.getCitizenPortalLink() + details.caseId()
        );
    }

    /**
     * Sends stored notification email to respondent.
     * @param details CoreEmailDetails
     * @param shortText short text
     * @param respondentIdamId respondent idam id
     */
    void sendNotificationStoredEmailToRespondent(CoreEmailDetails details, String shortText, String respondentIdamId) {
        RespondentSumTypeItem respondent = getRespondent(details.caseData(), respondentIdamId);

        String emailAddress = getRespondentEmail(respondent);
        if (isBlank(emailAddress)) {
            log.info(NO_RESPONDENT_EMAIL_ADDRESS_ASSOCIATED);
            return;
        }

        String portalLink = notificationsProperties.getRespondentPortalLink()
            + details.caseId() + "/" + respondent.getId()
            + (notificationService.isWelshLanguage(respondent) ? WELSH_LANGUAGE_PARAM_WITHOUT_FWDSLASH : "");

        sendNotificationStoredEmail(
            notificationsProperties.getClaimantTseEmailStoredTemplateId(),
            details,
            shortText,
            emailAddress,
            portalLink
        );
    }

    private void sendNotificationStoredEmail(String emailTemplate, CoreEmailDetails details,
                                     String shortText, String emailAddress, String portalLinkKey) {
        Map<String, Object> claimantParameters = new ConcurrentHashMap<>();

        NotificationsHelper.addCommonParameters(
            claimantParameters,
            details.claimant(),
            details.respondentNames(),
            details.caseId(),
            details.caseNumber()
        );
        claimantParameters.put(SEND_EMAIL_PARAMS_SHORTTEXT_KEY, defaultIfEmpty(shortText, ""));
        claimantParameters.put(SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY, portalLinkKey);

        try {
            notificationClient.sendEmail(
                emailTemplate,
                emailAddress,
                claimantParameters,
                details.caseId()
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }
}
