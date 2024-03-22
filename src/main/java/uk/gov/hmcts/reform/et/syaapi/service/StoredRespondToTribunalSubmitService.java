package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToTribunalRequest;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class StoredRespondToTribunalSubmitService {

    private final CaseService caseService;
    private final NotificationService notificationService;
    private final CaseDetailsConverter caseDetailsConverter;

    private static final String RESPOND_ID_INCORRECT = "Respond id provided is incorrect";
    private static final String SEND_NOTIFICATION_ID_INCORRECT = "SendNotification Id is incorrect";
    private static final String RESPOND_EMPTY = "Respond collection is empty";

    /**
     * Submit stored Claimant Response to Tribunal Send Notification.
     *
     * @param authorization - authorization
     * @param request - response from the claimant
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails submitRespondToTribunal(String authorization, UpdateStoredRespondToTribunalRequest request) {
        String caseId = request.getCaseId();
        String caseTypeId = request.getCaseTypeId();

        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            caseId,
            caseTypeId,
            CaseEvent.CLAIMANT_TSE_RESPOND
        );

        CaseData caseData = EmployeeObjectMapper
            .mapRequestCaseDataToCaseData(startEventResponse.getCaseDetails().getData());

        // get selected SendNotificationType
        var sendNotificationTypeItem =
            caseData.getSendNotificationCollection()
                .stream()
                .filter(notification -> notification.getId().equals(request.getOrderId()))
                .findFirst();
        if (sendNotificationTypeItem.isEmpty()) {
            throw new IllegalArgumentException(SEND_NOTIFICATION_ID_INCORRECT);
        }
        SendNotificationType selectedSendNotificationType = sendNotificationTypeItem.get().getValue();

        // get selected PseResponseTypeItem
        if (CollectionUtils.isEmpty(selectedSendNotificationType.getRespondCollection())) {
            throw new IllegalArgumentException(RESPOND_EMPTY);
        }
        PseResponseTypeItem responseToModify = getResponseInSelectedSendNotification(
            selectedSendNotificationType.getRespondCollection(), request.getRespondId()
        );
        if (responseToModify == null) {
            throw new IllegalArgumentException(RESPOND_ID_INCORRECT);
        }

        // Update response details and SendNotificationType status
        responseToModify.getValue().setDate(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        responseToModify.getValue().setStatus(null);

        // Send confirmation emails
        sendEmailForRespondToTribunal(caseData, caseId, responseToModify.getValue().getResponse());

        return caseService.submitUpdate(
            authorization, caseId, caseDetailsConverter.caseDataContent(startEventResponse, caseData), caseTypeId);
    }

    private static PseResponseTypeItem getResponseInSelectedSendNotification(List<PseResponseTypeItem> responds,
                                                                             String respondId) {
        return responds.stream()
            .filter(a -> a.getId().equals(respondId))
            .findAny()
            .orElse(null);
    }

    private void sendEmailForRespondToTribunal(CaseData caseData, String caseId, String shortText) {
        notificationService.sendResponseNotificationEmailToTribunal(caseData, caseId);
        notificationService.sendSubmitStoredEmailToClaimant(
            notificationService.formatCoreEmailDetails(caseData, caseId), shortText);
    }
}
