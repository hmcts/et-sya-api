package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.AdminDecisionNotificationStateUpdateRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;


class AdminDecisionNotificationServiceTest {

    @MockBean
    private CaseService caseService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;
    @MockBean
    private AdminDecisionNotificationService adminDecisionNotificationService;

    private final TestData testData;

    public AdminDecisionNotificationServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() {
        caseService = mock(CaseService.class);
        caseDetailsConverter = mock(CaseDetailsConverter.class);
        adminDecisionNotificationService = new AdminDecisionNotificationService(caseService, caseDetailsConverter);

        when(caseService.startUpdate(
            any(),
            any(),
            any(),
            any()
        )).thenReturn(testData.getUpdateCaseEventResponse());

    }

    @Test
    void shouldFindAndUpdateCase() {
        adminDecisionNotificationService.updateAdminDecisionNotificationState(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getAdminDecisionNotificationStateUpdateRequest()
        );

        verify(caseDetailsConverter, times(1)).caseDataContent(
            any(),
            any()
        );
        verify(caseService, times(1)).submitUpdate(
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void shouldNotUpdateCaseAndThrowException() {
        AdminDecisionNotificationStateUpdateRequest testRequest =
            testData.getAdminDecisionNotificationStateUpdateRequest();
        testRequest.setAdminDecisionId("778");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            adminDecisionNotificationService.updateAdminDecisionNotificationState(
                TEST_SERVICE_AUTH_TOKEN,
                testRequest
            ));
        assertThat(exception.getMessage()).isEqualTo("Admin decision id is invalid");
    }
}
