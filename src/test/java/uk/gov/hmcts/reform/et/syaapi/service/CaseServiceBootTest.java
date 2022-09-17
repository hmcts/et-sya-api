package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
class CaseServiceBootTest {

    @Autowired
    CaseService caseService;

    private TestData testData;

    @BeforeEach
    void beforeEach() {
        testData = new TestData();
    }

    @Test
    void testSubmitCase() {
        assertEquals(testData.getTestCaseData().getEcmCaseType(), "Single");
    }
}
