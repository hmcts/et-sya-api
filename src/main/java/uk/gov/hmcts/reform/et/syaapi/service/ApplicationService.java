package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;

@RequiredArgsConstructor
@Service
public class ApplicationService {

    private final CaseService caseService;
    private final NotificationService notificationService;


    public CaseDetails submitApplication(String authorization, ClaimantApplicationRequest request) {
        CaseDetails caseDetails = caseService.getUserCase(authorization, request.getCaseId());
        caseDetails.getData().put("claimantTse", request.getClaimantTse());


        CaseDetails finalCaseDetails = caseService.triggerEvent(
            authorization,
            request.getCaseId(),
            CaseEvent.UPDATE_CASE_SUBMITTED,
            request.getCaseTypeId(),
            caseDetails.getData()
        );

        notificationService.sendAcknowledgementEmailToClaimant(
            finalCaseDetails,
            request.getClaimantTse()
        );

        return finalCaseDetails;


    }


}
