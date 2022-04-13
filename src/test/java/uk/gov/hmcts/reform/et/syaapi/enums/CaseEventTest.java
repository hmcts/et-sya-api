package uk.gov.hmcts.reform.et.syaapi.enums;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.INITIATE_CASE_DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.UPDATE_CASE_DRAFT;

@Import(CaseEvent.class)
class CaseEventTest {

    @Test
    void shouldGetUpdateEventName() {
        assertThat(UPDATE_CASE_DRAFT.equalsIgnoreCase(String.valueOf(CaseEvent.UPDATE_CASE_DRAFT))).isTrue();
    }

    @Test
    void shouldGetSubmitEventName() {
        assertThat(INITIATE_CASE_DRAFT.equalsIgnoreCase(String.valueOf(CaseEvent.INITIATE_CASE_DRAFT))).isTrue();
    }

}

