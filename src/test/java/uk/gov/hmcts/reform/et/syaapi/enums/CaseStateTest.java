package uk.gov.hmcts.reform.et.syaapi.enums;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.service.util.TestConstants.AWAITING_SUBMISSION_TO_HMCTS;
import static uk.gov.hmcts.reform.et.syaapi.service.util.TestConstants.DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.service.util.TestConstants.SUBMITTED;

@Import(CaseEvent.class)
class CaseStateTest {

    @Test
    void shouldGetDraftStatetName() {
        assertThat(String.valueOf(CaseState.DRAFT)).isEqualToIgnoringCase(DRAFT);
    }

    @Test
    void shouldGetAwaitSubmissionName() {
        assertThat(String.valueOf(CaseState.AWAITING_SUBMISSION_TO_HMCTS)).isEqualToIgnoringCase(
            AWAITING_SUBMISSION_TO_HMCTS);
    }

    @Test
    void shouldGetSubmitCaseStatetName() {
        assertThat(String.valueOf(CaseState.SUBMITTED)).isEqualToIgnoringCase(SUBMITTED);
    }

}
