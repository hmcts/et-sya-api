package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.StoredApplicationService;

import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/store")
public class StoreCaseController {

    private final StoredApplicationService storedApplicationService;

    /**
     * Submits a stored Claimant Application.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains the claimant application passed from sya-frontend
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/submit-stored-claimant-application")
    @Operation(summary = "Submit a stored claimant application")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> submitStoredClaimantApplication(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody SubmitStoredApplicationRequest request
    ) {
        log.info("Received submit the stored claimant application request - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );
        CaseDetails finalCaseDetails = storedApplicationService.submitStoredApplication(authorization, request);
        return ok(finalCaseDetails);
    }

    /**
     * Respond to an Application.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains the appId and claimant response passed from sya-frontend
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/submit-stored-respond-to-application")
    @Operation(summary = "Submit Stored Respond to an application")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> submitStoredRespondToApplication(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody UpdateStoredRespondToApplicationRequest request
    ) {
        log.info("Received submit respond to application request - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );
        CaseDetails finalCaseDetails = storedApplicationService.submitRespondToApplication(authorization, request);
        return ok(finalCaseDetails);
    }
}
