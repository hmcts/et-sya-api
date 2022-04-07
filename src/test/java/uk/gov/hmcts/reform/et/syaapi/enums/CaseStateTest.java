package uk.gov.hmcts.reform.et.syaapi.enums;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.AWAITING_SUBMISSION_TO_HMCTS;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.SUBMITTED;


@Import(CaseEvent.class)
class CaseStateTest {

    @Test
    void shouldGetDraftStatetName() throws Exception {
        assertEquals(DRAFT, String.valueOf(CaseState.DRAFT));
    }

    @Test
    void shouldGetAwaitSubmissionName() throws Exception {
        assertEquals(AWAITING_SUBMISSION_TO_HMCTS, String.valueOf(CaseState.AWAITING_SUBMISSION_TO_HMCTS));
    }

    @Test
    void shouldGetSubmitCaseStatetName() throws Exception {
        assertEquals(SUBMITTED, String.valueOf(CaseState.SUBMITTED));
    }

}
