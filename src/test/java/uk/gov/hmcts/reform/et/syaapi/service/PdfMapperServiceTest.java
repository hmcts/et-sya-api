package uk.gov.hmcts.reform.et.syaapi.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

public class PdfMapperServiceTest {
    private PdfMapperService pdfMapperService;
    private final List<CaseDetails> caseDataJson = ResourceLoader.fromStringToList(
        "responses/pdfMapperCaseDetails.json",
        CaseDetails.class
    );


    @BeforeEach
    void setup() throws IOException {
        pdfMapperService = new PdfMapperService();
    }

    @Test
    void givenCorrectJsonProducesPdfHeaderMap() {
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseDataJson.get(0).getData());

        assertEquals(expectedResponse, pdfMap);
    }

    private final Map<String, String> expectedResponse = Map.of(
        "tribunal office", "Manchester",
        "case number", "1654880362724199",
        "date received", "2021-07-12"
    );
}
