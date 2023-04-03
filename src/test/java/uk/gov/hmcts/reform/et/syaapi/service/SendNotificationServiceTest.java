package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationStateUpdateRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {
    @Mock
    private CaseService caseService;
    @InjectMocks
    private SendNotificationService sendNotificationService;
    private TestData testData;
    private static final String MOCK_TOKEN = "Bearer Token";

    @BeforeEach
    void beforeEach() {
        testData = new TestData();
    }

    @Test
    void shouldUpdateSendNotificationState() {
        SendNotificationStateUpdateRequest request = testData.getSendNotificationStateUpdateRequest();
        when(caseService.getUserCase(any(),any())).thenReturn(testData.getCaseDetailWithSendNotification());
        sendNotificationService.updateSendNotificationState(MOCK_TOKEN, request);

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id("777")
                .value(SendNotificationType.builder()
                           .notificationState("viewed")
                           .build())
                .build()
        );

        Map<String, Object> updatedCaseData = new HashMap<>();
        updatedCaseData.put("sendNotificationCollection", items);

        verify(caseService, times(1)).triggerEvent(
            MOCK_TOKEN, "11", CaseEvent.UPDATE_CASE_SUBMITTED, "1234", updatedCaseData);
    }
}
