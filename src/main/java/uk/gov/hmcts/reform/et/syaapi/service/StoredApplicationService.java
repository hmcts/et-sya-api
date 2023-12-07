package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToTribunalRequest;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.IN_PROGRESS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper.WAITING_FOR_TRIBUNAL;

@RequiredArgsConstructor
@Service
@Slf4j
public class StoredApplicationService {

    private final CaseService caseService;
    private final NotificationService notificationService;
    private final CaseDetailsConverter caseDetailsConverter;

    private static final String APP_ID_INCORRECT = "Application id provided is incorrect";
    private static final String RESPOND_ID_INCORRECT = "Respond id provided is incorrect";
    private static final String SEND_NOTIFICATION_ID_INCORRECT = "SendNotification Id is incorrect";
    private static final String RESPOND_EMPTY = "Respond collection is empty";

    /**
     * Submits a stored Claimant Application:
     * - Update application state in ExUI from 'Un-submitted' to be 'Open'.
     *
     * @param authorization - authorization
     * @param request - request with application's id
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails submitStoredApplication(String authorization, SubmitStoredApplicationRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.SUBMIT_STORED_CLAIMANT_TSE
        );

        CaseData caseData = EmployeeObjectMapper
            .mapRequestCaseDataToCaseData(startEventResponse.getCaseDetails().getData());

        // Get selected GenericTseApplicationTypeItem
        GenericTseApplicationTypeItem appToModify = TseApplicationHelper.getSelectedApplication(
            caseData.getGenericTseApplicationCollection(),
            request.getApplicationId()
        );
        if (appToModify == null) {
            throw new IllegalArgumentException(APP_ID_INCORRECT);
        }

        // Update GenericTseApplicationTypeItem
        appToModify.getValue().setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        appToModify.getValue().setDueDate(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7));
        appToModify.getValue().setApplicationState(IN_PROGRESS);
        appToModify.getValue().setStatus(OPEN_STATE);

        // Uploading pdf of TSE application
        uploadPdfTseApplication(authorization, caseData, appToModify, request.getCaseTypeId());

        CaseDetails finalCaseDetails =  caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            caseDetailsConverter.caseDataContent(startEventResponse, caseData),
            request.getCaseTypeId()
        );

        // Send confirmation emails
        if (finalCaseDetails != null) {
            sendEmailForApplication(finalCaseDetails, caseData, appToModify);
        }

        return finalCaseDetails;
    }

    private void uploadPdfTseApplication(String authorization, CaseData caseData,
                                         GenericTseApplicationTypeItem appToModify, String caseTypeId) {
        try {
            log.info("Uploading pdf of TSE application");
            caseService.uploadStoredTseCyaAsPdf(authorization, caseData, appToModify, caseTypeId);
        } catch (CaseDocumentException | DocumentGenerationException e) {
            log.error("Couldn't upload pdf of TSE application " + e.getMessage());
        }
    }

    private void sendEmailForApplication(CaseDetails finalCaseDetails, CaseData caseData,
                                         GenericTseApplicationTypeItem appToModify) {
        String caseId = finalCaseDetails.getId().toString();
        NotificationService.CoreEmailDetails details = notificationService.formatCoreEmailDetails(caseData, caseId);
        String shortText = appToModify.getValue().getType();

        notificationService.sendAcknowledgementEmailToTribunal(details, shortText);
        notificationService.sendSubmitStoredEmailToClaimant(details, shortText);
    }

    /**
     * Submit stored claimant response to Tribunal response.
     *
     * @param authorization - authorization
     * @param request - response from the claimant
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails submitRespondToApplication(String authorization,
                                                  UpdateStoredRespondToApplicationRequest request) {
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

        // Get selected GenericTseApplicationTypeItem
        GenericTseApplicationTypeItem appToModify = TseApplicationHelper.getSelectedApplication(
            caseData.getGenericTseApplicationCollection(), request.getApplicationId()
        );
        if (appToModify == null) {
            throw new IllegalArgumentException(APP_ID_INCORRECT);
        }

        // Get selected TseRespondTypeItem
        TseRespondTypeItem responseToModify = TseApplicationHelper.getResponseInSelectedApplication(
            appToModify.getValue().getRespondCollection(), request.getRespondId()
        );
        if (responseToModify == null) {
            throw new IllegalArgumentException(RESPOND_ID_INCORRECT);
        }

        // Update response details and application status
        updateResponseForSubmitStored(responseToModify, appToModify);

        // Update pdf
        createAndAddPdfOfResponse(authorization, request, caseData, appToModify.getValue(),
            responseToModify.getValue());

        // Send confirmation email
        sendEmailForRespondToApplication(caseData, caseId, appToModify, request.isRespondingToRequestOrOrder());

        return caseService.submitUpdate(
            authorization, caseId, caseDetailsConverter.caseDataContent(startEventResponse, caseData), caseTypeId);
    }

    private static void updateResponseForSubmitStored(TseRespondTypeItem responseToModify,
                                                      GenericTseApplicationTypeItem appToModify) {
        TseRespondType tseRespondType = responseToModify.getValue();
        tseRespondType.setDate(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        tseRespondType.setStatus(null);
        appToModify.getValue().setApplicationState(WAITING_FOR_TRIBUNAL);
    }

    private void createAndAddPdfOfResponse(
        String authorization,
        UpdateStoredRespondToApplicationRequest request,
        CaseData caseData,
        GenericTseApplicationType application,
        TseRespondType tseRespond) {
        try {
            log.info("Uploading pdf of claimant response to application");

            RespondToApplicationRequest respondRequest = RespondToApplicationRequest.builder()
                .caseId(request.getCaseId())
                .caseTypeId(request.getCaseTypeId())
                .applicationId("")
                .supportingMaterialFile(
                    tseRespond.getSupportingMaterial() != null
                        ? tseRespond.getSupportingMaterial().get(0).getValue().getUploadedDocument()
                        : null
                )
                .response(tseRespond)
                .isRespondingToRequestOrOrder(request.isRespondingToRequestOrOrder())
                .build();

            caseService.createResponsePdf(
                authorization,
                caseData,
                respondRequest,
                application.getType()
            );
        } catch (CaseDocumentException | DocumentGenerationException e) {
            log.error("Couldn't upload pdf of TSE application " + e.getMessage());
        }
    }

    private void sendEmailForRespondToApplication(CaseData caseData, String caseId,
                                                  GenericTseApplicationTypeItem appToModify,
                                                  boolean isRespondingToRequestOrOrder) {
        NotificationService.CoreEmailDetails details = notificationService.formatCoreEmailDetails(caseData, caseId);
        notificationService.sendResponseEmailToTribunal(
            details, appToModify.getValue().getType(), isRespondingToRequestOrOrder);
        notificationService.sendSubmitStoredEmailToClaimant(details, appToModify.getValue().getType());
    }

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
