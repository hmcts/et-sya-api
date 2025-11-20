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
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.PseNotificationHelper;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.models.ChangeRespondentNotificationStatusRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationAddResponseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitRespondentPseRespondRequest;

import java.time.LocalDate;
import java.util.ArrayList;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONDENT_CORRESPONDENCE;
import static uk.gov.hmcts.reform.et.syaapi.helper.PseNotificationHelper.getSelectedResponseInPse;
import static uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper.getCurrentDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendNotificationRespondentService {

    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseDocumentService caseDocumentService;
    private final NotificationService notificationService;

    private static final String SEND_NOTIFICATION_ID_INCORRECT = "Notification id provided is incorrect";
    private static final String RESPOND_EMPTY = "Respond collection is empty";
    private static final String RESPOND_ID_INCORRECT = "Respond id provided is incorrect";

    /**
     * Update respondent's notification state.
     *
     * @param authorization - authorization
     * @param request - request with notification's id
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails updateRespondentNotificationStatus(String authorization,
                                                          ChangeRespondentNotificationStatusRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.UPDATE_RESPONDENT_PSE_STATE
        );

        CaseData caseData = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(startEventResponse.getCaseDetails().getData());

        // get sendNotificationTypeItem
        SendNotificationTypeItem itemToModify = PseNotificationHelper.getSelectedNotification(
            caseData.getSendNotificationCollection(),
            request.getNotificationId()
        );
        if (itemToModify == null) {
            throw new IllegalArgumentException(SEND_NOTIFICATION_ID_INCORRECT);
        }

        // update respondent state
        updateRespondentState(itemToModify.getValue(), request.getUserIdamId(), request.getNewStatus());

        return caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            caseDetailsConverter.caseDataContent(startEventResponse, caseData),
            request.getCaseTypeId()
        );
    }

    private void updateRespondentState(SendNotificationType app, String userIdamId, String newState) {
        if (app.getRespondentState() == null) {
            app.setRespondentState(new ArrayList<>());
        }

        app.getRespondentState().stream()
            .filter(status -> status.getValue().getUserIdamId().equals(userIdamId))
            .findFirst()
            .ifPresentOrElse(
                status -> status.getValue().setNotificationState(newState),
                () -> app.getRespondentState()
                    .add(PseNotificationHelper.buildPseStatusTypeItem(userIdamId, newState))
        );
    }

    /**
     * Adds a respondent pseResponse to a notification.
     *
     * @param authorization - authorization
     * @param request       - request containing the response, and the notification details
     * @return the associated {@link CaseDetails}
     */
    public CaseDetails addRespondentResponseNotification(
        String authorization,
        SendNotificationAddResponseRequest request) {
        String caseId = request.getCaseId();
        String caseTypeId = request.getCaseTypeId();

        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            caseId,
            caseTypeId,
            CaseEvent.ADD_RESPONDENT_PSE_RESPONSE
        );

        CaseData caseData = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(startEventResponse.getCaseDetails().getData());

        // get sendNotificationTypeItem
        SendNotificationTypeItem itemToModify = PseNotificationHelper.getSelectedNotification(
            caseData.getSendNotificationCollection(),
            request.getSendNotificationId()
        );
        if (itemToModify == null) {
            throw new IllegalArgumentException(SEND_NOTIFICATION_ID_INCORRECT);
        }
        SendNotificationType sendNotificationType = itemToModify.getValue();

        // add PseResponseTypeItem
        if (CollectionUtils.isEmpty(sendNotificationType.getRespondCollection())) {
            sendNotificationType.setRespondCollection(new ArrayList<>());
        }
        PseResponseType pseResponseType = getPseResponseType(request);
        PseResponseTypeItem pseResponseTypeItem = PseNotificationHelper.getPseResponseTypeItem(pseResponseType);
        sendNotificationType.getRespondCollection().add(pseResponseTypeItem);

        // update responses count
        sendNotificationType.setSendNotificationResponsesCount(
            String.valueOf(sendNotificationType.getRespondCollection().size()));

        // update respondent state
        updateRespondentState(
            itemToModify.getValue(),
            request.getPseResponseType().getFromIdamId(),
            SUBMITTED
        );

        // convert to CaseDataContent
        CaseDataContent content = caseDetailsConverter.caseDataContent(startEventResponse, caseData);

        // send email
        // Send confirmation emails
        sendAddResponseSendNotificationEmails(
            caseData,
            caseId,
            pseResponseTypeItem.getValue().getCopyToOtherParty(),
            request.getPseResponseType().getFromIdamId()
        );

        return caseService.submitUpdate(
            authorization,
            caseId,
            content,
            caseTypeId
        );
    }

    private PseResponseType getPseResponseType(SendNotificationAddResponseRequest request) {
        PseResponseType pseResponseType = request.getPseResponseType();

        pseResponseType.setDate(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        pseResponseType.setDateTime(getCurrentDateTime());
        pseResponseType.setFrom(RESPONDENT_TITLE);

        if (request.getSupportingMaterialFile() != null) {
            pseResponseType.setSupportingMaterial(getSupportingMaterial(request));
            pseResponseType.setHasSupportingMaterial(YES);
        } else {
            pseResponseType.setHasSupportingMaterial(NO);
        }

        return pseResponseType;
    }

    private ArrayList<GenericTypeItem<DocumentType>> getSupportingMaterial(SendNotificationAddResponseRequest request) {
        DocumentTypeItem documentTypeItem = caseDocumentService.createDocumentTypeItem(
            RESPONDENT_CORRESPONDENCE,
            request.getSupportingMaterialFile()
        );
        var documentTypeItems = new ArrayList<GenericTypeItem<DocumentType>>();
        documentTypeItems.add(documentTypeItem);
        return documentTypeItems;
    }

    /**
     * Store a respondent pseResponse to a notification.
     *
     * @param authorization - authorization
     * @param request - request containing the response, and the notification details
     * @return the associated {@link CaseDetails}
     */
    public CaseDetails storeResponseSendNotification(String authorization, SendNotificationAddResponseRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.STORE_RESPONDENT_PSE_RESPONSE
        );

        CaseData caseData = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(startEventResponse.getCaseDetails().getData());

        // get sendNotificationTypeItem
        SendNotificationTypeItem itemToModify = PseNotificationHelper.getSelectedNotification(
            caseData.getSendNotificationCollection(),
            request.getSendNotificationId()
        );
        if (itemToModify == null) {
            throw new IllegalArgumentException(SEND_NOTIFICATION_ID_INCORRECT);
        }
        SendNotificationType modifyValue = itemToModify.getValue();

        // store PseResponseTypeItem
        if (CollectionUtils.isEmpty(modifyValue.getRespondentRespondStoredCollection())) {
            modifyValue.setRespondentRespondStoredCollection(new ArrayList<>());
        }
        PseResponseType pseResponseType = getPseResponseType(request);
        PseResponseTypeItem pseResponseTypeItem = PseNotificationHelper.getPseResponseTypeItem(pseResponseType);
        modifyValue.getRespondentRespondStoredCollection().add(pseResponseTypeItem);

        // update respondent state
        updateRespondentState(
            modifyValue,
            request.getPseResponseType().getFromIdamId(),
            STORED
        );

        // convert to CaseDataContent
        CaseDataContent content = caseDetailsConverter.caseDataContent(startEventResponse, caseData);

        // send email
        NotificationService.CoreEmailDetails details =
            notificationService.formatCoreEmailDetails(caseData, request.getCaseId());
        notificationService.sendNotificationStoredEmailToRespondent(
            details,
            request.getPseResponseType().getResponse(),
            request.getPseResponseType().getFromIdamId()
        );

        return caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            content,
            request.getCaseTypeId()
        );
    }

    /**
     * Submit stored respondent response to tribunal notification.
     *
     * @param authorization - authorization
     * @param request - response from the claimant
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails submitRespondToTribunal(String authorization, SubmitRespondentPseRespondRequest request) {
        String caseId = request.getCaseId();
        String caseTypeId = request.getCaseTypeId();

        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            caseId,
            caseTypeId,
            CaseEvent.SUBMIT_RESPONDENT_PSE_RESPONSE
        );

        CaseData caseData = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(startEventResponse.getCaseDetails().getData());

        // get selected SendNotificationType
        SendNotificationTypeItem itemToModify = PseNotificationHelper.getSelectedNotification(
            caseData.getSendNotificationCollection(),
            request.getNotificationId()
        );
        if (itemToModify == null) {
            throw new IllegalArgumentException(SEND_NOTIFICATION_ID_INCORRECT);
        }
        SendNotificationType modifyValue = itemToModify.getValue();

        // get selected PseResponseTypeItem
        PseResponseTypeItem responseToModify = getSelectedResponseInPse(
            modifyValue.getRespondentRespondStoredCollection(), request.getStoredRespondId()
        );
        if (responseToModify == null) {
            throw new IllegalArgumentException(RESPOND_ID_INCORRECT);
        }

        // update date
        responseToModify.getValue().setDate(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        responseToModify.getValue().setDateTime(getCurrentDateTime());

        // add response
        if (CollectionUtils.isEmpty(modifyValue.getRespondCollection())) {
            modifyValue.setRespondCollection(new ArrayList<>());
        }
        modifyValue.getRespondCollection().add(responseToModify);

        // update responses count
        modifyValue.setSendNotificationResponsesCount(String.valueOf(modifyValue.getRespondCollection().size()));

        // update respondent state
        updateRespondentState(
            modifyValue,
            request.getFromIdamId(),
            SUBMITTED
        );

        // Remove Stored Response
        modifyValue.getRespondentRespondStoredCollection()
            .removeIf(item -> item.getId().equals(request.getStoredRespondId()));

        // Send confirmation emails
        sendAddResponseSendNotificationEmails(
            caseData,
            caseId,
            responseToModify.getValue().getCopyToOtherParty(),
            request.getFromIdamId()
        );

        return caseService.submitUpdate(
            authorization, caseId, caseDetailsConverter.caseDataContent(startEventResponse, caseData), caseTypeId);
    }

    private void sendAddResponseSendNotificationEmails(CaseData caseData,
                                                       String caseId,
                                                       String copyToOtherParty,
                                                       String respondentIdamId) {
        notificationService.sendResponseNotificationEmailToTribunal(caseData, caseId);
        notificationService.sendResponseNotificationEmailToRespondent(caseData, caseId, copyToOtherParty,
                                                                      false, respondentIdamId);
        notificationService.sendResponseNotificationEmailToClaimant(caseData, caseId, copyToOtherParty, false);
    }

}
