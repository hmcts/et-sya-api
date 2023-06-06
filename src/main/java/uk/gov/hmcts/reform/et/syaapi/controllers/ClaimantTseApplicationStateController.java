package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantTseApplicationStateUpdateRequest;
import uk.gov.hmcts.reform.et.syaapi.service.ClaimantTseApplicationStateService;

import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/applicationState")
public class ClaimantTseApplicationStateController {
    private final ClaimantTseApplicationStateService claimantTseApplicationStateService;

    /**
     * Updates claimant tse application status.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains sendNotification id and new status value passed
     *                      from sya-frontend
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/update-claimant-tse-application-state")
    @Operation(summary = "Update claimant tse application state")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> updateClaimantTseApplicationState(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody ClaimantTseApplicationStateUpdateRequest request
    ) {
        log.info("Received update claimant tse application state request - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );
        CaseDetails finalCaseDetails
            = claimantTseApplicationStateService.updateClaimantTseApplicationState(authorization, request);
        return ok(finalCaseDetails);
    }
}
