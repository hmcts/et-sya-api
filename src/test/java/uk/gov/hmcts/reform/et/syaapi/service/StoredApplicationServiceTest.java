package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredApplicationRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
    void shouldSubmitStoredApplication() {
        SubmitStoredApplicationRequest testRequest = testData.getSubmitStoredApplicationRequest();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.SUBMIT_STORED_CLAIMANT_TSE
        )).thenReturn(testData.getUpdateCaseEventResponse());

        storedApplicationService.submitStoredApplication(TEST_SERVICE_AUTH_TOKEN, testRequest);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        GenericTseApplicationType actual =
            argumentCaptor.getValue().getGenericTseApplicationCollection().get(0).getValue();
        assertThat(actual.getDate()).isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getDueDate()).isEqualTo(UtilHelper.formatCurrentDatePlusDays(LocalDate.now(), 7));
        assertThat(actual.getApplicationState()).isEqualTo("inProgress");
        assertThat(actual.getStatus()).isEqualTo("Open");
    }

}
