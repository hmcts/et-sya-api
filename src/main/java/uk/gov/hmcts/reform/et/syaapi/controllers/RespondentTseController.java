package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
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
import uk.gov.hmcts.reform.et.syaapi.models.ChangeRespondentApplicationStatusRequest;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.RespondentApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.ApplicationService;
import uk.gov.hmcts.reform.et.syaapi.service.RespondentTseService;
import uk.gov.hmcts.reform.et.syaapi.service.StoreRespondentTseService;
import uk.gov.service.notify.NotificationClientException;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/respondentTSE")
public class RespondentTseController {

    private final ApplicationService applicationService;
    private final RespondentTseService respondentTseService;
    private final StoreRespondentTseService storeRespondentTseService;

    /**
     * Submit a Respondent Application.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains the appId and respondent
     *                      application passed from sya-frontend
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/submit-respondent-application")
    @Operation(summary = "Submit a respondent application")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> submitRespondentApplication(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody RespondentApplicationRequest request
    ) throws NotificationClientException {
        log.info("Received submit respondent application request - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );

        CaseDetails finalCaseDetails = applicationService.submitRespondentApplication(authorization, request);

        return ok(finalCaseDetails);
    }

    /**
     * Store a Respondent Application.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains the appId and respondent
     *                      application passed from sya-frontend
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/store-respondent-application")
    @Operation(summary = "Store a respondent application")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> storeRespondentApplication(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody RespondentApplicationRequest request
    ) {
        log.info("Received store respondent application request - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );

        CaseDetails finalCaseDetails = storeRespondentTseService.storeApplication(authorization, request);

        return ok(finalCaseDetails);
    }

    /**
     * Respond to an application.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains the appId and
     *                      respondent application passed from sya-frontend
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/respond-to-claimant-application")
    @Operation(summary = "Respond to an application")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> respondToApplication(
            @RequestHeader(AUTHORIZATION) String authorization,
            @NotNull @RequestBody RespondToApplicationRequest request
    ) {
        log.info("Received submit respond to application request - caseTypeId: {} caseId: {}",
                request.getCaseTypeId(), request.getCaseId()
        );

        CaseDetails finalCaseDetails = applicationService.respondToClaimantApplication(authorization, request);

        return ok(finalCaseDetails);
    }

    /**
     * Change respondent application status in syr.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains the appId and new status
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/change-respondent-application-status")
    @Operation(summary = "Change respondent application status")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> changeRespondentApplicationStatus(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody ChangeRespondentApplicationStatusRequest request
    ) {
        log.info("Received a change respondent application status request - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );

        CaseDetails finalCaseDetails = respondentTseService.changeRespondentApplicationStatus(authorization, request);

        return ok(finalCaseDetails);
    }
}
