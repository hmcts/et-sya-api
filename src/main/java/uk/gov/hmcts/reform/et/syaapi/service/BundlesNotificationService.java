package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_EXUI_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_HEARING_DATE_KEY;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.addCommonParameters;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getRespondentNames;
import static uk.gov.hmcts.reform.et.syaapi.service.NotificationService.NOT_SET;

@Service
@Slf4j
@RequiredArgsConstructor
public class BundlesNotificationService {

    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;
    private static final String CONCAT2STRINGS = "%s%s";

    public void sendBundlesEmailToRespondent(CaseData caseData,
                                             String caseId) {
        ClaimantIndType claimantIndType = caseData.getClaimantIndType();
        String claimant = claimantIndType.getClaimantFirstNames() + " " + claimantIndType.getClaimantLastName();
        String caseNumber = caseData.getEthosCaseReference();
        String respondentNames = getRespondentNames(caseData);
        String hearingDate = NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET);

        sendClaimantSubmittedBundleNotificationEmailToRespondent(
            caseData,
            claimant,
            caseNumber,
            respondentNames,
            hearingDate,
            caseId
        );
    }

    private void sendClaimantSubmittedBundleNotificationEmailToRespondent(
        CaseData caseData,
        String claimant,
        String caseNumber,
        String respondentNames,
        String hearingDate,
        String caseId
    ) {
        Map<String, Object> respondentParameters = new ConcurrentHashMap<>();

        addCommonParameters(
            respondentParameters,
            claimant,
            respondentNames,
            caseId,
            caseNumber
        );
        respondentParameters.put(
            SEND_EMAIL_PARAMS_HEARING_DATE_KEY,
            hearingDate
        );
        respondentParameters.put(
            SEND_EMAIL_PARAMS_EXUI_LINK_KEY,
            String.format(CONCAT2STRINGS, notificationsProperties.getExuiCaseDetailsLink(), caseId)
        );

        String emailToRespondentTemplate
            = notificationsProperties.getBundlesClaimantSubmittedRespondentNotificationTemplateId();

        try {
            notificationClient.sendEmail(
                emailToRespondentTemplate,
                caseData.getTribunalCorrespondenceEmail(),
                respondentParameters,
                caseId
            );
        } catch (NotificationClientException ne) {
            throw new NotificationException(ne);
        }
    }
}
