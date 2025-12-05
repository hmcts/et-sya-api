package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.service.notify.NotificationClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.NOT_SET;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_EXUI_LINK_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_HEARING_DATE_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationPseService {
    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;
    private final NotificationService notificationService;

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
}
