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
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToTribunalRequest;
import uk.gov.hmcts.reform.et.syaapi.service.StoredApplicationService;
import uk.gov.hmcts.reform.et.syaapi.service.StoredRespondToApplicationSubmitService;
import uk.gov.hmcts.reform.et.syaapi.service.StoredRespondToTribunalSubmitService;

import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/store")
public class StoreCaseController {

    private final StoredApplicationService storedApplicationService;
    private final StoredRespondToApplicationSubmitService storedRespondToApplicationSubmitService;
    private final StoredRespondToTribunalSubmitService storedRespondToTribunalSubmitService;

    /**
     * Store a Claimant Application.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains the claimant application passed from sya-frontend
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/store-claimant-application")
    @Operation(summary = "Store a claimant application")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> storeClaimantApplication(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody ClaimantApplicationRequest request
    ) {
        log.info("Received store claimant application request - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );
        CaseDetails finalCaseDetails = storedApplicationService.storeApplication(authorization, request);
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
        CaseDetails finalCaseDetails =
            storedRespondToApplicationSubmitService.submitRespondToApplication(authorization, request);
        return ok(finalCaseDetails);
    }

    /**
     * Updates a Tribunal Send Notification from stored to submit.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains the appId and claimant response passed from sya-frontend
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/submit-stored-respond-to-tribunal")
    @Operation(summary = "Submit Stored Respond to a Tribunal Send Notification")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> submitStoredRespondToTribunal(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody UpdateStoredRespondToTribunalRequest request
    ) {
        log.info("Received submit respond to application request - caseTypeId: {} caseId: {}",
            request.getCaseTypeId(), request.getCaseId()
        );
        CaseDetails finalCaseDetails =
            storedRespondToTribunalSubmitService.submitRespondToTribunal(authorization, request);
        return ok(finalCaseDetails);
    }
}
