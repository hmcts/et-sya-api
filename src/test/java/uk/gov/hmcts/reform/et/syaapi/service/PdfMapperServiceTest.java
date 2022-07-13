package uk.gov.hmcts.reform.et.syaapi.service;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimCaseDocument;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

@Slf4j
class PdfMapperServiceTest {

    private final Integer TOTAL_VALUES = 27;

    private PdfMapperService pdfMapperService;

    private CaseData caseData;


    @BeforeEach
    void setup() throws IOException {
        pdfMapperService = new PdfMapperService();

        caseData = ResourceLoader.fromString(
            "responses/pdfMapperCaseDetails.json", CaseData.class
        );
    }

    @Test
    void givenCaseProducesPdfHeaderMap() throws PdfMapperException {
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);

        assertEquals(TOTAL_VALUES, pdfMap.size());

        // test by measure length
        log.info(pdfMap.toString());
    }

    @Test
    void givenEmptyCaseProducesPdfMapperException() {
        assertThrows(
            PdfMapperException.class,
            () -> pdfMapperService.mapHeadersToPdf(null));
    }

    @Test
    void givenPreferredContactAsEmailReflectsInMap() throws PdfMapperException {
        ClaimantType claimantType = caseData.getClaimantType();
        claimantType.setClaimantContactPreference("Email");
        caseData.setClaimantType(claimantType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get("1.8 How should we contact you - Email"));
    }

    @Test
    void givenPreferredContactAsPostReflectsInMap() throws PdfMapperException {
        ClaimantType claimantType = caseData.getClaimantType();
        claimantType.setClaimantContactPreference("Post");
        caseData.setClaimantType(claimantType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get("1.8 How should we contact you - Post"));
    }

    @Test
    void givenAcasEarlyCertificateNumberReflectsInMap() {

    }

    @Test
    void withoutAcasEarlyCertficateReflectsInMap() {

    }

    @Test
    void givenTwoRespondentsReflectsInMap() {

    }

    @Test
    void givenThreeRespondentsReflectsInMap() {

    }
}

// TODO: Accepted Test
// TODO: Empty case data
// TODO: contact preferance -email
// TODO: contact preferance -post
// TODO: claimant work address different to respondent
// TODO: With acas early cert number
// TODO: Without acas early cert number
// TODO: With 2 respondents
// TODO: 3 respondents