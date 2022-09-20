package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;

import java.util.List;
import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/cases")
public class ManageCaseController {

    private final CaseService caseService;

    @PostMapping("/user-case")
    @Operation(summary = "Return individual case details")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> getUserCaseDetails(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestBody CaseRequest caseRequest) {
        var caseDetails = caseService.getUserCase(authorization, caseRequest);
        return ok(caseDetails);
    }

    @GetMapping("/user-cases")
    @Operation(summary = "Return list of case details for a given user")
    @ApiResponseGroup
    public ResponseEntity<List<CaseDetails>> getUserCasesDetails(
        @RequestHeader(AUTHORIZATION) String authorization) {
        var caseDetails = caseService.getAllUserCases(authorization);
        return ok(caseDetails);
    }

    @PostMapping("/initiate-case")
    @Operation(summary = "Create a draft case for the user")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> createDraftCase(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody CaseRequest caseRequest
    ) {
        log.info("Received initiate-case request - caseTypeId: {}",
                 caseRequest.getCaseId());

        var caseDetails = caseService.createCase(authorization, caseRequest);
        return ok(caseDetails);
    }

    @PutMapping("/update-case")
    @Operation(summary = "Update draft case API method")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> updateDraftCase(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody CaseRequest caseRequest
    ) {
        log.info("Received update-case request - caseTypeId: {} caseId: {}",
                 caseRequest.getCaseTypeId(), caseRequest.getCaseId());

        var caseDetails = caseService.triggerEvent(
            authorization,
            caseRequest.getCaseId(),
            CaseEvent.UPDATE_CASE_DRAFT,
            caseRequest.getCaseTypeId(),
            caseRequest.getCaseData()
        );
        return ok(caseDetails);
    }

    @PutMapping("/submit-case")
    @Operation(summary = "Submit a draft case API method")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> submitCase(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody CaseRequest caseRequest
    ) {
        log.info("Received submit-case request - caseTypeId: {} caseId: {}",
                 caseRequest.getCaseTypeId(), caseRequest.getCaseId());

        var caseDetails = caseService.triggerEvent(
            authorization,
            caseRequest.getCaseId(),
            CaseEvent.SUBMIT_CASE_DRAFT,
            caseRequest.getCaseTypeId(),
            caseRequest.getCaseData()
        );
        return ok(caseDetails);
    }

    @PutMapping("/update-case-submitted")
    @Operation(summary = "Update submitted case API method")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> updateCase(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody CaseRequest caseRequest
    ) {
        log.info("Received update-case-submitted request - caseTypeId: {} caseId: {}",
                 caseRequest.getCaseTypeId(), caseRequest.getCaseId());

        var caseDetails = caseService.triggerEvent(
            authorization,
            caseRequest.getCaseId(),
            CaseEvent.UPDATE_CASE_SUBMITTED,
            caseRequest.getCaseTypeId(),
            caseRequest.getCaseData()
        );
        return ok(caseDetails);
    }
}
