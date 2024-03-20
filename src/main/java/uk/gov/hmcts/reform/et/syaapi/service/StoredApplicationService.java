package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredApplicationRequest;

import java.time.LocalDate;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.IN_PROGRESS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;

@RequiredArgsConstructor
@Service
@Slf4j
public class StoredApplicationService {

    private final CaseService caseService;
    private final NotificationService notificationService;
    private final CaseDetailsConverter caseDetailsConverter;

    private static final String APP_ID_INCORRECT = "Application id provided is incorrect";

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
}
