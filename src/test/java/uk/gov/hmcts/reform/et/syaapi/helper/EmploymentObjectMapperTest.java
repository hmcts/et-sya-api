package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.et.syaapi.models.EmploymentCaseData;

import static org.junit.Assert.assertEquals;

@Import(EmployeeObjectMapper.class)
public class EmploymentObjectMapperTest {
    private static final String TEST_STRING = "TEST";
    private static final String SINGLE = "Single";
    private static final String MANUALLY_CREATED = "Manually Created";

    @Mock
    private EmployeeObjectMapper employmentObjectMapper;

    @BeforeEach
    void setUp() {
        employmentObjectMapper = new EmployeeObjectMapper()  ;
    }

    @Test
    void shouldGetEmployeeObjetMapper() throws Exception {
        EmploymentCaseData employmentCaseData = employmentObjectMapper.getEmploymentCaseData("{\"caseType\": \"Single\"}");
        assertEquals(SINGLE, employmentCaseData.getCaseType());
    }

    @Test
    void shouldGetNullData() throws Exception {
        EmploymentCaseData employmentCaseData = employmentObjectMapper.getEmploymentCaseData("\"caseType\": \"Single\"");
        assertEquals(null, employmentCaseData);
    }
}
