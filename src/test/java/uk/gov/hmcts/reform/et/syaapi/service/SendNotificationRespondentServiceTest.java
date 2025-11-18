package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.ChangeRespondentNotificationStatusRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationAddResponseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitRespondentPseRespondRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.ADD_RESPONDENT_PSE_RESPONSE;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.STORE_RESPONDENT_PSE_RESPONSE;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_RESPONDENT_PSE_STATE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SendNotificationRespondentServiceTest {
    @Mock
    private CaseService caseService;
    @Mock
    private CaseDocumentService caseDocumentService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private NotificationService notificationService;

    private SendNotificationRespondentService sendNotificationRespondentService;

    private TestData testData;
    private static final String CASE_ID = "1234";
    DateTimeFormatter formatter = ISO_LOCAL_DATE_TIME;

    @BeforeEach
    void beforeEach() {
        sendNotificationRespondentService = new SendNotificationRespondentService(
            caseService,
            caseDetailsConverter,
            caseDocumentService,
            notificationService
        );
        testData = new TestData();
    }

    @Test
    void shouldAddRespondentNotificationStatus() {
        ChangeRespondentNotificationStatusRequest request = ChangeRespondentNotificationStatusRequest.builder()
            .caseId(CASE_ID)
            .caseTypeId(ENGLAND_CASE_TYPE)
            .notificationId("777")
            .userIdamId("e67fae0a-7a75-4f45-abc8-6f9d2a79801f")
            .newStatus("viewed")
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_RESPONDENT_PSE_STATE
        )).thenReturn(testData.getUpdateCaseEventResponse());

        sendNotificationRespondentService.updateRespondentNotificationStatus(TEST_SERVICE_AUTH_TOKEN, request);

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
            .caseTypeId(ENGLAND_CASE_TYPE)
            .notificationId("invalidAppId")
            .userIdamId("e67fae0a-7a75-4f45-abc8-6f9d2a79801f")
            .newStatus("viewed")
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            UPDATE_RESPONDENT_PSE_STATE)
        ).thenReturn(testData.getUpdateCaseEventResponse());

        assertThatThrownBy(() -> sendNotificationRespondentService
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
            ADD_RESPONDENT_PSE_RESPONSE
        )).thenReturn(startEventResponse);

        sendNotificationRespondentService.addRespondentResponseNotification(TEST_SERVICE_AUTH_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        SendNotificationType actualNotification = data.getSendNotificationCollection().getFirst().getValue();
        PseResponseType actualResponse = actualNotification.getRespondCollection().getFirst().getValue();

        assertThat(actualResponse.getResponse()).isEqualTo("RESPONSE");
        assertThat(actualResponse.getFrom()).isEqualTo(RESPONDENT_TITLE);
        assertThat(actualResponse.getDate()).isEqualTo(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        assertDoesNotThrow(() -> LocalDateTime.parse(actualResponse.getDateTime(), formatter));
        assertThat(actualResponse.getHasSupportingMaterial()).isEqualTo(NO);
        assertThat(actualNotification.getRespondentState().getFirst().getValue().getNotificationState())
            .isEqualTo(SUBMITTED);
    }

    @Test
    void shouldStoreResponseSendNotification() {
        SendNotificationAddResponseRequest request = testData.getSendNotificationAddResponseRequest();
        StartEventResponse startEventResponse = testData.getUpdateCaseEventResponse();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            STORE_RESPONDENT_PSE_RESPONSE
        )).thenReturn(startEventResponse);

        sendNotificationRespondentService.storeResponseSendNotification(TEST_SERVICE_AUTH_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());
        CaseData data = argumentCaptor.getValue();

        SendNotificationType actualNotification = data.getSendNotificationCollection().getFirst().getValue();
        PseResponseType actualResponse =
            actualNotification.getRespondentRespondStoredCollection().getFirst().getValue();

        assertThat(actualResponse.getResponse()).isEqualTo("RESPONSE");
        assertThat(actualResponse.getFrom()).isEqualTo(RESPONDENT_TITLE);
        assertThat(actualResponse.getDate()).isEqualTo(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        assertDoesNotThrow(() -> LocalDateTime.parse(actualResponse.getDateTime(), formatter));
        assertThat(actualResponse.getHasSupportingMaterial()).isEqualTo(NO);
        assertThat(actualNotification.getRespondentState().getFirst().getValue().getNotificationState())
            .isEqualTo(STORED);
    }

    @Test
    void submitRespondToTribunalShouldReturnCaseDetails() {
        SubmitRespondentPseRespondRequest request = SubmitRespondentPseRespondRequest.builder()
            .caseId(CASE_ID)
            .caseTypeId(ENGLAND_CASE_TYPE)
            .fromIdamId("550e8400-e29b-41d4-a716-446655440000")
            .notificationId("d20bbe0e-66a1-46e2-8073-727b5dd08b45")
            .storedRespondId("9b1deb4d-3b7d-4d6f-9b7f-9e8a2c4d5e6f")
            .build();

        StartEventResponse startEventResponse = testData.getSendNotificationCollectionResponse();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.SUBMIT_RESPONDENT_PSE_RESPONSE
        )).thenReturn(startEventResponse);

        sendNotificationRespondentService.submitRespondToTribunal(TEST_SERVICE_AUTH_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        SendNotificationType actual = argumentCaptor.getValue().getSendNotificationCollection().getFirst().getValue();
        assertThat(actual.getRespondCollection()).hasSize(2);

        int responseIndex = actual.getRespondCollection().size() - 1;
        PseResponseType actualResponse = actual.getRespondCollection().get(responseIndex).getValue();
        assertThat(actualResponse.getDate()).isEqualTo(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        assertDoesNotThrow(() -> LocalDateTime.parse(actualResponse.getDateTime(), formatter));
    }
}
