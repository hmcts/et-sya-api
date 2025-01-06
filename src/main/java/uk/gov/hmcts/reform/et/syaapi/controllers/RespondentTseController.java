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
import uk.gov.hmcts.reform.et.syaapi.models.RespondentApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.ApplicationService;
import uk.gov.service.notify.NotificationClientException;

import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/respondentTSE")
public class RespondentTseController {

    private final ApplicationService applicationService;

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
}
