package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.models.AdminDecisionNotificationStateUpdateRequest;

import static uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper.findAdminDecision;

@Service
@RequiredArgsConstructor

public class AdminDecisionNotificationService {

    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;
    private static final String VIEWED = "viewed";

    public CaseDetails updateAdminDecisionNotificationState(String authorization,
                                                            AdminDecisionNotificationStateUpdateRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.UPDATE_ADMIN_DECISION_STATE
        );

        CaseData caseData = EmployeeObjectMapper
            .mapRequestCaseDataToCaseData(startEventResponse.getCaseDetails().getData());

        GenericTseApplicationTypeItem selectedApplication = TseApplicationHelper.getSelectedApplication(
            caseData.getGenericTseApplicationCollection(), request.getAppId()
        );
        TseAdminRecordDecisionTypeItem decisionToUpdate = findAdminDecision(
            selectedApplication,
            request.getAdminDecisionId()
        );

        if (decisionToUpdate != null) {
            decisionToUpdate.getValue().setDecisionState(VIEWED);
            CaseDataContent content = caseDetailsConverter.caseDataContent(startEventResponse, caseData);

            return caseService.submitUpdate(
                authorization,
                request.getCaseId(),
                content,
                request.getCaseTypeId()
            );
        } else {
            throw new IllegalArgumentException("Admin decision id is invalid");
        }
    }
}
