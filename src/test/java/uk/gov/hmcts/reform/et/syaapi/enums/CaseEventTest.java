package uk.gov.hmcts.reform.et.syaapi.enums;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.INITIATE_CASE_DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.UPDATE_CASE_DRAFT;

@Import(CaseEvent.class)
class CaseEventTest {

    @Test
    void shouldGetUpdateEventName() throws Exception {
        assertEquals(UPDATE_CASE_DRAFT, String.valueOf(CaseEvent.UPDATE_CASE_DRAFT));
    }

    @Test
    void shouldGetSubmitEventName() throws Exception {
        assertEquals(INITIATE_CASE_DRAFT, String.valueOf(CaseEvent.INITIATE_CASE_DRAFT));
    }

}
