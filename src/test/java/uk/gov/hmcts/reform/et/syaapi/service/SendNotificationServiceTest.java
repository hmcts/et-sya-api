package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.ChangeRespondentNotificationStatusRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationAddResponseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationStateUpdateRequest;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VIEWED;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.ADD_RESPONDENT_NOTIFICATION_RESPONSE;
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
    void shouldUpdateClaimantSendNotificationState() {
        SendNotificationStateUpdateRequest request = testData.getSendNotificationStateUpdateRequest();

        StartEventResponse updateCaseEventResponse = testData.getUpdateCaseEventResponseWithClaimantResponse();
        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_NOTIFICATION_STATE
        )).thenReturn(updateCaseEventResponse);

        sendNotificationService.updateClaimantSendNotificationState(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        assertEquals(1, data.getSendNotificationCollection().size());

        SendNotificationTypeItem actualItem = data.getSendNotificationCollection().getFirst();
        assertEquals(ID, actualItem.getId());
        assertEquals(VIEWED, actualItem.getValue().getNotificationState());

        for (int j = 0; j < actualItem.getValue().getRespondCollection().size(); j++) {
            PseResponseType actualResponseType = actualItem.getValue().getRespondCollection().get(j).getValue();
            assertEquals(VIEWED, actualResponseType.getResponseState());
        }
    }

    @Test
    void shouldUpdateClaimantSendNotificationStateNoResponses() {
        SendNotificationStateUpdateRequest request = testData.getSendNotificationStateUpdateRequest();

        StartEventResponse updateCaseEventResponse = updateCaseEventResponseNoNotificationResponses();
        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_NOTIFICATION_STATE
        )).thenReturn(updateCaseEventResponse);

        sendNotificationService.updateClaimantSendNotificationState(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        List<SendNotificationTypeItem> expected = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .notificationState(VIEWED)
                           .build())
                .build()
        );
        assertEquals(expected, data.getSendNotificationCollection());
    }

    @Test
    void shouldNotUpdateClaimantSendNotificationStateWhenNotificationDoesNotMatch() {
        SendNotificationStateUpdateRequest request = testData.getSendNotificationStateUpdateRequest();
        request.setSendNotificationId("222");

        StartEventResponse updateCaseEventResponse = updateCaseEventResponseNoNotificationResponses();
        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_NOTIFICATION_STATE
        )).thenReturn(updateCaseEventResponse);

        sendNotificationService.updateClaimantSendNotificationState(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        List<SendNotificationTypeItem> expected = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .notificationState(NOT_VIEWED_YET)
                           .build())
                .build()
        );
        assertEquals(expected, data.getSendNotificationCollection());
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

        sendNotificationService.updateClaimantSendNotificationState(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        List<SendNotificationTypeItem> expected = List.of(
            SendNotificationTypeItem.builder()
                .id(ID)
                .value(SendNotificationType.builder()
                           .notificationState(SUBMITTED)
                           .build())
                .build()
        );
        assertEquals(expected, data.getSendNotificationCollection());
    }

    @Test
    void shouldUpdateAddClaimantResponseSendNotification() {
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

        sendNotificationService.addClaimantResponseSendNotification(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        SendNotificationType notification = data.getSendNotificationCollection().getFirst().getValue();
        PseResponseType actual = notification.getRespondCollection().getFirst().getValue();

        assertEquals("RESPONSE", actual.getResponse());
        assertEquals(CLAIMANT_TITLE, actual.getFrom());
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
    void shouldUpdateAddClaimantResponseSendNotificationNoResponsesOutstanding() {
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

        sendNotificationService.addClaimantResponseSendNotification(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        SendNotificationType notification = data.getSendNotificationCollection().getFirst().getValue();
        PseResponseType actual = notification.getRespondCollection().getFirst().getValue();

        assertEquals("RESPONSE", actual.getResponse());
        assertEquals(CLAIMANT_TITLE, actual.getFrom());
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
    void shouldNotUpdateAddResponseWhenIdNotMatch() {
        SendNotificationAddResponseRequest request = testData.getSendNotificationAddResponseRequest();
        request.setSendNotificationId("test");

        StartEventResponse updateCaseEventResponse = updateCaseEventResponseNoNotificationResponses();
        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_NOTIFICATION_RESPONSE
        )).thenReturn(updateCaseEventResponse);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> sendNotificationService.addClaimantResponseSendNotification(MOCK_TOKEN, request)
        );

        assertEquals("SendNotification Id is incorrect", exception.getMessage());
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

        sendNotificationService.updateRespondentNotificationStatus(TEST_SERVICE_AUTH_TOKEN, request);

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
                .updateRespondentNotificationStatus(TEST_SERVICE_AUTH_TOKEN, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Notification id provided is incorrect");
    }

    @Test
    void shouldAddRespondentResponseNotification() {
        SendNotificationAddResponseRequest request = testData.getSendNotificationAddResponseRequest();
        StartEventResponse startEventResponse = testData.getUpdateCaseEventResponse();

        when(caseService.startUpdate(
                TEST_SERVICE_AUTH_TOKEN,
                request.getCaseId(),
                request.getCaseTypeId(),
                ADD_RESPONDENT_NOTIFICATION_RESPONSE
        )).thenReturn(startEventResponse);

        sendNotificationService.addRespondentResponseNotification(MOCK_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        SendNotificationType actualNotification = data.getSendNotificationCollection().getFirst().getValue();
        PseResponseType actualResponse = actualNotification.getRespondCollection().getFirst().getValue();

        assertEquals("RESPONSE", actualResponse.getResponse());
        assertEquals(RESPONDENT_TITLE, actualResponse.getFrom());
        assertEquals(TseApplicationHelper.formatCurrentDate(LocalDate.now()), actualResponse.getDate());
        assertDoesNotThrow(() -> LocalDateTime.parse(actualResponse.getDateTime(), formatter));
        assertEquals(NO, actualResponse.getHasSupportingMaterial());
        assertEquals(SUBMITTED, actualNotification.getRespondentState().getFirst().getValue().getNotificationState());
    }
}
