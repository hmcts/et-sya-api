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
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationAddResponseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationStateUpdateRequest;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.SendNotificationService;
import uk.gov.hmcts.reform.et.syaapi.service.StoredApplicationService;

import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/sendNotification")
public class SendNotificationController {

    private final SendNotificationService sendNotificationService;
    private final StoredApplicationService storedApplicationService;

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
        CaseDetails finalCaseDetails = sendNotificationService.addResponseSendNotification(authorization, request);
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
    @Operation(summary = "Submit Stored Respond to an application")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> submitStoredRespondToApplication(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody UpdateStoredRespondToApplicationRequest request
    ) {
        log.info("Received submit respond to application request - caseTypeId: {} caseId: {}",
                 request.getCaseTypeId(), request.getCaseId()
        );

        CaseDetails finalCaseDetails = storedApplicationService.submitRespondToTribunal(authorization, request);

        return ok(finalCaseDetails);
    }
}
