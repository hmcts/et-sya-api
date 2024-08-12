package uk.gov.hmcts.reform.et.syaapi.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings({"PMD.UseConcurrentHashMap"})
class CaseDetailsConverterTest {
    private static final String CASE_SOURCE = "caseSource";
    private static final String CASE_NOTES = "caseNotes";
    private final CaseDetails expectedDetails = ResourceLoader.fromString(
        "responses/caseDetails.json",
        CaseDetails.class
    );

    private final StartEventResponse startEventResponse = ResourceLoader.fromString(
        "responses/startEventResponse.json",
        StartEventResponse.class
    );

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private Et1CaseData et1CaseData;

    @BeforeEach
    void setUp() {
        et1CaseData = new Et1CaseData();
        et1CaseData.setCaseNotes("TEST_STRING");
        et1CaseData.setCaseSource("MANUALLY_CREATED");
        caseDetailsConverter = new CaseDetailsConverter(objectMapper);
    }

    @Test
    void shouldGetCaseDetailsConverter() {
        caseDetailsConverter.et1ToCaseDataContent(startEventResponse, et1CaseData);
        assertThat(caseDetailsConverter.toCaseData(expectedDetails).getCaseSource())
            .isEqualToIgnoringCase("Manually Created");
    }

    @Test
    void shouldConvertDataToCaseDataWithNullCaseDetails() {
        assertNull(caseDetailsConverter.toCaseData(null));
    }

    @Test
    void shouldConvertCaseDetailsDataToCaseData() {
        caseDetailsConverter.et1ToCaseDataContent(startEventResponse, et1CaseData);
        assertNotNull(caseDetailsConverter.toCaseData(expectedDetails));
    }

    @Test
    void shouldGetCaseDataNull() {
        caseDetailsConverter.getCaseData(expectedDetails.getData());
        assertNotNull(caseDetailsConverter.getCaseData(expectedDetails.getData()));
        assertEquals(caseDetailsConverter.getCaseData(expectedDetails.getData()).getClass(), CaseData.class);
    }

    @Test
    void testGetUpdatedCaseDataWithEmptyMaps() {
        Map<String, Object> requestData = new ConcurrentHashMap<>();
        Map<String, Object> latestData = new ConcurrentHashMap<>();
        Et1CaseData result = caseDetailsConverter.getUpdatedCaseData(requestData, latestData);
        assertNotNull(result);
    }

    @Test
    void testGetUpdatedCaseDataWithNullFieldsInRequestData() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put(CASE_SOURCE, "Manually created");
        requestData.put(CASE_NOTES, null); // This should not overwrite latestData

        Map<String, Object> latestData = new ConcurrentHashMap<>();
        latestData.put(CASE_SOURCE, "ET1 Online");
        requestData.put(CASE_NOTES, "test case notes");

        Et1CaseData result = caseDetailsConverter.getUpdatedCaseData(requestData, latestData);
        assertNotNull(result);
        assertEquals("Manually created", result.getCaseSource());
        assertEquals("test case notes", result.getCaseNotes());
    }

    @Test
    void testGetUpdatedCaseDataWithNullValues() {
        // Handle null requestData and latestData
        Map<String, Object> requestData2 = new HashMap<>();
        requestData2.put(CASE_SOURCE, null); // Should not overwrite latestData
        requestData2.put(CASE_NOTES, "original test notes");

        Map<String, Object> latestData2 = new ConcurrentHashMap<>();
        latestData2.put(CASE_SOURCE, "ET1 Online");
        latestData2.put(CASE_NOTES, "test Case Notes");

        Et1CaseData result = caseDetailsConverter.getUpdatedCaseData(requestData2, latestData2);

        assertEquals("ET1 Online", result.getCaseSource());
        assertEquals("original test notes", result.getCaseNotes());
    }

}
