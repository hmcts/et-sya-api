package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.ChangeRespondentNotificationStatusRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_RESPONDENT_NOTIFICATION_STATE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SendNotificationRespondentServiceTest {
    private TestData testData;
    private static final String CASE_ID = "1234";
    private static final String CASE_TYPE = "ET_EnglandWales";

    @Mock
    private CaseService caseService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private SendNotificationRespondentService sendNotificationRespondentService;

    @BeforeEach
    void beforeEach() {
        sendNotificationRespondentService = new SendNotificationRespondentService(
            caseService,
            caseDetailsConverter
        );
        testData = new TestData();
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

        sendNotificationRespondentService.changeRespondentNotificationStatus(TEST_SERVICE_AUTH_TOKEN, request);

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

        assertThatThrownBy(() -> sendNotificationRespondentService
            .changeRespondentNotificationStatus(TEST_SERVICE_AUTH_TOKEN, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Notification id provided is incorrect");
    }
}
