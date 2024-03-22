package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredApplicationRequest;
import uk.gov.service.notify.NotificationClientException;

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

class StoreApplicationServiceTest {
    @MockBean
    private CaseService caseService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;
    @InjectMocks
    private StoreApplicationService storeApplicationService;

    private final TestData testData;

    private static final String APP_ID_INCORRECT = "Application id provided is incorrect";

    private static final long CASE_ID = 1_646_225_213_651_590L;
    private static final String CASE_TYPE_ID = "ET_EnglandWales";
    private static final String APP_ID = "3be1ea83-06ef-40ff-bda5-e2ae88998a18";
    private static final String TEST = "Test";

    private StartEventResponse startEventResponse;

    StoreApplicationServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() {
        storeApplicationService = new StoreApplicationService(
            mock(CaseService.class),
            mock(CaseDetailsConverter.class),
            mock(NotificationService.class)
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
    void shouldSubmitStoredApplicationShouldReturnCaseDetails() throws NotificationClientException {
        ClaimantApplicationRequest testRequest = ClaimantApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.SUBMIT_STORED_CLAIMANT_TSE
        )).thenReturn(startEventResponse);

        when(caseService.submitUpdate(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(testRequest.getCaseId()),
            any(),
            eq(testRequest.getCaseTypeId())
        )).thenReturn(CaseDetails.builder().id(CASE_ID).build());

        storeApplicationService.storeApplication(TEST_SERVICE_AUTH_TOKEN, testRequest);

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
        ClaimantApplicationRequest testRequest = ClaimantApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storeApplicationService.storeApplication(TEST_SERVICE_AUTH_TOKEN, testRequest));
        assertThat(exception.getMessage())
            .isEqualTo(APP_ID_INCORRECT);
    }
}
