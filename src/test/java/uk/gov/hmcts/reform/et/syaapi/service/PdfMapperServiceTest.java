package uk.gov.hmcts.reform.et.syaapi.service;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

@Slf4j
class PdfMapperServiceTest {

    private final Integer TOTAL_VALUES = 36;

    private PdfMapperService pdfMapperService;

    private CaseData caseData = ResourceLoader.fromString(
        "responses/pdfMapperCaseDetails.json", CaseData.class
    );


    @BeforeEach
    void setup() throws IOException {
        pdfMapperService = new PdfMapperService();
    }

    @Test
    void givenCorrectJsonProducesPdfHeaderMap() {
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);

        assertEquals(TOTAL_VALUES, pdfMap.size());

        // test by measure length
        log.info(pdfMap.toString());
    }


}

// TODO: Accepted Test
// TODO: Empty case data
// TODO: contact preferance -email
// TODO: contact preferance -post
// TODO: With acas early cert number
// TODO: Without acas early cert number
// TODO: With 2 respondents
// TODO: 3 respondents