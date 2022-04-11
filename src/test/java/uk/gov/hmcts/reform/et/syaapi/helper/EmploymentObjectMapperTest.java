package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.ecm.common.model.ccd.Et1CaseData;

import static org.junit.Assert.assertEquals;

@Import(EmployeeObjectMapper.class)
class EmploymentObjectMapperTest {
    @Mock
    private EmployeeObjectMapper employmentObjectMapper;

    @BeforeEach
    void setUp() {
        employmentObjectMapper = new EmployeeObjectMapper();
    }

    @Test
    void shouldGetEmployeeObjetMapper() throws Exception {
        Et1CaseData et1CaseData = employmentObjectMapper.getEmploymentCaseData("{\"caseNotes\": \"TEST\"}");
        assertEquals("TEST", et1CaseData.getCaseNotes());
    }

    @Test
    void shouldGetNullData() throws Exception {
        Et1CaseData employmentCaseData = employmentObjectMapper.getEmploymentCaseData("\"caseType\": \"Single\"");
        assertEquals(null, employmentCaseData);
    }
}
