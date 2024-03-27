package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToTribunalRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

class StoredRespondToTribunalSubmitServiceTest {

    @MockBean
    private CaseService caseService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;
    @InjectMocks
    private StoredRespondToTribunalSubmitService storedRespondToTribunalSubmitService;

    private final TestData testData;

    private static final String RESPOND_ID_INCORRECT = "Respond id provided is incorrect";
    private static final String SEND_NOTIFICATION_ID_INCORRECT = "SendNotification Id is incorrect";

    private static final long CASE_ID = 1_646_225_213_651_590L;
    private static final String CASE_TYPE_ID = "ET_EnglandWales";
    private static final String ORDER_ID = "d20bbe0e-66a1-46e2-8073-727b5dd08b45";
    private static final String ORDER_RESPOND_ID = "667affe6-0de5-46b8-8ba9-8d695b6f8368";
    private static final String TEST = "Test";

    private StartEventResponse startEventResponse;

    StoredRespondToTribunalSubmitServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() {
        caseService = mock(CaseService.class);
        caseDetailsConverter = mock(CaseDetailsConverter.class);

        storedRespondToTribunalSubmitService = new StoredRespondToTribunalSubmitService(
            caseService,
            mock(NotificationService.class),
            caseDetailsConverter
        );

        startEventResponse = testData.getSendNotificationCollectionResponse();

        when(caseService.startUpdate(
            any(),
            any(),
            any(),
            any()
        )).thenReturn(testData.getUpdateCaseEventResponse());
    }

    @Test
    void submitRespondToTribunalShouldReturnCaseDetails() {
        UpdateStoredRespondToTribunalRequest testRequest = UpdateStoredRespondToTribunalRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .orderId(ORDER_ID)
            .respondId(ORDER_RESPOND_ID)
            .isRespondingToRequestOrOrder(true)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(startEventResponse);

        storedRespondToTribunalSubmitService.submitRespondToTribunal(TEST_SERVICE_AUTH_TOKEN, testRequest);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        PseResponseType actual =
            argumentCaptor.getValue().getSendNotificationCollection().get(0).getValue()
                .getRespondCollection().get(0).getValue();
        assertThat(actual.getDate()).isEqualTo(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getStatus()).isNull();
    }

    @Test
    void submitRespondToTribunalShouldOrderIdException() {
        UpdateStoredRespondToTribunalRequest testRequest = UpdateStoredRespondToTribunalRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .orderId(TEST)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(startEventResponse);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storedRespondToTribunalSubmitService.submitRespondToTribunal(TEST_SERVICE_AUTH_TOKEN, testRequest));
        assertThat(exception.getMessage())
            .isEqualTo(SEND_NOTIFICATION_ID_INCORRECT);
    }

    @Test
    void submitRespondToTribunalShouldRespondIdError() {
        UpdateStoredRespondToTribunalRequest testRequest = UpdateStoredRespondToTribunalRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .orderId(ORDER_ID)
            .respondId(TEST)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(startEventResponse);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storedRespondToTribunalSubmitService.submitRespondToTribunal(TEST_SERVICE_AUTH_TOKEN, testRequest));
        assertThat(exception.getMessage())
            .isEqualTo(RESPOND_ID_INCORRECT);
    }
}
