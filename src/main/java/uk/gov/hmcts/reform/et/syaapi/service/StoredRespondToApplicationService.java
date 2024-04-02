package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredRespondToApplicationRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.IN_PROGRESS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.CLAIMANT_CORRESPONDENCE_DOCUMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.YES;
import static uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper.getCurrentDateTime;
import static uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper.setRespondentApplicationWithResponse;

@RequiredArgsConstructor
@Service
@Slf4j
public class StoredRespondToApplicationService {

    private final CaseService caseService;
    private final NotificationService notificationService;
    private final CaseDocumentService caseDocumentService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;

    private static final String APP_ID_INCORRECT = "Application id provided is incorrect";
    private static final String RESPOND_ID_INCORRECT = "Respond id provided is incorrect";

    /**
     * Store Claimant Response to Application.
     *
     * @param authorization - authorization
     * @param request - response from the claimant
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails storeRespondToApplication(String authorization, RespondToApplicationRequest request) {
        String caseId = request.getCaseId();
        String caseTypeId = request.getCaseTypeId();

        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            caseId,
            caseTypeId,
            CaseEvent.STORE_CLAIMANT_TSE_RESPOND
        );

        CaseData caseData = EmployeeObjectMapper
            .mapRequestCaseDataToCaseData(startEventResponse.getCaseDetails().getData());

        // Update application
        GenericTseApplicationTypeItem appToModify = TseApplicationHelper.getSelectedApplication(
            caseData.getGenericTseApplicationCollection(), request.getApplicationId()
        );

        if (appToModify == null) {
            throw new IllegalArgumentException(APP_ID_INCORRECT);
        }

        GenericTseApplicationType appType = appToModify.getValue();
        appType.setApplicationState(STORED);
        appType.setClaimantResponseRequired(NO);

        // Add response to respondStoredCollection
        if (CollectionUtils.isEmpty(appType.getRespondStoredCollection())) {
            appType.setRespondStoredCollection(new ArrayList<>());
        }

        appType.getRespondStoredCollection().add(
            TseRespondTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(getRespondToStore(request, appType))
                .build()
        );

        CaseDetails submitUpdate = caseService.submitUpdate(
            authorization, caseId, caseDetailsConverter.caseDataContent(startEventResponse, caseData), caseTypeId);

        // Send email
        NotificationService.CoreEmailDetails details = notificationService.formatCoreEmailDetails(caseData, caseId);
        notificationService.sendStoredEmailToClaimant(details, appType.getType());

        return submitUpdate;
    }

    private TseRespondType getRespondToStore(RespondToApplicationRequest request,
                                   GenericTseApplicationType appToModify) {
        TseRespondType responseToAdd = request.getResponse();
        responseToAdd.setDate(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        responseToAdd.setFrom(CLAIMANT_TITLE);
        responseToAdd.setDateTime(getCurrentDateTime());
        responseToAdd.setApplicationType(appToModify.getType());

        if (request.getSupportingMaterialFile() != null) {
            DocumentTypeItem documentTypeItem = caseDocumentService.createDocumentTypeItem(
                CLAIMANT_CORRESPONDENCE_DOCUMENT,
                request.getSupportingMaterialFile()
            );
            documentTypeItem.getValue().setShortDescription("Response to " + appToModify.getType());
            responseToAdd.setSupportingMaterial(new ArrayList<>());
            responseToAdd.getSupportingMaterial().add(documentTypeItem);
        }

        return responseToAdd;
    }

    /**
     * Submit stored claimant response to Tribunal response.
     *
     * @param authorization - authorization
     * @param request - response from the claimant
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails submitRespondToApplication(String authorization,
                                                  SubmitStoredRespondToApplicationRequest request) {
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
        GenericTseApplicationType appType = appToModify.getValue();

        // Get selected TseRespondTypeItem
        TseRespondTypeItem responseToAdd = TseApplicationHelper.getResponseInSelectedApplication(
            appToModify.getValue().getRespondStoredCollection(), request.getStoredRespondId()
        );
        if (responseToAdd == null) {
            throw new IllegalArgumentException(RESPOND_ID_INCORRECT);
        }
        TseRespondType respondType = responseToAdd.getValue();

        // Update application and Add response to RespondCollection
        boolean isRespondingToTribunal = true;
        appType.setApplicationState(IN_PROGRESS);
        appType.setClaimantResponseRequired(NO);

        RespondToApplicationRequest respondRequest = RespondToApplicationRequest.builder()
            .caseId(request.getCaseId())
            .caseTypeId(request.getCaseTypeId())
            .applicationId(request.getApplicationId())
            .supportingMaterialFile(respondType.getSupportingMaterial().isEmpty()
                                        ? null
                                        : respondType.getSupportingMaterial().get(0).getValue().getUploadedDocument())
            .response(respondType)
            .isRespondingToRequestOrOrder(isRespondingToTribunal)
            .build();

        boolean waEnabled = featureToggleService.isWorkAllocationEnabled();
        setRespondentApplicationWithResponse(respondRequest, appType, caseData, caseDocumentService, waEnabled);

        // Update pdf
        createAndAddPdfOfResponse(authorization, respondRequest, caseData, appType);

        // Remove Stored Response
        if (isNotEmpty(appType.getRespondStoredCollection())
            && request.getStoredRespondId() != null) {
            appType.getRespondStoredCollection().removeIf(item -> item.getId().equals(request.getStoredRespondId()));
        }

        CaseDetails submitUpdate = caseService.submitUpdate(
            authorization, caseId, caseDetailsConverter.caseDataContent(startEventResponse, caseData), caseTypeId);

        // Send confirmation email
        sendResponseToApplicationEmails(appType, caseData, caseId, isRespondingToTribunal);

        return submitUpdate;
    }

    private void createAndAddPdfOfResponse(
        String authorization,
        RespondToApplicationRequest request,
        CaseData caseData,
        GenericTseApplicationType application
    ) {
        if (YES.equals(request.getResponse().getCopyToOtherParty())) {
            try {
                log.info("Uploading pdf of claimant response to application");
                caseService.createResponsePdf(
                    authorization,
                    caseData,
                    request,
                    application.getType()
                );
            } catch (CaseDocumentException | DocumentGenerationException e) {
                log.error("Couldn't upload pdf of TSE application " + e.getMessage());
            }
        }
    }

    private void sendResponseToApplicationEmails(
        GenericTseApplicationType application,
        CaseData caseData,
        String caseId,
        boolean isRespondingToRequestOrOrder
    ) {
        String type = application.getType();
        NotificationService.CoreEmailDetails details = notificationService.formatCoreEmailDetails(caseData, caseId);
        notificationService.sendResponseEmailToTribunal(details, type, isRespondingToRequestOrOrder);
        notificationService.sendSubmitStoredEmailToClaimant(details, type);
    }
}
