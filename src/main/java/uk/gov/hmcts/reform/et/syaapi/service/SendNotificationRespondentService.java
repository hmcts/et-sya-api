package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.PseNotificationHelper;
import uk.gov.hmcts.reform.et.syaapi.models.ChangeRespondentNotificationStatusRequest;

import java.util.ArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendNotificationRespondentService {

    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;

    /**
     * Update Respondent's application state.
     *
     * @param authorization - authorization
     * @param request - request with application's id
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails changeRespondentNotificationStatus(String authorization,
                                                          ChangeRespondentNotificationStatusRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.UPDATE_RESPONDENT_NOTIFICATION_STATE
        );

        CaseData caseData = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(startEventResponse.getCaseDetails().getData());

        SendNotificationTypeItem itemToModify = PseNotificationHelper.getSelectedNotification(
            caseData.getSendNotificationCollection(),
            request.getNotificationId()
        );
        if (itemToModify == null) {
            throw new IllegalArgumentException("Notification id provided is incorrect");
        }

        updateRespondentState(itemToModify.getValue(), request.getUserIdamId(), request.getNewStatus());

        return caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            caseDetailsConverter.caseDataContent(startEventResponse, caseData),
            request.getCaseTypeId()
        );
    }

    private void updateRespondentState(SendNotificationType app, String userIdamId, String newState) {
        if (app.getRespondentState() == null) {
            app.setRespondentState(new ArrayList<>());
        }

        app.getRespondentState().stream()
            .filter(status -> status.getValue().getUserIdamId().equals(userIdamId))
            .findFirst()
            .ifPresentOrElse(
                status -> status.getValue().setNotificationState(newState),
                () -> app.getRespondentState().add(PseNotificationHelper.buildPseStatusTypeItem(userIdamId, newState))
        );
    }
}
