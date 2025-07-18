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
import uk.gov.hmcts.reform.et.syaapi.models.AdminDecisionNotificationStateUpdateRequest;
import uk.gov.hmcts.reform.et.syaapi.service.AdminDecisionNotificationService;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/tseAdmin")
public class AdminDecisionNotificationController {

    private final AdminDecisionNotificationService adminDecisionNotificationService;

    /**
     * Updates SendNotification status.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains sendNotification id and new status value passed
     *                      from sya-frontend
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/update-admin-decision-state")
    @Operation(summary = "Update admin decision state")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> updateAdminDecisionNotificationState(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody AdminDecisionNotificationStateUpdateRequest request
    ) {
        log.info("Received update sendNotification state request - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );
        CaseDetails finalCaseDetails
            = adminDecisionNotificationService.updateAdminDecisionNotificationState(authorization, request);
        return ok(finalCaseDetails);
    }

}

