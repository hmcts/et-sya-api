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
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;
import uk.gov.hmcts.reform.et.syaapi.service.CaseRoleManagementService;

import java.io.IOException;
import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

/**
 * Rest Controller will use {@link CaseService} for interacting and accessing cases.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/caseRoleManagement")
public class CaseRoleManagementController {

    private final CaseRoleManagementService caseRoleManagementService;

    /**
     * Assigns defendant role to case.
     * @param authorization jwt of the user
     * @param caseRequest the request object which contains the case data including respondent address
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/assign-defendant")
    @Operation(summary = "Assigns defendant role to the case")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> assignDefendant(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody CaseRequest caseRequest
    ) throws IOException {
        log.info("Received a request to set tribunal response to viewed - caseTypeId: {} caseId: {}",
                 caseRequest.getCaseTypeId(), caseRequest.getCaseId()
        );
        caseRoleManagementService.assignDefendant(authorization, caseRequest);
        return ok(CaseDetails.builder().build());
    }

    /**
     * Removes defendant role from case.
     * @param authorization jwt of the user
     * @param caseRequest the request object which contains the case data including respondent address
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/revoke-defendant")
    @Operation(summary = "Removes defendant role from case")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> revokeDefendant(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody CaseRequest caseRequest
    ) throws IOException {
        log.info("Received a request to remove respondent - caseTypeId: {} caseId: {}",
                 caseRequest.getCaseTypeId(), caseRequest.getCaseId()
        );
        caseRoleManagementService.revokeDefendant(authorization, caseRequest);
        return ok(CaseDetails.builder().build());
    }
}
