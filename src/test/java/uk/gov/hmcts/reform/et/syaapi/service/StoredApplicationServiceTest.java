package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToApplicationRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

class StoredApplicationServiceTest {

    @MockBean
    private CaseService caseService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;
    @InjectMocks
    private StoredApplicationService storedApplicationService;

    private final TestData testData;

    private static final String APP_ID_INCORRECT =  "Application id provided is incorrect";
    private static final String RESPOND_ID_INCORRECT =  "Respond id provided is incorrect";

    private static final long CASE_ID = 1_646_225_213_651_590L;
    private static final String CASE_TYPE_ID = "ET_EnglandWales";
    private static final String APP_ID = "1xx567";
    private static final String RESPOND_ID = "777";

    StoredApplicationServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() {
        caseService = mock(CaseService.class);
        caseDetailsConverter = mock(CaseDetailsConverter.class);

        storedApplicationService = new StoredApplicationService(
            caseService,
            mock(NotificationService.class),
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
    void shouldSubmitStoredApplicationShouldReturnCaseDetails() {
        SubmitStoredApplicationRequest testRequest = SubmitStoredApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .applicationId(APP_ID)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.SUBMIT_STORED_CLAIMANT_TSE
        )).thenReturn(testData.getUpdateCaseEventResponse());

        when(caseService.submitUpdate(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(testRequest.getCaseId()),
            any(),
            eq(testRequest.getCaseTypeId())
        )).thenReturn(CaseDetails.builder().id(CASE_ID).build());

        storedApplicationService.submitStoredApplication(TEST_SERVICE_AUTH_TOKEN, testRequest);

        verify(caseService, times(1)).submitUpdate(
            any(),
            any(),
            any(),
            any()
        );

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        GenericTseApplicationType actual =
            argumentCaptor.getValue().getGenericTseApplicationCollection().get(0).getValue();
        assertThat(actual.getDate()).isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getDueDate()).isEqualTo(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7));
        assertThat(actual.getApplicationState()).isEqualTo("inProgress");
        assertThat(actual.getStatus()).isEqualTo("Open");
    }

    @Test
    void shouldSubmitStoredApplicationShouldApplicationIdException() {
        SubmitStoredApplicationRequest testRequest = SubmitStoredApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .applicationId("Test")
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storedApplicationService.submitStoredApplication(TEST_SERVICE_AUTH_TOKEN, testRequest));
        assertThat(exception.getMessage())
            .isEqualTo(APP_ID_INCORRECT);
    }

    @Test
    void submitRespondToApplicationShouldReturnCaseDetails() {
        UpdateStoredRespondToApplicationRequest testRequest = UpdateStoredRespondToApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .applicationId(APP_ID)
            .respondId(RESPOND_ID)
            .isRespondingToRequestOrOrder(true)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getUpdateCaseEventResponse());

        storedApplicationService.submitRespondToApplication(TEST_SERVICE_AUTH_TOKEN, testRequest);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        GenericTseApplicationType actualAppType =
            argumentCaptor.getValue().getGenericTseApplicationCollection().get(0).getValue();
        TseRespondType actual = actualAppType.getRespondCollection().get(0).getValue();
        assertThat(actual.getDate()).isEqualTo(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getStatus()).isNull();
    }

    @Test
    void submitRespondToApplicationShouldApplicationIdException() {
        UpdateStoredRespondToApplicationRequest testRequest = UpdateStoredRespondToApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .applicationId("Test")
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getUpdateCaseEventResponse());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                storedApplicationService.submitRespondToApplication(TEST_SERVICE_AUTH_TOKEN, testRequest));
        assertThat(exception.getMessage())
            .isEqualTo(APP_ID_INCORRECT);
    }

    @Test
    void submitRespondToApplicationShouldRespondIdError() {
        UpdateStoredRespondToApplicationRequest testRequest = UpdateStoredRespondToApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .applicationId(APP_ID)
            .respondId("Test")
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getUpdateCaseEventResponse());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storedApplicationService.submitRespondToApplication(TEST_SERVICE_AUTH_TOKEN, testRequest));
        assertThat(exception.getMessage())
            .isEqualTo(RESPOND_ID_INCORRECT);
    }
}
