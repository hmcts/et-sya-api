package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.service.notify.NotificationClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.tika.utils.StringUtils.isBlank;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.NOT_SET;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_EXUI_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_HEARING_DATE_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationPseService {
    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;
    private final NotificationService notificationService;

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
        if ((NO.equals(copyToOtherParty) && isClaimantPseResponse)
            || copyToOtherParty == null) {
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
                sendPseResponseFromRespondentWithNoCopy(caseData, caseId, respondentIdamId, respondentParameters);
            } else {
                // send email to all the respondents
                String emailTemplate = notificationsProperties.getPseClaimantResponseYesTemplateId();
                notificationService.sendRespondentEmails(caseData, caseId, respondentParameters, emailTemplate);
            }
        }
    }

    private void sendPseResponseFromRespondentWithNoCopy(CaseData caseData, String caseId, String respondentIdamId,
                                                         Map<String, Object> respondentParameters) {
        String emailTemplate = notificationsProperties.getPseClaimantResponseNoTemplateId();

        RespondentSumTypeItem respondent = notificationService.getRespondent(caseData, respondentIdamId);

        String emailAddress = notificationService.getRespondentEmail(respondent);
        if (isBlank(emailAddress)) {
            log.info("Respondent does not have an email address associated with their account");
            return;
        }

        String linkToCase = notificationService.getRespondentPortalLink(
            caseId, respondent.getId(), notificationService.isWelshLanguage(respondent));
        respondentParameters.put(SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY, linkToCase);

        notificationService.sendEmailToRespondent(emailAddress, emailTemplate, respondentParameters, caseId);
    }
}
