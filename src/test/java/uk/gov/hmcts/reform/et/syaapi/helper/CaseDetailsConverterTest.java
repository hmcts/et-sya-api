package uk.gov.hmcts.reform.et.syaapi.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class CaseDetailsConverterTest {

    private final CaseDetails expectedDetails = ResourceLoader.fromString(
        "responses/caseDetails.json",
        CaseDetails.class
    );

    private final StartEventResponse startEventResponse = ResourceLoader.fromString(
        "responses/startEventResponse.json",
        StartEventResponse.class
    );

    @Autowired
    private Et1CaseData et1CaseData;

    @BeforeEach
    void setUp() {
        et1CaseData = new Et1CaseData();
        et1CaseData.setCaseNotes("TEST_STRING");
        et1CaseData.setCaseSource("MANUALLY_CREATED");
    }

    @Test
    void shouldGetCaseDetailsConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        caseDetailsConverter.caseDataContent(startEventResponse, et1CaseData);
        assertThat("Manually Created".equalsIgnoreCase(
            caseDetailsConverter.toCaseData(expectedDetails).getCaseSource())).isTrue();
    }
}
