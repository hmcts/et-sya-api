package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredApplicationRequest;
import uk.gov.service.notify.NotificationClientException;

@RequiredArgsConstructor
@Service
@Slf4j
public class StoredApplicationSubmitService {

    private final CaseService caseService;
    private final ApplicationService applicationService;
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
        ClaimantApplicationRequest appRequest = ClaimantApplicationRequest.builder()
            .caseTypeId(request.getCaseTypeId())
            .caseId(request.getCaseId())
            .typeC(request.isTypeC())
            .claimantTse(request.getClaimantTse())
            .build();

        CaseDetails finalCaseDetails = applicationService.submitApplication(authorization, appRequest);

        // TODO: Remove item
        // Remove item from tseApplicationStoredCollection
        // caseData.getTseApplicationStoredCollection().removeIf(item -> item.getId().equals(request.getApplicationId()));

        return finalCaseDetails;
    }
}
