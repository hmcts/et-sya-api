package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantBundlesRequest;
import uk.gov.hmcts.reform.et.syaapi.service.BundlesService;
import uk.gov.hmcts.reform.et.syaapi.service.FeatureToggleService;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/bundles")
public class BundlesController {
    private final BundlesService bundlesService;
    private final FeatureToggleService featureToggleService;


    /**
     * Submits claimant hearing document pdf and related information.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains the claimant application passed from sya-frontend
     * @return the new updated case wrapped in a {@link CaseDetails
     * }
     */
    @PutMapping("/submit-bundles")
    @Operation(summary = "Submit bundles hearing document and related data")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> submitBundles(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody ClaimantBundlesRequest request
    ) {
        boolean bundlesToggle = featureToggleService.isBundlesEnabled();
        if (bundlesToggle) {
            log.info("Received submit bundles request - caseTypeId: {} caseId: {}",
                     request.getCaseTypeId(), request.getCaseId()
            );
            CaseDetails finalCaseDetails = bundlesService.submitBundles(authorization, request);
            return ok(finalCaseDetails);
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Bundles feature is not available");
    }
}
