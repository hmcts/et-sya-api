package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredApplicationRequest;
import uk.gov.service.notify.NotificationClientException;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.YES;

@RequiredArgsConstructor
@Service
@Slf4j
public class StoredApplicationSubmitService {

    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;

    private static final String APP_ID_INCORRECT = "Application id provided is incorrect";

    /**
     * Submits a stored Claimant Application.
     *
     * @param authorization - authorization
     * @param request - request with application's id
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails submitStoredApplication(String authorization, SubmitStoredApplicationRequest request)
        throws NotificationClientException {
        String caseTypeId = request.getCaseTypeId();
        CaseDetails caseDetails = caseService.getUserCase(authorization, request.getCaseId());
        ClaimantTse claimantTse = new ClaimantTse();
        caseDetails.getData().put("claimantTse", claimantTse);

        if (YES.equals(claimantTse.getCopyToOtherPartyYesOrNo())) {
            try {
                log.info("Uploading pdf of TSE application");
                caseService.uploadTseCyaAsPdf(authorization, caseDetails, claimantTse, caseTypeId);
            } catch (CaseDocumentException | DocumentGenerationException e) {
                log.error("Couldn't upload pdf of TSE application " + e.getMessage());
            }
        }

        UploadedDocumentType contactApplicationFile = claimantTse.getContactApplicationFile();
        if (contactApplicationFile != null) {
            log.info("Uploading supporting file to document collection");
            caseService.uploadTseSupportingDocument(caseDetails, contactApplicationFile,
                                                    claimantTse.getContactApplicationType()
            );
        }

        CaseDetails finalCaseDetails = caseService.triggerEvent(
            authorization,
            request.getCaseId(),
            CaseEvent.SUBMIT_CLAIMANT_TSE,
            caseTypeId,
            caseDetails.getData()
        );

        sendAcknowledgementEmails(authorization, request, finalCaseDetails);

        return finalCaseDetails;
    }

    /**
     * Remove a stored Claimant Application.
     *
     * @param authorization - authorization
     * @param request - request with application's id
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails removeStoredApplication(String authorization, SubmitStoredApplicationRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.SUBMIT_STORED_CLAIMANT_TSE
        );

        CaseData caseData = EmployeeObjectMapper
            .mapRequestCaseDataToCaseData(startEventResponse.getCaseDetails().getData());

        // Remove item from tseApplicationStoredCollection
        caseData.getTseApplicationStoredCollection().removeIf(item -> item.getId().equals(request.getApplicationId()));

        return caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            caseDetailsConverter.caseDataContent(startEventResponse, caseData),
            request.getCaseTypeId()
        );
    }
}
