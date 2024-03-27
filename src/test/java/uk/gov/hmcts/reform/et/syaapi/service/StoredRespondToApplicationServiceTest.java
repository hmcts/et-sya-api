package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredRespondToApplicationRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.WAITING_FOR_THE_TRIBUNAL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

class StoredRespondToApplicationServiceTest {

    @MockBean
    private CaseService caseService;
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
    private static final String APP_RESPOND_ID = "a0d58d55-bfe1-421a-b80f-c3843ae18be8";
    private static final String TEST = "Test";

    private StartEventResponse startEventResponse;

    StoredRespondToApplicationServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() {
        caseService = mock(CaseService.class);
        caseDetailsConverter = mock(CaseDetailsConverter.class);

        storedRespondToApplicationService = new StoredRespondToApplicationService(
            caseService,
            mock(NotificationService.class),
            mock(CaseDocumentService.class),
            caseDetailsConverter,
            mock(FeatureToggleService.class)
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
    void submitRespondToApplicationShouldReturnCaseDetails() {
        SubmitStoredRespondToApplicationRequest testRequest = SubmitStoredRespondToApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .applicationId(APP_ID)
            .storedRespondId(APP_RESPOND_ID)
            .isRespondingToRequestOrOrder(true)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(startEventResponse);

        storedRespondToApplicationService.submitRespondToApplication(TEST_SERVICE_AUTH_TOKEN, testRequest);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        GenericTseApplicationType actual =
            argumentCaptor.getValue().getGenericTseApplicationCollection().get(0).getValue();
        assertThat(actual.getApplicationState()).isEqualTo(WAITING_FOR_THE_TRIBUNAL);

        TseRespondType actualRespond = actual.getRespondCollection().get(1).getValue();
        assertThat(actualRespond.getDate()).isEqualTo(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actualRespond.getStatus()).isNull();
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
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(startEventResponse);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storedRespondToApplicationService.submitRespondToApplication(TEST_SERVICE_AUTH_TOKEN, testRequest));
        assertThat(exception.getMessage())
            .isEqualTo(APP_ID_INCORRECT);
    }

    @Test
    void submitRespondToApplicationShouldRespondIdError() {
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
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(startEventResponse);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storedRespondToApplicationService.submitRespondToApplication(TEST_SERVICE_AUTH_TOKEN, testRequest));
        assertThat(exception.getMessage())
            .isEqualTo(RESPOND_ID_INCORRECT);
    }
}
