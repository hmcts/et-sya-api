package uk.gov.hmcts.reform.et.syaapi.helper;

import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.models.EmploymentCaseData;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.INITIATE_CASE_DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.UPDATE_CASE_DRAFT;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
public class EmploymentCaseDataTest {
    private static final String TEST_STRING = "TEST";
    private static final String SINGLE = "Single";
    private static final String MANUALLY_CREATED = "Manually Created";

    @Autowired
    private EmploymentCaseData employmentCaseData;

    @BeforeEach
    void setUp() {
        employmentCaseData = new EmploymentCaseData();
        employmentCaseData.setCaseNotes(TEST_STRING);
        employmentCaseData.setCaseType(SINGLE);
        employmentCaseData.setCaseSource(MANUALLY_CREATED);
        employmentCaseData.builder().build();
    }

    @Test
    void shouldGetEmploymentCaseData() throws Exception {
        assertEquals(TEST_STRING, employmentCaseData.getCaseNotes());
        assertEquals(SINGLE, employmentCaseData.getCaseType());
        assertEquals(MANUALLY_CREATED, employmentCaseData.getCaseSource());
    }
}
