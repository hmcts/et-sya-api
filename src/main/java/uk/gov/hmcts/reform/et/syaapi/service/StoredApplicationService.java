package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToApplicationRequest;

import java.time.LocalDate;
import java.util.ArrayList;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.IN_PROGRESS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.getRespondentNames;
import static uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper.WAITING_FOR_TRIBUNAL;

@RequiredArgsConstructor
@Service
@Slf4j
public class StoredApplicationService {

    private static final String NOT_SET = "Not set";

    private final CaseService caseService;
    private final NotificationService notificationService;
    private final CaseDetailsConverter caseDetailsConverter;

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

        GenericTseApplicationTypeItem appToModify = TseApplicationHelper.getSelectedApplication(
            caseData.getGenericTseApplicationCollection(),
            request.getApplicationId()
        );

        if (appToModify == null) {
            throw new IllegalArgumentException("Application id provided is incorrect");
        }

        appToModify.getValue().setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        appToModify.getValue().setDueDate(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7));
        appToModify.getValue().setApplicationState(IN_PROGRESS);
        appToModify.getValue().setStatus(OPEN_STATE);

        CaseDetails finalCaseDetails =  caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            caseDetailsConverter.caseDataContent(startEventResponse, caseData),
            request.getCaseTypeId()
        );

        if (finalCaseDetails != null) {
            sendSubmitStoredEmails(finalCaseDetails, appToModify);
        }

        return finalCaseDetails;
    }

    private void sendSubmitStoredEmails(CaseDetails finalCaseDetails, GenericTseApplicationTypeItem appToModify) {
        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(finalCaseDetails.getData());
        ClaimantIndType claimantIndType = caseData.getClaimantIndType();

        NotificationService.CoreEmailDetails details = new NotificationService.CoreEmailDetails(
            caseData,
            claimantIndType.getClaimantFirstNames() + " " + claimantIndType.getClaimantLastName(),
            caseData.getEthosCaseReference(),
            getRespondentNames(caseData),
            NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET),
            finalCaseDetails.getId().toString()
        );

        notificationService.sendSubmitStoredEmailToClaimant(details, appToModify);
        //TODO: sendAcknowledgementEmailToRespondents
        //TODO: sendAcknowledgementEmailToTribunal
    }

    /**
     * Submit stored Claimant Response to Respondent's request to Tell Something Else.
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

        GenericTseApplicationTypeItem appToModify = TseApplicationHelper.getSelectedApplication(
            caseData.getGenericTseApplicationCollection(), request.getApplicationId()
        );
        if (appToModify == null) {
            throw new IllegalArgumentException("Application id provided is incorrect");
        }

        TseRespondTypeItem responseToModify = TseApplicationHelper.getResponseInSelectedApplication(
            appToModify.getValue().getRespondCollection(), request.getRespondId()
        );
        if (responseToModify == null) {
            throw new IllegalArgumentException("Respond id provided is incorrect");
        }

        updateResponseForSubmitStored(responseToModify, caseData, appToModify);

        createAndAddPdfOfResponse(authorization, request, caseData, appToModify.getValue(),
            responseToModify.getValue());

        sendResponseToApplicationEmails(caseData, caseId, appToModify);
        // TODO: Send Tribunal Email

        return caseService.submitUpdate(
            authorization, caseId, caseDetailsConverter.caseDataContent(startEventResponse, caseData), caseTypeId);
    }

    private static void updateResponseForSubmitStored(TseRespondTypeItem responseToModify, CaseData caseData,
                                                      GenericTseApplicationTypeItem appToModify) {
        TseRespondType tseRespondType = responseToModify.getValue();
        if (tseRespondType.getSupportingMaterial() != null) {
            DocumentTypeItem documentTypeItem =
                (DocumentTypeItem) tseRespondType.getSupportingMaterial().get(0);
            if (caseData.getDocumentCollection() == null) {
                caseData.setDocumentCollection(new ArrayList<>());
            }
            caseData.getDocumentCollection().add(documentTypeItem);
        }
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

    private void sendResponseToApplicationEmails(CaseData caseData, String caseId,
                                                 GenericTseApplicationTypeItem appToModify) {
        ClaimantIndType claimantIndType = caseData.getClaimantIndType();

        NotificationService.CoreEmailDetails details = new NotificationService.CoreEmailDetails(
            caseData,
            claimantIndType.getClaimantFirstNames() + " " + claimantIndType.getClaimantLastName(),
            caseData.getEthosCaseReference(),
            getRespondentNames(caseData),
            NotificationsHelper.getNearestHearingToReferral(caseData, NOT_SET),
            caseId
        );

        //TODO: sendResponseEmailToTribunal
        notificationService.sendSubmitStoredRespondToAppEmailToClaimant(details, appToModify);
        //TODO: sendResponseEmailToRespondent?
    }

    public CaseDetails submitRespondToTribunal(String authorization, UpdateStoredRespondToApplicationRequest request) {
        //TODO:
    }
}
