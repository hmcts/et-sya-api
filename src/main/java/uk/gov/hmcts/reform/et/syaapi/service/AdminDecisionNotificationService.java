package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.AdminDecisionNotificationStateUpdateRequest;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

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

        List<TseAdminRecordDecisionTypeItem> notifications = caseData.getGenericTseApplicationCollection().stream()
            .map(tse -> {
                if (tse != null && tse.getValue() != null) {
                    return tse.getValue().getAdminDecision();
                } else {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .toList();

        for (TseAdminRecordDecisionTypeItem item : notifications) {
            if (item.getId().equals(request.getAdminDecisionId())) {
                item.getValue().setDecisionState(VIEWED);
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
