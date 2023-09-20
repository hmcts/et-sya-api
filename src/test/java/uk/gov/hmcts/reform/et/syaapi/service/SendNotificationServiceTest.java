package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationAddResponseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationStateUpdateRequest;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;

import java.util.LinkedHashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_NOTIFICATION_RESPONSE;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_NOTIFICATION_STATE;
import static uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper.CLAIMANT;
import static uk.gov.hmcts.reform.et.syaapi.service.SendNotificationService.VIEWED;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {
    @Mock
    private CaseService caseService;
    @Mock
    private CaseDocumentService caseDocumentService;
    @Mock
    private NotificationService notificationService;
    private SendNotificationService sendNotificationService;
    private TestData testData;
    private static final String MOCK_TOKEN = "Bearer TestServiceAuth";
    private static final String ID = "777";

    @BeforeEach
    void beforeEach() {
        ObjectMapper objectMapper = new ObjectMapper();
        sendNotificationService = new SendNotificationService(
            caseService,
            caseDocumentService,
            new CaseDetailsConverter(objectMapper),
            notificationService
        );
        testData = new TestData();
    }

    @Test
    void shouldUpdateSendNotificationState() {
        SendNotificationStateUpdateRequest request = testData.getSendNotificationStateUpdateRequest();

        StartEventResponse updateCaseEventResponse = testData.getUpdateCaseEventResponse();
        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_NOTIFICATION_STATE
        )).thenReturn(updateCaseEventResponse);

        ListTypeItem<RespondNotificationType> from = ListTypeItem.from(
            GenericTypeItem.from(ID, RespondNotificationType.builder()
                .state(VIEWED).isClaimantResponseDue(YES).build()
            )
        );

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .notificationState("viewed")
                           .respondNotificationTypeCollection(from)
                           .build())
                .build()
        );

        ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);
        sendNotificationService.updateSendNotificationState(MOCK_TOKEN, request);

        verify(caseService, times(1)).submitUpdate(
            eq(MOCK_TOKEN), eq("11"), contentCaptor.capture(), eq("1234"));

        CaseData data = (CaseData) contentCaptor.getValue().getData();
        Assertions.assertEquals(items, data.getSendNotificationCollection());
    }

    @Test
    void shouldUpdateSendNotificationStateNoResponses() {
        SendNotificationStateUpdateRequest request = testData.getSendNotificationStateUpdateRequest();

        StartEventResponse updateCaseEventResponse = updateCaseEventResponseNoNotificationResponses();
        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_NOTIFICATION_STATE
        )).thenReturn(updateCaseEventResponse);

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .notificationState("viewed")
                           .build())
                .build()
        );

        ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);
        sendNotificationService.updateSendNotificationState(MOCK_TOKEN, request);

        verify(caseService, times(1)).submitUpdate(
            eq(MOCK_TOKEN), eq("11"), contentCaptor.capture(), eq("1234"));

        CaseData data = (CaseData) contentCaptor.getValue().getData();
        Assertions.assertEquals(items, data.getSendNotificationCollection());
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

        ListTypeItem<RespondNotificationType> from = ListTypeItem.from(
            GenericTypeItem.from(ID, RespondNotificationType.builder()
                .state(VIEWED).isClaimantResponseDue(YES).build()
            )
        );

        PseResponseTypeItem build = PseResponseTypeItem.builder()
            .id(ID)
            .value(
                PseResponseType.builder()
                    .from(CLAIMANT)
                    .hasSupportingMaterial(NO)
                    .response("RESPONSE")
                    .build()
            ).build();

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .respondCollection(List.of(build))
                           .respondNotificationTypeCollection(from)
                           .build())
                .build()
        );

        sendNotificationService.addResponseSendNotification(MOCK_TOKEN, request);

        ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);

        verify(caseService, times(1)).submitUpdate(
            eq(MOCK_TOKEN), eq("11"), contentCaptor.capture(), eq("1234"));

        CaseData data = (CaseData) contentCaptor.getValue().getData();
        List<PseResponseTypeItem> expectedResponses = items.get(0).getValue().getRespondCollection();

        PseResponseType expected = expectedResponses.get(0).getValue();
        SendNotificationType notification = data.getSendNotificationCollection().get(0).getValue();
        PseResponseType actual = notification.getRespondCollection().get(0).getValue();
        var tribunalResponse = notification.getRespondNotificationTypeCollection().get(0);

        Assertions.assertEquals(expected.getResponse(), actual.getResponse());
        Assertions.assertEquals(expected.getFrom(), actual.getFrom());
        Assertions.assertTrue(actual.getDate().matches("^\\d{1,2} [A-Za-z]{3,4} \\d{4}$"));
        Assertions.assertEquals(NO, actual.getHasSupportingMaterial());
        Assertions.assertNull(tribunalResponse.getValue().getIsClaimantResponseDue());
    }

    @SuppressWarnings("unchecked")
    public StartEventResponse updateCaseEventResponseNoNotificationResponses() {
        StartEventResponse startEventResponse1 = ResourceLoader.fromString(
            "responses/updateCaseEventResponse.json",
            StartEventResponse.class
        );
        Object notifications = startEventResponse1.getCaseDetails().getData().get("sendNotificationCollection");
        ((List<LinkedHashMap<String, LinkedHashMap<String, Object>>>)notifications).get(0).get("value")
            .remove("respondNotificationTypeCollection");

        return startEventResponse1;
    }
}
