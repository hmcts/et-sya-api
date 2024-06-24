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
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VIEWED;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_NOTIFICATION_RESPONSE;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_NOTIFICATION_STATE;
import static uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper.CLAIMANT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

@SuppressWarnings({"PMD.TooManyMethods"})
@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {
    private TestData testData;
    private static final String MOCK_TOKEN = "Bearer TestServiceAuth";
    private static final String ID = "777";
    private static final String CASE_ID = "1234";
    private static final List<String> NOTIFICATION_SUBJECT_IS_ECC =
        List.of("Employer Contract Claim", "Case management orders / requests");
    private static final List<String> NOTIFICATION_SUBJECT_IS_NOT_ECC =
        List.of("Case management orders / requests");
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Mock
    private CaseService caseService;
    @Mock
    private CaseDocumentService caseDocumentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    IdamClient idamClient;

    private SendNotificationService sendNotificationService;

    @BeforeEach
    void beforeEach() {
        ObjectMapper objectMapper = new ObjectMapper();
        sendNotificationService = new SendNotificationService(
            caseService,
            caseDocumentService,
            new CaseDetailsConverter(objectMapper),
            notificationService,
            featureToggleService,
            idamClient
        );
        testData = new TestData();
    }

    @Test
    void shouldUpdateSendNotificationState() {
        SendNotificationStateUpdateRequest request = testData.getSendNotificationStateUpdateRequest();

        StartEventResponse updateCaseEventResponse = testData.getUpdateCaseEventResponseWithClaimantResponse();
        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_NOTIFICATION_STATE
        )).thenReturn(updateCaseEventResponse);

        List<PseResponseTypeItem> pseResponseItems = List.of(PseResponseTypeItem.builder().id(ID).value(
            PseResponseType.builder()
                .from(CLAIMANT)
                .hasSupportingMaterial(NO)
                .response("Some response text")
                .responseState(null)
                .build()).build()
        );

        ListTypeItem<RespondNotificationType> from = ListTypeItem.from(
            GenericTypeItem.from(ID, RespondNotificationType.builder()
                .state(VIEWED).isClaimantResponseDue(YES).build()
            )
        );

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .notificationState(VIEWED)
                           .respondNotificationTypeCollection(from)
                           .respondCollection(pseResponseItems)
                           .build())
                .build()
        );

        ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);
        sendNotificationService.updateSendNotificationState(MOCK_TOKEN, request);

        verify(caseService, times(1)).submitUpdate(
            eq(MOCK_TOKEN), eq("11"), contentCaptor.capture(), eq(CASE_ID));

        CaseData data = (CaseData) contentCaptor.getValue().getData();

        for (int i = 0; i < items.size(); i++) {
            SendNotificationTypeItem expectedItem = items.get(i);
            SendNotificationTypeItem actualItem = data.getSendNotificationCollection().get(i);

            Assertions.assertEquals(expectedItem.getId(), actualItem.getId());
            Assertions.assertEquals(
                expectedItem.getValue().getNotificationState(), actualItem.getValue().getNotificationState());

            for (int j = 0; j < actualItem.getValue().getRespondCollection().size(); j++) {
                PseResponseType actualResponseType = actualItem.getValue().getRespondCollection().get(j).getValue();
                Assertions.assertEquals(VIEWED, actualResponseType.getResponseState());
            }
        }
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
            eq(MOCK_TOKEN), eq("11"), contentCaptor.capture(), eq(CASE_ID));

        CaseData data = (CaseData) contentCaptor.getValue().getData();
        Assertions.assertEquals(items, data.getSendNotificationCollection());
    }

    @Test
    void shouldNotUpdateSendNotificationStateWhenNotificationDoesNotMatch() {
        SendNotificationStateUpdateRequest request = testData.getSendNotificationStateUpdateRequest();
        request.setSendNotificationId("222");

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
                           .notificationState(NOT_VIEWED_YET)
                           .build())
                .build()
        );

        ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);
        sendNotificationService.updateSendNotificationState(MOCK_TOKEN, request);
        verify(caseService, times(1)).submitUpdate(
            eq(MOCK_TOKEN), eq("11"), contentCaptor.capture(), eq(CASE_ID));

        CaseData data = (CaseData) contentCaptor.getValue().getData();
        Assertions.assertEquals(items, data.getSendNotificationCollection());
    }

    @Test
    void shouldNotUpdateSubmittedSendNotification() {
        SendNotificationStateUpdateRequest request = testData.getSendNotificationStateUpdateRequest();

        StartEventResponse updateCaseEventResponse = updateCaseEventResponseSubmittedNotificationNoResponses();
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
                           .notificationState(SUBMITTED)
                           .build())
                .build()
        );

        ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);
        sendNotificationService.updateSendNotificationState(MOCK_TOKEN, request);
        verify(caseService, times(1)).submitUpdate(
            eq(MOCK_TOKEN), eq("11"), contentCaptor.capture(), eq(CASE_ID));

        CaseData data = (CaseData) contentCaptor.getValue().getData();
        Assertions.assertEquals(items, data.getSendNotificationCollection());
    }

    @Test
    void shouldUpdateAddResponseSendNotification() {
        SendNotificationAddResponseRequest request = testData.getSendNotificationAddResponseRequest();

        StartEventResponse startEventResponse = testData.getUpdateCaseEventResponse();
        addNotificationSubject(startEventResponse, NOTIFICATION_SUBJECT_IS_ECC);

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_NOTIFICATION_RESPONSE
        )).thenReturn(startEventResponse);

        when(featureToggleService.isEccEnabled()).thenReturn(true);

        ListTypeItem<RespondNotificationType> from = ListTypeItem.from(
            GenericTypeItem.from(ID, RespondNotificationType.builder()
                .state(VIEWED).isClaimantResponseDue(YES).build()
            )
        );

        PseResponseTypeItem buildResponse = PseResponseTypeItem.builder()
            .id(ID)
            .value(
                PseResponseType.builder()
                    .from(CLAIMANT)
                    .response("RESPONSE")
                    .hasSupportingMaterial(NO)
                    .build()
            ).build();

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .sendNotificationSubject(NOTIFICATION_SUBJECT_IS_ECC)
                           .respondCollection(List.of(buildResponse))
                           .respondNotificationTypeCollection(from)
                           .build())
                .build()
        );

        sendNotificationService.addResponseSendNotification(MOCK_TOKEN, request);

        ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);

        verify(caseService, times(1)).submitUpdate(
            eq(MOCK_TOKEN), eq("11"), contentCaptor.capture(), eq(CASE_ID));

        CaseData data = (CaseData) contentCaptor.getValue().getData();
        List<PseResponseTypeItem> expectedResponses = items.get(0).getValue().getRespondCollection();
        PseResponseType expected = expectedResponses.get(0).getValue();

        SendNotificationType notification = data.getSendNotificationCollection().get(0).getValue();
        PseResponseType actual = notification.getRespondCollection().get(0).getValue();
        GenericTypeItem<RespondNotificationType> tribunalResponse =
            notification.getRespondNotificationTypeCollection().get(0);

        Assertions.assertEquals(expected.getResponse(), actual.getResponse());
        Assertions.assertEquals(expected.getFrom(), actual.getFrom());
        Assertions.assertTrue(actual.getDate().matches("^\\d{1,2} [A-Za-z]{3,4} \\d{4}$"));
        Assertions.assertEquals(NO, actual.getHasSupportingMaterial());
        Assertions.assertEquals(SUBMITTED, notification.getNotificationState());
        Assertions.assertEquals(SUBMITTED, tribunalResponse.getValue().getState());
        Assertions.assertNull(tribunalResponse.getValue().getIsClaimantResponseDue());

        Assertions.assertEquals(YES, actual.getIsECC());
        assertDoesNotThrow(() -> LocalDateTime.parse(actual.getDateTime(), formatter));
    }

    @Test
    void shouldUpdateAddResponseSendNotificationNoResponsesOutstanding() {
        SendNotificationAddResponseRequest request = testData.getSendNotificationAddResponseRequest();

        StartEventResponse startEventResponse = updateCaseEventResponseSubmittedNotification();
        addNotificationSubject(startEventResponse, NOTIFICATION_SUBJECT_IS_NOT_ECC);

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_NOTIFICATION_RESPONSE
        )).thenReturn(startEventResponse);

        when(featureToggleService.isEccEnabled()).thenReturn(true);

        ListTypeItem<RespondNotificationType> from = ListTypeItem.from(
            GenericTypeItem.from(ID, RespondNotificationType.builder()
                .state(VIEWED).build()
            )
        );

        PseResponseTypeItem buildResponse = PseResponseTypeItem.builder()
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
                           .sendNotificationSubject(NOTIFICATION_SUBJECT_IS_NOT_ECC)
                           .respondCollection(List.of(buildResponse))
                           .respondNotificationTypeCollection(from)
                           .build())
                .build()
        );

        sendNotificationService.addResponseSendNotification(MOCK_TOKEN, request);

        ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);

        verify(caseService, times(1)).submitUpdate(
            eq(MOCK_TOKEN), eq("11"), contentCaptor.capture(), eq(CASE_ID));

        CaseData data = (CaseData) contentCaptor.getValue().getData();
        List<PseResponseTypeItem> expectedResponses = items.get(0).getValue().getRespondCollection();
        PseResponseType expected = expectedResponses.get(0).getValue();

        SendNotificationType notification = data.getSendNotificationCollection().get(0).getValue();
        PseResponseType actual = notification.getRespondCollection().get(0).getValue();
        GenericTypeItem<RespondNotificationType> tribunalResponse =
            notification.getRespondNotificationTypeCollection().get(0);

        Assertions.assertEquals(expected.getResponse(), actual.getResponse());
        Assertions.assertEquals(expected.getFrom(), actual.getFrom());
        Assertions.assertTrue(actual.getDate().matches("^\\d{1,2} [A-Za-z]{3,4} \\d{4}$"));
        Assertions.assertEquals(NO, actual.getHasSupportingMaterial());
        Assertions.assertEquals(SUBMITTED, notification.getNotificationState());
        Assertions.assertNull(tribunalResponse.getValue().getIsClaimantResponseDue());

        Assertions.assertEquals(NO, actual.getIsECC());
        assertDoesNotThrow(() -> LocalDateTime.parse(actual.getDateTime(), formatter));
    }

    @Test
    void shouldNotUpdateAddResponseWhenNone() {
        SendNotificationAddResponseRequest request = testData.getSendNotificationAddResponseRequest();
        StartEventResponse updateCaseEventResponse = updateCaseEventResponseNoNotificationResponses();
        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_NOTIFICATION_RESPONSE
        )).thenReturn(updateCaseEventResponse);

        when(featureToggleService.isEccEnabled()).thenReturn(true);

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .build())
                .build()
        );

        sendNotificationService.addResponseSendNotification(MOCK_TOKEN, request);

        ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);

        verify(caseService, times(1)).submitUpdate(
            eq(MOCK_TOKEN), eq("11"), contentCaptor.capture(), eq(CASE_ID));

        List<PseResponseTypeItem> expectedResponses = items.get(0).getValue().getRespondCollection();

        Assertions.assertNull(expectedResponses);
    }


    public StartEventResponse updateCaseEventResponseNoNotificationResponses() {
        StartEventResponse startEventResponse1 = ResourceLoader.fromString(
            "responses/updateCaseEventResponse.json",
            StartEventResponse.class
        );
        removeResponses(startEventResponse1);
        return startEventResponse1;
    }

    public StartEventResponse updateCaseEventResponseSubmittedNotificationNoResponses() {
        StartEventResponse startEventResponse1 = updateCaseEventResponseSubmittedNotification();
        removeResponses(startEventResponse1);
        return startEventResponse1;
    }

    private StartEventResponse updateCaseEventResponseSubmittedNotification() {
        return ResourceLoader.fromString(
            "responses/updateCaseEventResponseSubmittedNotification.json",
            StartEventResponse.class
        );
    }

    @SuppressWarnings("unchecked")
    private static void removeResponses(StartEventResponse startEventResponse1) {
        Object notifications = startEventResponse1.getCaseDetails().getData().get("sendNotificationCollection");
        ((List<LinkedHashMap<String, LinkedHashMap<String, Object>>>) notifications).get(0).get("value")
            .remove("respondNotificationTypeCollection");
    }

    @SuppressWarnings("unchecked")
    private static void addNotificationSubject(
        StartEventResponse startEventResponse1, List<String> notificationSubject) {
        Object notifications = startEventResponse1.getCaseDetails().getData().get("sendNotificationCollection");
        ((List<LinkedHashMap<String, LinkedHashMap<String, Object>>>) notifications).get(0).get("value")
            .put("sendNotificationSubject", notificationSubject);
    }
}
