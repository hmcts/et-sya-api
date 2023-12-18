package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationAddResponseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationStateUpdateRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VIEWED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.CLAIMANT_CORRESPONDENCE_DOCUMENT;
import static uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper.CLAIMANT;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendNotificationService {

    private final CaseService caseService;
    private final CaseDocumentService caseDocumentService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationService notificationService;

    public CaseDetails updateSendNotificationState(String authorization, SendNotificationStateUpdateRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.UPDATE_NOTIFICATION_STATE
        );

        CaseData caseData = EmployeeObjectMapper
            .mapRequestCaseDataToCaseData(startEventResponse.getCaseDetails().getData());

        List<SendNotificationTypeItem> notifications = caseData.getSendNotificationCollection();
        for (SendNotificationTypeItem item : notifications) {
            if (item.getId().equals(request.getSendNotificationId())) {
                if (item.getValue().getNotificationState().equals(NOT_VIEWED_YET)) {
                    item.getValue().setNotificationState(VIEWED);
                }
                setResponsesAsViewed(item.getValue().getRespondNotificationTypeCollection());
                setNonTribunalResponsesAsViewed(item.getValue().getRespondCollection());
                break;
            }
        }

        CaseDataContent content = caseDetailsConverter.caseDataContent(startEventResponse, caseData);

        return caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            content,
            request.getCaseTypeId()
        );
    }

    private void setResponsesAsViewed(List<GenericTypeItem<RespondNotificationType>> responses) {
        if (CollectionUtils.isEmpty(responses)) {
            return;
        }

        for (GenericTypeItem<RespondNotificationType> item : responses) {
            item.getValue().setState(VIEWED);
        }
    }

    private void setNonTribunalResponsesAsViewed(List<PseResponseTypeItem> responses) {
        if (CollectionUtils.isEmpty(responses)) {
            return;
        }

        for (PseResponseTypeItem item : responses) {
            item.getValue().setResponseState(VIEWED);
        }
    }

    /**
     * Adds a pseResponse to a notification.
     *
     * @param authorization - authorization
     * @param request       - request containing the response, and the notification details
     * @return the associated {@link CaseDetails}
     */
    public CaseDetails addResponseSendNotification(String authorization, SendNotificationAddResponseRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.UPDATE_NOTIFICATION_RESPONSE
        );

        CaseData caseData = EmployeeObjectMapper
            .mapRequestCaseDataToCaseData(startEventResponse.getCaseDetails().getData());
        var sendNotificationTypeItem =
            caseData.getSendNotificationCollection()
                .stream()
                .filter(notification -> notification.getId().equals(request.getSendNotificationId()))
                .findFirst();
        if (sendNotificationTypeItem.isEmpty()) {
            throw new IllegalArgumentException("SendNotification Id is incorrect");
        }

        SendNotificationType sendNotificationType = sendNotificationTypeItem.get().getValue();

        var pseRespondCollection = sendNotificationType.getRespondCollection();
        if (CollectionUtils.isEmpty(pseRespondCollection)) {
            sendNotificationTypeItem.get().getValue().setRespondCollection(new ArrayList<>());
        }

        PseResponseType pseResponseType = request.getPseResponseType();
        pseResponseType.setDate(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        pseResponseType.setFrom(CLAIMANT);

        if (request.getSupportingMaterialFile() != null) {
            DocumentTypeItem documentTypeItem = caseDocumentService.createDocumentTypeItem(
                CLAIMANT_CORRESPONDENCE_DOCUMENT,
                request.getSupportingMaterialFile()
            );
            var documentTypeItems = new ArrayList<GenericTypeItem<DocumentType>>();
            documentTypeItems.add(documentTypeItem);
            pseResponseType.setSupportingMaterial(documentTypeItems);
            pseResponseType.setHasSupportingMaterial(YES);
        } else {
            pseResponseType.setHasSupportingMaterial(NO);
        }

        PseResponseTypeItem pseResponseTypeItem =
            PseResponseTypeItem.builder().id(UUID.randomUUID().toString())
                .value(pseResponseType)
                .build();

        sendNotificationType.getRespondCollection().add(pseResponseTypeItem);
        sendNotificationType.setSendNotificationResponsesCount(String.valueOf(
            sendNotificationType.getRespondCollection().size()));
        sendNotificationType.setNotificationState(SUBMITTED);
        setResponsesAsRespondedTo(sendNotificationType.getRespondNotificationTypeCollection());

        CaseDataContent content = caseDetailsConverter.caseDataContent(startEventResponse, caseData);
        sendAddResponseSendNotificationEmails(
            caseData,
            request.getCaseId(),
            request.getPseResponseType().getCopyToOtherParty()
        );
        return caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            content,
            request.getCaseTypeId()
        );
    }

    private void sendAddResponseSendNotificationEmails(CaseData caseData,
                                                       String caseId,
                                                       String copyToOtherParty) {


        notificationService.sendResponseNotificationEmailToTribunal(caseData, caseId);
        notificationService.sendResponseNotificationEmailToRespondent(caseData, caseId, copyToOtherParty);
        notificationService.sendResponseNotificationEmailToClaimant(caseData, caseId, copyToOtherParty);


    }

    private void setResponsesAsRespondedTo(List<GenericTypeItem<RespondNotificationType>> responses) {
        if (CollectionUtils.isEmpty(responses)) {
            return;
        }

        for (GenericTypeItem<RespondNotificationType> item : responses) {
            item.getValue().setIsClaimantResponseDue(null);
        }
    }

}
