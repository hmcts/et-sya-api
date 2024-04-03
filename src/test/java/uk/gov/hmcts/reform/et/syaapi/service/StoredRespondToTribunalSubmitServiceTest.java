package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationAddResponseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToTribunalRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper.CLAIMANT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

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
    private static final String STORED_RESPOND_ID = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
    private static final String TEST = "Test";

    StoredRespondToTribunalSubmitServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() {
        caseService = mock(CaseService.class);
        caseDetailsConverter = mock(CaseDetailsConverter.class);

        storedRespondToTribunalSubmitService = new StoredRespondToTribunalSubmitService(
            caseService,
            mock(CaseDocumentService.class),
            caseDetailsConverter,
            mock(NotificationService.class)
        );

        when(caseService.startUpdate(
            any(),
            any(),
            any(),
            any()
        )).thenReturn(testData.getUpdateCaseEventResponse());
    }

    @Test
    void storeRespondToTribunalShouldReturnCaseDetails() {
        PseResponseType pseResponseType = PseResponseType.builder()
            .from(CLAIMANT)
            .hasSupportingMaterial(YES)
            .response("Response Text")
            .build();

        SendNotificationAddResponseRequest request = SendNotificationAddResponseRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .sendNotificationId(ORDER_ID)
            .pseResponseType(pseResponseType)
            .supportingMaterialFile(UploadedDocumentType.builder().build())
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.STORE_PSE_RESPONSE
        )).thenReturn(testData.getSendNotificationCollectionResponse());

        storedRespondToTribunalSubmitService.storeResponseSendNotification(TEST_SERVICE_AUTH_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        SendNotificationType notification = argumentCaptor.getValue().getSendNotificationCollection().get(0).getValue();
        int responseIndex = notification.getRespondStoredCollection().size() - 1;
        PseResponseType actual = notification.getRespondStoredCollection().get(responseIndex).getValue();

        assertThat(actual.getDate()).isEqualTo(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getResponse()).isEqualTo("Response Text");
        assertThat(actual.getFrom()).isEqualTo(CLAIMANT);
        assertThat(notification.getRespondNotificationTypeCollection().get(0).getValue().getIsClaimantResponseDue()).isNull();
    }

    @Test
    void storeRespondToTribunalShouldOrderIdException() {
        SendNotificationAddResponseRequest request = SendNotificationAddResponseRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .sendNotificationId(TEST)
            .pseResponseType(PseResponseType.builder().build())
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.STORE_PSE_RESPONSE
        )).thenReturn(testData.getSendNotificationCollectionResponse());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storedRespondToTribunalSubmitService.storeResponseSendNotification(TEST_SERVICE_AUTH_TOKEN, request));

        assertThat(exception.getMessage())
            .isEqualTo(SEND_NOTIFICATION_ID_INCORRECT);
    }

    @Test
    void submitRespondToTribunalShouldReturnCaseDetails() {
        UpdateStoredRespondToTribunalRequest request = UpdateStoredRespondToTribunalRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .orderId(ORDER_ID)
            .storedRespondId(STORED_RESPOND_ID)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.SUBMIT_STORED_PSE_RESPONSE
        )).thenReturn(testData.getSendNotificationCollectionResponse());

        storedRespondToTribunalSubmitService.submitRespondToTribunal(TEST_SERVICE_AUTH_TOKEN, request);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        SendNotificationType actual = argumentCaptor.getValue().getSendNotificationCollection().get(0).getValue();
        assertThat(actual.getRespondCollection()).hasSize(2);

        int responseIndex = actual.getRespondCollection().size() - 1;
        PseResponseType actualResponse = actual.getRespondCollection().get(responseIndex).getValue();
        assertThat(actualResponse.getDate()).isEqualTo(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
    }

    @Test
    void submitRespondToTribunalShouldOrderIdException() {
        UpdateStoredRespondToTribunalRequest request = UpdateStoredRespondToTribunalRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .orderId(TEST)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.SUBMIT_STORED_PSE_RESPONSE
        )).thenReturn(testData.getSendNotificationCollectionResponse());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storedRespondToTribunalSubmitService.submitRespondToTribunal(TEST_SERVICE_AUTH_TOKEN, request));

        assertThat(exception.getMessage())
            .isEqualTo(SEND_NOTIFICATION_ID_INCORRECT);
    }

    @Test
    void submitRespondToTribunalShouldRespondIdError() {
        UpdateStoredRespondToTribunalRequest request = UpdateStoredRespondToTribunalRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .orderId(ORDER_ID)
            .storedRespondId(TEST)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.SUBMIT_STORED_PSE_RESPONSE
        )).thenReturn(testData.getSendNotificationCollectionResponse());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storedRespondToTribunalSubmitService.submitRespondToTribunal(TEST_SERVICE_AUTH_TOKEN, request));

        assertThat(exception.getMessage())
            .isEqualTo(RESPOND_ID_INCORRECT);
    }
}
