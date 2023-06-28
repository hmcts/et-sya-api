package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.HubLinksStatusesRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;
import uk.gov.hmcts.reform.et.syaapi.exception.PdfServiceException;

import java.util.List;
import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

/**
 * Rest Controller will use {@link CaseService} for interacting and accessing cases.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/cases")
public class ManageCaseController {

    private final CaseService caseService;

    /**
     * Accepts parameter of type {@link CaseRequest} and returns the case specified in 'getCaseId'.
     * @param authorization jwt of the user
     * @param caseRequest search query for the requested case
     * @return the requested case wrapped in a {@link CaseDetails} object
     */
    @PostMapping("/user-case")
    @Operation(summary = "Return individual case details")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> getUserCaseDetails(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestBody CaseRequest caseRequest) {
        CaseDetails caseDetails = caseService.getUserCase(authorization, caseRequest.getCaseId());
        return ok(caseDetails);
    }

    /**
     * Uses the authorization token to extract the user and return all the cases that belong to that user.
     * @param authorization the JWT that contains the user information
     * @return a list of cases for the given user wrapped in a {@link CaseDetails} object
     */
    @GetMapping("/user-cases")
    @Operation(summary = "Return list of case details for a given user")
    @ApiResponseGroup
    public ResponseEntity<List<CaseDetails>> getUserCasesDetails(
        @RequestHeader(AUTHORIZATION) String authorization) {
        var caseDetails = caseService.getAllUserCases(authorization);
        return ok(caseDetails);
    }

    /**
     * Creates an initial draft case using the passed parameters.
     * @param authorization jwt of the user
     * @param caseRequest the inital values for the case to be created within a {@link CaseRequest} object
     * @return the newly created draft in an {@link CaseDetails} object
     */
    @PostMapping("/initiate-case")
    @Operation(summary = "Create a draft case for the user")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> createDraftCase(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody CaseRequest caseRequest
    ) {
        log.info("Received initiate-case request");

        var caseDetails = caseService.createCase(authorization, caseRequest);
        return ok(caseDetails);
    }

    /**
     * Updates the draft case with the new information defined in parameters.
     * @param authorization jwt of the user
     * @param caseRequest the new case set to replace the current case wrapped in a {@link CaseRequest} object
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/update-case")
    @Operation(summary = "Update draft case API method")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> updateDraftCase(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody CaseRequest caseRequest
    ) {
        log.info("Received update-case request - caseTypeId: {} caseId: {}",
                 caseRequest.getCaseTypeId(), caseRequest.getCaseId());

        var caseDetails = caseService.updateCase(authorization, caseRequest);
        return ok(caseDetails);
    }

    /**
     * Accepts a draft case and triggers a submit event and sent confirmation email.
     * @param authorization jwt of the user
     * @param caseRequest the case to be submitted {@link CaseRequest} object
     * @return the newly submitted case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/submit-case")
    @Operation(summary = "Submit a draft case API method")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> submitCase(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody CaseRequest caseRequest
    ) {
        log.info("Received submit-case request - caseTypeId: {} caseId: {}",
                 caseRequest.getCaseTypeId(), caseRequest.getCaseId());
        try {
            return ok(caseService.submitCase(authorization, caseRequest));
        } catch (PdfServiceException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Updates the Citizen Hub links Statuses.
     * @param authorization jwt of the user
     * @param request the request object which contains the HubLinksStatuses passed from sya-frontend
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/update-hub-links-statuses")
    @Operation(summary = "Update Citizen Hub links Statuses")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> updateHubLinksStatuses(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody HubLinksStatusesRequest request
    ) {
        log.info("Received update hub link statuses request - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId());

        CaseDetails caseDetails = caseService.getUserCase(authorization, request.getCaseId());
        caseDetails.getData().put("hubLinksStatuses", request.getHubLinksStatuses());

        CaseDetails finalCaseDetails = caseService.triggerEvent(
            authorization,
            request.getCaseId(),
            CaseEvent.UPDATE_CASE_SUBMITTED,
            request.getCaseTypeId(),
            caseDetails.getData()
        );
        return ok(finalCaseDetails);
    }
}
