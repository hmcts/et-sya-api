package uk.gov.hmcts.reform.et.syaapi.service;

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
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.ChangeRespondentNotificationStatusRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationAddResponseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationStateUpdateRequest;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VIEWED;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_NOTIFICATION_RESPONSE;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_NOTIFICATION_STATE;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_RESPONDENT_NOTIFICATION_STATE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {
    private static final String AUTHOR = "Barry White";
    private TestData testData;
    private static final String MOCK_TOKEN = "Bearer TestServiceAuth";
    private static final String ID = "777";
    private static final String CASE_ID = "1234";
    private static final String CASE_TYPE = "ET_EnglandWales";
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
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private NotificationService notificationService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    IdamClient idamClient;

    private SendNotificationService sendNotificationService;

    @BeforeEach
    void beforeEach() {
        sendNotificationService = new SendNotificationService(
            caseService,
            caseDocumentService,
            caseDetailsConverter,
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
                .from(CLAIMANT_TITLE)
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

        sendNotificationService.updateSendNotificationState(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

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

        for (int i = 0; i < items.size(); i++) {
            SendNotificationTypeItem expectedItem = items.get(i);
            SendNotificationTypeItem actualItem = data.getSendNotificationCollection().get(i);

            assertEquals(expectedItem.getId(), actualItem.getId());
            assertEquals(
                expectedItem.getValue().getNotificationState(), actualItem.getValue().getNotificationState());

            for (int j = 0; j < actualItem.getValue().getRespondCollection().size(); j++) {
                PseResponseType actualResponseType = actualItem.getValue().getRespondCollection().get(j).getValue();
                assertEquals(VIEWED, actualResponseType.getResponseState());
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

        sendNotificationService.updateSendNotificationState(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .notificationState("viewed")
                           .build())
                .build()
        );
        assertEquals(items, data.getSendNotificationCollection());
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

        sendNotificationService.updateSendNotificationState(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .notificationState(NOT_VIEWED_YET)
                           .build())
                .build()
        );
        assertEquals(items, data.getSendNotificationCollection());
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

        sendNotificationService.updateSendNotificationState(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .notificationState(SUBMITTED)
                           .build())
                .build()
        );
        assertEquals(items, data.getSendNotificationCollection());
    }

    @Test
    void shouldUpdateAddClaimantResponseNotification() {
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(
            UserInfo.builder()
                .name(AUTHOR)
                .build());
        when(featureToggleService.isMultiplesEnabled()).thenReturn(true);

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

        sendNotificationService.addClaimantResponseNotification(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        PseResponseTypeItem buildResponse = PseResponseTypeItem.builder()
            .id(ID)
            .value(
                PseResponseType.builder()
                    .from(CLAIMANT_TITLE)
                    .response("RESPONSE")
                    .hasSupportingMaterial(NO)
                    .build()
            ).build();

        ListTypeItem<RespondNotificationType> from = ListTypeItem.from(
            GenericTypeItem.from(ID, RespondNotificationType.builder()
                .state(VIEWED).isClaimantResponseDue(YES).build()
            )
        );

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
        List<PseResponseTypeItem> expectedResponses = items.getFirst().getValue().getRespondCollection();
        PseResponseType expected = expectedResponses.getFirst().getValue();

        SendNotificationType notification = data.getSendNotificationCollection().getFirst().getValue();
        PseResponseType actual = notification.getRespondCollection().getFirst().getValue();

        assertEquals(expected.getResponse(), actual.getResponse());
        assertEquals(expected.getFrom(), actual.getFrom());
        assertTrue(actual.getDate().matches("^\\d{1,2} [A-Za-z]{3,4} \\d{4}$"));
        assertEquals(NO, actual.getHasSupportingMaterial());
        assertEquals(SUBMITTED, notification.getNotificationState());

        GenericTypeItem<RespondNotificationType> tribunalResponse =
            notification.getRespondNotificationTypeCollection().getFirst();
        assertEquals(SUBMITTED, tribunalResponse.getValue().getState());
        assertNull(tribunalResponse.getValue().getIsClaimantResponseDue());
        assertEquals(AUTHOR, actual.getAuthor());

        assertEquals(YES, actual.getIsECC());
        assertDoesNotThrow(() -> LocalDateTime.parse(actual.getDateTime(), formatter));
    }

    @Test
    void shouldUpdateAddClaimantResponseNotificationNoResponsesOutstanding() {
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

        sendNotificationService.addClaimantResponseNotification(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        PseResponseTypeItem buildResponse = PseResponseTypeItem.builder()
            .id(ID)
            .value(
                PseResponseType.builder()
                    .from(CLAIMANT_TITLE)
                    .hasSupportingMaterial(YES)
                    .response("RESPONSE")
                    .build()
            ).build();

        ListTypeItem<RespondNotificationType> from = ListTypeItem.from(
            GenericTypeItem.from(ID, RespondNotificationType.builder()
                .state(VIEWED).build()
            )
        );

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
        List<PseResponseTypeItem> expectedResponses = items.getFirst().getValue().getRespondCollection();
        PseResponseType expected = expectedResponses.getFirst().getValue();

        SendNotificationType notification = data.getSendNotificationCollection().getFirst().getValue();
        PseResponseType actual = notification.getRespondCollection().getFirst().getValue();

        assertEquals(expected.getResponse(), actual.getResponse());
        assertEquals(expected.getFrom(), actual.getFrom());
        assertTrue(actual.getDate().matches("^\\d{1,2} [A-Za-z]{3,4} \\d{4}$"));
        assertEquals(NO, actual.getHasSupportingMaterial());
        assertEquals(SUBMITTED, notification.getNotificationState());

        GenericTypeItem<RespondNotificationType> tribunalResponse =
            notification.getRespondNotificationTypeCollection().getFirst();
        assertNull(tribunalResponse.getValue().getIsClaimantResponseDue());

        assertEquals(NO, actual.getIsECC());
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

        sendNotificationService.addClaimantResponseNotification(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        List<SendNotificationTypeItem> items = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .build())
                .build()
        );
        List<PseResponseTypeItem> expectedResponses = items.getFirst().getValue().getRespondCollection();

        assertNull(expectedResponses);
    }

    @Test
    void shouldAddRespondentNotificationStatus() {
        ChangeRespondentNotificationStatusRequest request = ChangeRespondentNotificationStatusRequest.builder()
            .caseId(CASE_ID)
            .caseTypeId(CASE_TYPE)
            .notificationId("777")
            .userIdamId("e67fae0a-7a75-4f45-abc8-6f9d2a79801f")
            .newStatus("viewed")
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_RESPONDENT_NOTIFICATION_STATE
        )).thenReturn(testData.getUpdateCaseEventResponse());

        sendNotificationService.changeRespondentNotificationStatus(TEST_SERVICE_AUTH_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        String actualState = argumentCaptor.getValue()
            .getSendNotificationCollection().getFirst().getValue()
            .getRespondentState().getFirst().getValue()
            .getNotificationState();
        assertThat(actualState).isEqualTo("viewed");
    }

    @Test
    void shouldThrowException_whenNotificationIdIsIncorrect() {
        ChangeRespondentNotificationStatusRequest request = ChangeRespondentNotificationStatusRequest.builder()
            .caseId(CASE_ID)
            .caseTypeId(CASE_TYPE)
            .notificationId("invalidAppId")
            .userIdamId("e67fae0a-7a75-4f45-abc8-6f9d2a79801f")
            .newStatus("viewed")
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_RESPONDENT_NOTIFICATION_STATE)
        ).thenReturn(testData.getUpdateCaseEventResponse());

        assertThatThrownBy(() -> sendNotificationService
            .changeRespondentNotificationStatus(TEST_SERVICE_AUTH_TOKEN, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Notification id provided is incorrect");
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
        ((List<LinkedHashMap<String, LinkedHashMap<String, Object>>>) notifications).getFirst().get("value")
            .remove("respondNotificationTypeCollection");
    }

    @SuppressWarnings("unchecked")
    private static void addNotificationSubject(
        StartEventResponse startEventResponse1, List<String> notificationSubject) {
        Object notifications = startEventResponse1.getCaseDetails().getData().get("sendNotificationCollection");
        ((List<LinkedHashMap<String, LinkedHashMap<String, Object>>>) notifications).getFirst().get("value")
            .put("sendNotificationSubject", notificationSubject);
    }
}
