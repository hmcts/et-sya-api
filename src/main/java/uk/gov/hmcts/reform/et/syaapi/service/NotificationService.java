package uk.gov.hmcts.reform.et.syaapi.service;

import com.microsoft.applicationinsights.core.dependencies.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE_PARAM;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.SHORT_TEXT_MAP;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getRespondentNames;


/**
 * Holds details for sending email to user(s) provided template been created beforehand.
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;
    private final String[] typeA =
        {"strike", "amend", "non-compliance", "other", "postpone", "vary", "respondent", "publicity"};
    private final String[] typeB = {"withdraw", "change-details", "reconsider-decision", "reconsider-judgement"};
    private static final String TYPE_C = "witness";
    private static final String DONT_SEND_COPY = "No";

    /**
     * Given a caseId, initialization of trigger event to start and submit update for case.
     *
     * @param templateId  - pass gov notify template id for each use case
     * @param targetEmail - recipient target email id
     * @param parameters  - map of strings to add this to the template
     * @param reference   - reference string for email template
     * @return response from notification api
     */
    public SendEmailResponse sendEmail(
        String templateId, String targetEmail, Map<String, String> parameters, String reference) {
        SendEmailResponse sendEmailResponse;
        try {
            sendEmailResponse = notificationClient.sendEmail(templateId, targetEmail, parameters, reference);
        } catch (NotificationClientException ne) {
            log.error("Error while trying to sending notification to client", ne);
            throw new NotificationException(ne);
        }
        return sendEmailResponse;
    }

    /**
     * Format user and case data then send email.
     *
     * @param caseDetails top level non-modifiable case details
     * @param caseData    user provided data
     * @param userInfo    user details from Idam
     * @return Gov notify email format
     */
    public SendEmailResponse sendSubmitCaseConfirmationEmail(CaseDetails caseDetails,
                                                             CaseData caseData,
                                                             UserInfo userInfo) {

        String firstName = Strings.isNullOrEmpty(caseData.getClaimantIndType().getClaimantFirstNames())
            ? userInfo.getGivenName()
            : caseData.getClaimantIndType().getClaimantFirstNames();
        String lastName = Strings.isNullOrEmpty(caseData.getClaimantIndType().getClaimantLastName())
            ? userInfo.getFamilyName()
            : caseData.getClaimantIndType().getClaimantLastName();
        String caseNumber = caseDetails.getId() == null ? "case id not found" : caseDetails.getId().toString();
        String emailTemplateId = notificationsProperties.getSubmitCaseEmailTemplateId();
        String citizenPortalLink = notificationsProperties.getCitizenPortalLink() + "%s";
        if (caseData.getClaimantHearingPreference().getContactLanguage() != null
            && WELSH_LANGUAGE.equals(caseData.getClaimantHearingPreference().getContactLanguage())) {
            emailTemplateId = notificationsProperties.getCySubmitCaseEmailTemplateId();
            citizenPortalLink = citizenPortalLink + WELSH_LANGUAGE_PARAM;
        }

        SendEmailResponse sendEmailResponse;
        try {
            Map<String, String> parameters = new ConcurrentHashMap<>();
            parameters.put("firstName", firstName);
            parameters.put("lastName", lastName);
            parameters.put("caseNumber", caseNumber);
            parameters.put("citizenPortalLink", String.format(citizenPortalLink, caseNumber));
            sendEmailResponse = notificationClient.sendEmail(
                emailTemplateId,
                caseData.getClaimantType().getClaimantEmailAddress(),
                parameters,
                caseNumber
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
        return sendEmailResponse;
    }

    /**
     * Format details of claimant request and retrieve case data, then send email.
     *
     * @param caseDetails existing case details
     * @param claimantApplication application request data
     * @return Gov notify email format
     */
    public SendEmailResponse sendAcknowledgementEmailToClaimant(CaseDetails caseDetails,
                                                                ClaimantTse claimantApplication) {

        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        String claimant = caseData.getClaimantIndType().getClaimantFirstNames() + " "
            + caseData.getClaimantIndType().getClaimantLastName();
        String caseNumber = caseData.getEthosCaseReference();
        String respondentNames = getRespondentNames(caseData);
        String caseId = caseDetails.getId() == null ? "case id not found" : caseDetails.getId().toString();

        SendEmailResponse sendEmailResponse;
        Map<String, String> parameters = new ConcurrentHashMap<>();
        parameters.put("claimant", claimant);
        parameters.put("respondentNames", respondentNames);
        parameters.put("caseId", caseId);
        parameters.put("caseNumber", caseNumber);

        String emailTemplate = notificationsProperties.getClaimantTseEmailTypeCTemplateId();
        if (!TYPE_C.equals(claimantApplication.getContactApplicationType())) {
            String hearingDate = NotificationsHelper.getNearestHearingToReferral(caseData, "Not set");
            parameters.put("hearingDate", hearingDate);
            if (DONT_SEND_COPY.equals(claimantApplication.getCopyToOtherPartyYesOrNo())) {
                String shortText = SHORT_TEXT_MAP.get(claimantApplication.getContactApplicationType());
                parameters.put("shortText", shortText);
                emailTemplate = notificationsProperties.getClaimantTseEmailNoTemplateId();
            } else {
                String shortText = SHORT_TEXT_MAP.get(claimantApplication.getContactApplicationType());
                String abText = "";
                if (Stream.of(typeA).anyMatch(appType -> Objects.equals(
                    appType,
                    claimantApplication.getContactApplicationType()
                ))) {
                    abText = "The other party will be notified that any objections to your "
                        + shortText
                        + " application should be sent to the tribunal as soon as possible, "
                        + "and in any event within 7 days";

                } else if (Stream.of(typeB).anyMatch(appType -> Objects.equals(
                    appType,
                    claimantApplication.getContactApplicationType()
                ))) {
                    abText = "The other party is not expected to respond to this application. "
                        + "However, they have been notified that any objections to your "
                        + shortText
                        + " application should be sent to the tribunal as soon as possible, "
                        + "and in any event within 7 days";
                }
                parameters.put("abText", abText);
                emailTemplate = notificationsProperties.getClaimantTseEmailYesTemplateId();
            }
        }
        try {
            sendEmailResponse = notificationClient.sendEmail(
                emailTemplate,
                caseData.getClaimantType().getClaimantEmailAddress(),
                parameters,
                caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
        return sendEmailResponse;
    }

}
