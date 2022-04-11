package uk.gov.hmcts.reform.et.syaapi.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.models.EmploymentCaseData;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

import static org.junit.Assert.assertEquals;

public class CaseDetailsConverterTest {

    private final CaseDetails expectedDetails = ResourceLoader.fromString(
        "responses/caseDetails.json",
        CaseDetails.class
    );

    private final StartEventResponse startEventResponse = ResourceLoader.fromString(
        "responses/startEventResponse.json",
        StartEventResponse.class
    );

    @Autowired
    private EmploymentCaseData employmentCaseData;

    @BeforeEach
    void setUp(){
        employmentCaseData = new EmploymentCaseData();
        employmentCaseData.setCaseNotes("TEST_STRING");
        employmentCaseData.setCaseType("SINGLE");
        employmentCaseData.setCaseSource("MANUALLY_CREATED");
        employmentCaseData.builder().build();
    }

    @Test
    void shouldGetCaseDetailsConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        caseDetailsConverter.caseDataContent(startEventResponse, employmentCaseData);
        assertEquals("Manually Created", caseDetailsConverter.toCaseData(expectedDetails).getCaseSource());
    }
}
