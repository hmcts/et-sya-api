package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;

import static org.assertj.core.api.Assertions.assertThat;

@Import(EmployeeObjectMapper.class)
class EmploymentObjectMapperTest {
    @Mock
    private EmployeeObjectMapper employmentObjectMapper;

    @BeforeEach
    void setUp() {
        employmentObjectMapper = new EmployeeObjectMapper();
    }

    @Test
    void shouldGetEmployeeObjetMapper() {
        Et1CaseData et1CaseData = employmentObjectMapper.getEmploymentCaseData("{\"caseNotes\": \"TEST\"}");
        assertThat("TEST".equalsIgnoreCase(et1CaseData.getCaseNotes())).isTrue();
    }

    @Test
    void shouldGetNullData() {
        Et1CaseData et1CaseData = employmentObjectMapper.getEmploymentCaseData("\"caseType\": \"Single\"");
        assertThat(et1CaseData).isNull();
    }
}
