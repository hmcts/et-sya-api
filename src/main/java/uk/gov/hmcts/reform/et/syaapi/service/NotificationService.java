package uk.gov.hmcts.reform.et.syaapi.service;

import com.microsoft.applicationinsights.core.dependencies.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE_PARAM;

/**
 * Holds details for sending email to user(s) provided template been created beforehand.
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;

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
     * @param caseDetails  top level non-modifiable case details
     * @param caseData  user provided data
     * @param userInfo   user details from Idam
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
}
