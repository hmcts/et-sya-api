package uk.gov.hmcts.reform.et.syaapi.enums;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.SUBMITTED;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.Draft;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.Submitted;

@Import(CaseEvent.class)
public class CaseStateTest {

    @Test
    void shouldGetDraftStatetName() throws Exception {
        assertEquals(DRAFT, String.valueOf(CaseState.DRAFT));
    }

    @Test
    void shouldGetSubmitCaseStatetName() throws Exception {
        assertEquals(SUBMITTED, String.valueOf(CaseState.SUBMITTED));
    }

    @Test
    void shouldGetDraftStatetLowerCaseName() throws Exception {
        assertEquals(Draft, String.valueOf(CaseState.Draft));
    }

    @Test
    void shouldGetSubmitStatetLowerCaseName() throws Exception {
        assertEquals(Submitted, String.valueOf(CaseState.DRAFT));
    }
}
