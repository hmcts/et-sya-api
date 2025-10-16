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
import uk.gov.hmcts.reform.et.syaapi.models.ChangeRespondentNotificationStatusRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationAddResponseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationStateUpdateRequest;
import uk.gov.hmcts.reform.et.syaapi.service.SendNotificationService;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/sendNotification")
public class SendNotificationController {

    private final SendNotificationService sendNotificationService;

    /**
     * Updates SendNotification status.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains sendNotification id and new status value passed
     *                      from sya-frontend
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/update-notification-state")
    @Operation(summary = "Update notification state")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> updateSendNotificationState(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody SendNotificationStateUpdateRequest request
    ) {
        log.info("Received update sendNotification state request - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );
        CaseDetails finalCaseDetails = sendNotificationService.updateSendNotificationState(authorization, request);
        return ok(finalCaseDetails);
    }

    /**
     * Adds pseResponse to a sendNotification object.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains sendNotification id and the new response
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/add-response-send-notification")
    @Operation(summary = "add response to send notification")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> addResponseSendNotification(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody SendNotificationAddResponseRequest request
    ) {
        log.info("Received response for case - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );
        CaseDetails finalCaseDetails = sendNotificationService.addClaimantResponseNotification(authorization, request);
        return ok(finalCaseDetails);
    }

    /**
     * Change respondent notification status in syr.
     * @param authorization jwt of the user
     * @param request       the request object which contains the appId and new status
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/change-respondent-notification-status")
    @Operation(summary = "Change respondent notification status")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> changeRespondentNotificationStatus(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody ChangeRespondentNotificationStatusRequest request
    ) {
        log.info("Received a change respondent notification status request - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );

        CaseDetails finalCaseDetails =
            sendNotificationService.changeRespondentNotificationStatus(authorization, request);

        return ok(finalCaseDetails);
    }

    /**
     * Adds pseResponse from respondent to a sendNotification object.
     *
     * @param authorization jwt of the user
     * @param request       the request object which contains sendNotification id and the new response
     * @return the new updated case wrapped in a {@link CaseDetails}
     */
    @PutMapping("/add-respondent-respond-to-notification")
    @Operation(summary = "add respondent response to send notification")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> addRespondentRespondToNotification(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody SendNotificationAddResponseRequest request
    ) {
        log.info("Received response from respondent for case - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );
        CaseDetails finalCaseDetails =
            sendNotificationService.addRespondentResponseNotification(authorization, request);
        return ok(finalCaseDetails);
    }
}
