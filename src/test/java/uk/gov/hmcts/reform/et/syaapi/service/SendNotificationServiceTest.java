package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationAddResponseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationStateUpdateRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_NOTIFICATION_RESPONSE;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_NOTIFICATION_STATE;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.UPDATE_CASE_DRAFT;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {
    @Mock
    private CaseService caseService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @InjectMocks
    private SendNotificationService sendNotificationService;
    private TestData testData;
    private static final String MOCK_TOKEN = "Bearer TestServiceAuth";

    @BeforeEach
    void beforeEach() {
        testData = new TestData();
    }

    @Test
    void shouldUpdateSendNotificationState() {
        SendNotificationStateUpdateRequest request = testData.getSendNotificationStateUpdateRequest();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_NOTIFICATION_STATE
        )).thenReturn(testData.getUpdateCaseEventResponse());

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id("777")
                .value(SendNotificationType.builder()
                           .notificationState("viewed")
                           .build())
                .build()
        );

        Map<String, Object> updatedCaseData = new ConcurrentHashMap<>();
        updatedCaseData.put("sendNotificationCollection", items);

        CaseDataContent expectedEnrichedData = CaseDataContent.builder()
            .event(Event.builder().id(UPDATE_CASE_DRAFT).build())
            .eventToken(testData.getStartEventResponse().getToken())
            .data(updatedCaseData)
            .build();

        when(caseDetailsConverter.caseDataContent(any(), any())).thenReturn(expectedEnrichedData);

        sendNotificationService.updateSendNotificationState(MOCK_TOKEN, request);

        verify(caseService, times(1)).submitUpdate(
            MOCK_TOKEN, "11", expectedEnrichedData, "1234");
    }


    @Test
    void shouldUpdateAddResponseSendNotification() {
        SendNotificationAddResponseRequest request = testData.getSendNotificationAddResponseRequest();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_NOTIFICATION_RESPONSE
        )).thenReturn(testData.getUpdateCaseEventResponse());

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id("777")
                .value(SendNotificationType.builder()
                           .build())
                .build()
        );

        Map<String, Object> updatedCaseData = new ConcurrentHashMap<>();
        updatedCaseData.put("sendNotificationCollection", items);

        CaseDataContent expectedEnrichedData = CaseDataContent.builder()
            .event(Event.builder().id(UPDATE_CASE_DRAFT).build())
            .eventToken(testData.getStartEventResponse().getToken())
            .data(updatedCaseData)
            .build();

        when(caseDetailsConverter.caseDataContent(any(), any())).thenReturn(expectedEnrichedData);

        sendNotificationService.addRequestSendNotification(MOCK_TOKEN, request);

        verify(caseService, times(1)).submitUpdate(
            MOCK_TOKEN, "11", expectedEnrichedData, "1234");
    }
}
