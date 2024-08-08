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
    private static final String CORE_CASE_DATA_ID = "ccdID";
    private static final String ETHOS_CASE_REFERENCE = "ethosCaseReference";
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
        CaseData result = caseDetailsConverter.getUpdatedCaseData(requestData, latestData);
        assertNotNull(result);
    }

    @Test
    void testGetUpdatedCaseDataWithNullFieldsInRequestData() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put(CORE_CASE_DATA_ID, "123");
        requestData.put(ETHOS_CASE_REFERENCE, null); // This should not overwrite latestData

        Map<String, Object> latestData = new ConcurrentHashMap<>();
        latestData.put(CORE_CASE_DATA_ID, "456");
        latestData.put(ETHOS_CASE_REFERENCE, "Original Name");

        CaseData result = caseDetailsConverter.getUpdatedCaseData(requestData, latestData);
        assertNotNull(result);
        assertEquals("123", result.getCcdID());
        assertEquals("Original Name", result.getEthosCaseReference());
    }

    @Test
    void testGetUpdatedCaseDataWithNullValues() {
        // Handle null requestData and latestData
        Map<String, Object> requestData2 = new HashMap<>();
        requestData2.put(CORE_CASE_DATA_ID, null);  // Should not overwrite latestData
        requestData2.put(ETHOS_CASE_REFERENCE, "2002/998083");

        Map<String, Object> latestData2 = new ConcurrentHashMap<>();
        latestData2.put(CORE_CASE_DATA_ID, "456");
        latestData2.put(ETHOS_CASE_REFERENCE, "18002/99808003");

        CaseData result = caseDetailsConverter.getUpdatedCaseData(requestData2, latestData2);

        assertEquals("456", result.getCcdID());
        assertEquals("2002/998083", result.getEthosCaseReference());
    }

}
