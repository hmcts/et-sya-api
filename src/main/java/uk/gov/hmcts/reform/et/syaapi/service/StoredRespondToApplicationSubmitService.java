package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToApplicationRequest;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper.WAITING_FOR_TRIBUNAL;

@RequiredArgsConstructor
@Service
@Slf4j
public class StoredRespondToApplicationSubmitService {

    private final CaseService caseService;
    private final NotificationService notificationService;
    private final CaseDetailsConverter caseDetailsConverter;

    private static final String APP_ID_INCORRECT = "Application id provided is incorrect";
    private static final String RESPOND_ID_INCORRECT = "Respond id provided is incorrect";

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
                .applicationId(request.getApplicationId())
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
}
