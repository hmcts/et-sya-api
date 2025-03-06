package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.ChangeApplicationStatusRequest;
import uk.gov.hmcts.reform.et.syaapi.models.ChangeRespondentApplicationStatusRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

class RespondentTseServiceTest {
    @MockBean
    private CaseService caseService;
    @MockBean
    private RespondentTseService respondentTseService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    private final TestData testData;

    RespondentTseServiceTest() {
        testData = new TestData();
    }

    @Test
    void shouldMarkApplicationAsViewed() {
        ChangeRespondentApplicationStatusRequest testRequest =
        ChangeRespondentApplicationStatusRequest.builder()
            .caseId("12345")
            .caseTypeId("ET_EnglandWales")
            .applicationId("1cba4d4c-26d1-47c3-9197-0603f935d708")
            .userIdamId("e67fae0a-7a75-4f45-abc8-6f9d2a79801f")
            .newStatus("viewed")
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.UPDATE_APPLICATION_STATE
        )).thenReturn(testData.getUpdateCaseEventResponse());

        respondentTseService.changeRespondentApplicationStatus(TEST_SERVICE_AUTH_TOKEN, testRequest);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        String actualState = argumentCaptor.getValue()
            .getGenericTseApplicationCollection().get(0).getValue()
            .getRespondentState().get(0)
            .getApplicationState();
        assertThat(actualState).isEqualTo("viewed");
    }
}
