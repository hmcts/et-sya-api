package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredRespondToApplicationRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

class StoredRespondToApplicationServiceTest {

    @MockBean
    private CaseService caseService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private CaseDocumentService caseDocumentService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;
    @InjectMocks
    private StoredRespondToApplicationService storedRespondToApplicationService;

    private final TestData testData;

    private static final String APP_ID_INCORRECT = "Application id provided is incorrect";
    private static final String RESPOND_ID_INCORRECT = "Respond id provided is incorrect";

    private static final long CASE_ID = 1_646_225_213_651_590L;
    private static final String CASE_TYPE_ID = "ET_EnglandWales";
    private static final String APP_ID = "3be1ea83-06ef-40ff-bda5-e2ae88998a18";
    private static final String APP_RESPOND_ID = "dee74336-ca55-4856-b208-fad0d418e88b";
    private static final String TEST = "Test";

    StoredRespondToApplicationServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() {
        caseService = mock(CaseService.class);
        notificationService = mock(NotificationService.class);
        caseDocumentService = mock(CaseDocumentService.class);
        caseDetailsConverter = mock(CaseDetailsConverter.class);

        storedRespondToApplicationService = new StoredRespondToApplicationService(
            caseService,
            notificationService,
            caseDocumentService,
            caseDetailsConverter
        );

        when(caseService.startUpdate(
            any(),
            any(),
            any(),
            any()
        )).thenReturn(testData.getUpdateCaseEventResponse());
    }

    @Test
    void storeRespondToApplicationShouldReturnCaseDetails() {
        RespondToApplicationRequest request = testData.getRespondToApplicationRequest();
        request.getResponse().setCopyToOtherParty(YES);
        request.getResponse().setCopyNoGiveDetails(null);

        DocumentTypeItem docType = DocumentTypeItem.builder().id("1").value(new DocumentType()).build();
        when(caseDocumentService.createDocumentTypeItem(any(), any())).thenReturn(docType);

        storedRespondToApplicationService.storeRespondToApplication(TEST_SERVICE_AUTH_TOKEN, request);

        ArgumentCaptor<NotificationService.CoreEmailDetails> argument =
            ArgumentCaptor.forClass(NotificationService.CoreEmailDetails.class);
        verify(notificationService, times(0)).sendAcknowledgementEmailToTribunal(
            argument.capture(),
            any()
        );
        verify(notificationService, times(1)).sendStoredEmailToClaimant(
            argument.capture(),
            any()
        );
    }

    @Test
    void storeRespondToApplicationShouldApplicationIdException() {
        RespondToApplicationRequest testRequest = RespondToApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .applicationId(TEST)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.STORE_CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getSendNotificationCollectionResponse());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storedRespondToApplicationService.storeRespondToApplication(TEST_SERVICE_AUTH_TOKEN, testRequest));

        assertThat(exception.getMessage())
            .isEqualTo(APP_ID_INCORRECT);
    }

    @Test
    void submitRespondToApplicationShouldReturnCaseDetails() {
        SubmitStoredRespondToApplicationRequest testRequest = SubmitStoredRespondToApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .applicationId(APP_ID)
            .storedRespondId(APP_RESPOND_ID)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.SUBMIT_STORED_CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getSendNotificationCollectionResponse());

        storedRespondToApplicationService.submitRespondToApplication(TEST_SERVICE_AUTH_TOKEN, testRequest);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        GenericTseApplicationType actual =
            argumentCaptor.getValue().getGenericTseApplicationCollection().get(0).getValue();
        assertThat(actual.getRespondCollection()).hasSize(3);
        assertThat(actual.getResponsesCount()).isEqualTo("3");

        int responseIndex = actual.getRespondCollection().size() - 1;
        TseRespondType actualRespond = actual.getRespondCollection().get(responseIndex).getValue();
        assertThat(actualRespond.getDate()).isEqualTo(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
    }

    @Test
    void submitRespondToApplicationShouldApplicationIdException() {
        SubmitStoredRespondToApplicationRequest testRequest = SubmitStoredRespondToApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .applicationId(TEST)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.SUBMIT_STORED_CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getSendNotificationCollectionResponse());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storedRespondToApplicationService.submitRespondToApplication(TEST_SERVICE_AUTH_TOKEN, testRequest));

        assertThat(exception.getMessage())
            .isEqualTo(APP_ID_INCORRECT);
    }

    @Test
    void submitRespondToTribunalShouldRespondIdError() {
        SubmitStoredRespondToApplicationRequest testRequest = SubmitStoredRespondToApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .applicationId(APP_ID)
            .storedRespondId(TEST)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.SUBMIT_STORED_CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getSendNotificationCollectionResponse());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storedRespondToApplicationService.submitRespondToApplication(TEST_SERVICE_AUTH_TOKEN, testRequest));

        assertThat(exception.getMessage())
            .isEqualTo(RESPOND_ID_INCORRECT);
    }

}
