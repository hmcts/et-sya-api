package uk.gov.hmcts.reform.et.syaapi.enums;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.*;

@Import(CaseEvent.class)
public class CaseEventTest {

    @Test
    void shouldGetUpdateEventName() throws Exception {
        assertEquals(UPDATE_CASE_DRAFT, String.valueOf(CaseEvent.UPDATE_CASE_DRAFT));
    }

    @Test
    void shouldGetSubmitEventName() throws Exception {
        assertEquals(SUBMIT_CASE_DRAFT, String.valueOf(CaseEvent.SUBMIT_CASE_DRAFT));
    }

    @Test
    void shouldGetSubmitCaseEventName() throws Exception {
        assertEquals(SUBMITCASE_DRAFT_LOWER, String.valueOf(CaseEvent.submitCaseDraft));
    }
}
