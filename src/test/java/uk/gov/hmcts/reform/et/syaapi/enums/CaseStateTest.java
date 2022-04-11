package uk.gov.hmcts.reform.et.syaapi.enums;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.AWAITING_SUBMISSION_TO_HMCTS;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.SUBMITTED;

@Import(CaseEvent.class)
class CaseStateTest {

    @Test
    void shouldGetDraftStatetName() {
        assertThat(DRAFT.equalsIgnoreCase(String.valueOf(CaseState.DRAFT))).isTrue();
    }

    @Test
    void shouldGetAwaitSubmissionName() {
        assertThat(AWAITING_SUBMISSION_TO_HMCTS
                       .equalsIgnoreCase(String.valueOf(CaseState.AWAITING_SUBMISSION_TO_HMCTS))).isTrue();
    }

    @Test
    void shouldGetSubmitCaseStatetName() {
        assertThat(SUBMITTED.equalsIgnoreCase(String.valueOf(CaseState.SUBMITTED))).isTrue();
    }

}
