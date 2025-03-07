package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseStatusType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.models.ChangeRespondentApplicationStatusRequest;

import java.util.ArrayList;

@RequiredArgsConstructor
@Service
@Slf4j
public class RespondentTseService {
    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;

    /**
     * Update Respondent's application state.
     *
     * @param authorization - authorization
     * @param request - request with application's id
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails changeRespondentApplicationStatus(String authorization,
                                                         ChangeRespondentApplicationStatusRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.UPDATE_APPLICATION_STATE
        );

        CaseData caseData = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(startEventResponse.getCaseDetails().getData());

        GenericTseApplicationTypeItem appToModify = TseApplicationHelper.getSelectedApplication(
            caseData.getGenericTseApplicationCollection(),
            request.getApplicationId()
        );

        if (appToModify == null) {
            throw new IllegalArgumentException("Application id provided is incorrect");
        }

        updateRespondentState(appToModify.getValue(), request.getUserIdamId(), request.getNewStatus());

        return caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            caseDetailsConverter.caseDataContent(startEventResponse, caseData),
            request.getCaseTypeId()
        );
    }

    private void updateRespondentState(GenericTseApplicationType app, String userIdamId, String newState) {
        if (app.getRespondentState() == null) {
            app.setRespondentState(new ArrayList<>());
        }
        app.getRespondentState().stream()
            .filter(status -> status.getUserIdamId().equals(userIdamId))
            .findFirst()
            .ifPresentOrElse(
                status -> status.setApplicationState(newState),
                () -> app.getRespondentState().add(TseStatusType.builder()
                                                       .userIdamId(userIdamId)
                                                       .applicationState(newState)
                                                       .build())
        );
    }
}
