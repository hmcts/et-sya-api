package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantTseApplicationStateUpdateRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimantTseApplicationStateService {
    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;
    private static final String VIEWED = "viewed";
    private static final String IN_PROGRESS = "inProgress";
    private static final String UPDATED = "updated";
    private static final String WAITING_FOR_TRIBUNAL = "waitingForTheTribunal";
    private static final String NOT_STARTED_YET = "notStartedYet";
    private static final String NOT_VIEWED = "notViewedYet";

    public CaseDetails updateClaimantTseApplicationState(String authorization,
                                                         ClaimantTseApplicationStateUpdateRequest request) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.UPDATE_CLAIMANT_TSE_STATE
        );

        CaseData caseData = EmployeeObjectMapper
            .mapRequestCaseDataToCaseData(startEventResponse.getCaseDetails().getData());


        List<GenericTseApplicationTypeItem> notifications = caseData.getGenericTseApplicationCollection();
        for (GenericTseApplicationTypeItem item : notifications) {
            if (item.getId().equals(request.getApplicationId())) {
                item.getValue().setApplicationState(request.getApplicationState());
                break;
            }
        }

        CaseDataContent content = caseDetailsConverter.caseDataContent(startEventResponse, caseData);

        return caseService.submitUpdate(
            authorization,
            request.getCaseId(),
            content,
            request.getCaseTypeId()
        );
    }
}
