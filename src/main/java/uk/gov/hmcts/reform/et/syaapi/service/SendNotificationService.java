package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationStateUpdateRequest;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SendNotificationService {

    private final CaseService caseService;
    private static final String VIEWED = "viewed";

    public CaseDetails updateSendNotificationState(String authorization, SendNotificationStateUpdateRequest request) {
        CaseDetails caseDetails = caseService.getUserCase(authorization, request.getCaseId());
        Map<String, Object> detailsData = caseDetails.getData();
        List<SendNotificationTypeItem> notifications = EmployeeObjectMapper.mapRequestCaseDataToCaseData(detailsData)
            .getSendNotificationCollection();
        for (SendNotificationTypeItem item : notifications) {
            if (item.getId().equals(request.getSendNotificationId())) {
                item.getValue().setNotificationState(VIEWED);
                break;
            }
        }

        detailsData.put("sendNotificationCollection", notifications);

        return caseService.triggerEvent(
            authorization,
            request.getCaseId(),
            CaseEvent.UPDATE_CASE_SUBMITTED,
            request.getCaseTypeId(),
            detailsData
        );
    }

}
