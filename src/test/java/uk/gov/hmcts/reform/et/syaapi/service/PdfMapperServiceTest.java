package uk.gov.hmcts.reform.et.syaapi.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

@Slf4j
class PdfMapperServiceTest {
    private final Map<String, String> MAP_KEYS = Map.of(
        "emailContact", "1.8 How should we contact you - Email",
        "postContact", "1.8 How should we contact you - Post",
        "earlyConciliationCertNumQ1", "2.3 Do you have an Acas early conciliation certificate number? Yes",
        "employmentStart", "5.1 when did your employment start?",
        "employmentContinued", "5.1 is your employment continuing? Yes",
        "employmentEnded", "5.1 is your employment continuing? No",
        "withPension", "6.4 Were you in your employer's pension scheme? Yes",
        "withoutPension", "6.4 Were you in your employer's pension scheme? No",
        "weeklyPensionContribution", "6.4 If Yes, give your employers weekly contributions"
    );
    private final Integer TOTAL_VALUES = 39;
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
        assertNotNull(pdfMap.get(MAP_KEYS.get("emailContact")));
    }

    @Test
    void givenPreferredContactAsPostReflectsInMap() throws PdfMapperException {
        ClaimantType claimantType = caseData.getClaimantType();
        claimantType.setClaimantContactPreference("Post");
        caseData.setClaimantType(claimantType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(MAP_KEYS.get("postContact")));
    }

    @Test
    void givenAcasEarlyConciliationCertificateNumberReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentACASQuestion("Yes");
        respondentSumType.setRespondentACAS("1111");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(MAP_KEYS.get("earlyConciliationCertNumQ1")));
    }

    @Test
    void withoutAcasEarlyCertficateReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentACASQuestion("No");
        respondentSumType.setRespondentACAS(null);
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNull(pdfMap.get(MAP_KEYS.get("earlyConciliationCertNumQ1")));
    }

    @Test
    void givenTwoRespondentsReflectsInMap() {

    }

    @Test
    void givenThreeRespondentsReflectsInMap() {

    }

    @Test
    void givenClaimentDidntWorkForRespondentSkipsSection() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantEmployedFrom(null);
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNull(pdfMap.get(MAP_KEYS.get("employmentStart")));
    }

    @Test
    void givenContinuedEmploymentReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantEmployedCurrently("Yes");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(MAP_KEYS.get("employmentContinued")));
        assertNull(pdfMap.get(MAP_KEYS.get("employmentEnded")));
    }

    @Test
    void givenDiscontinuedEmploymentReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantEmployedCurrently("No");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(MAP_KEYS.get("employmentEnded")));
        assertNull(pdfMap.get(MAP_KEYS.get("employmentContinued")));
    }

    @Test
    void givenPensionContributionReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPensionContribution("Yes");
        claimantOtherType.setClaimantPensionWeeklyContribution("100");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(MAP_KEYS.get("withPension")));
        assertNotNull(pdfMap.get(MAP_KEYS.get("weeklyPensionContribution")));
    }

    @Test
    void givenNoPensionContributionReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPensionContribution("No");
        claimantOtherType.setClaimantPensionWeeklyContribution(null);
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(MAP_KEYS.get("withoutPension")));
        assertNull(pdfMap.get(MAP_KEYS.get("weeklyPensionContribution")));
    }

    private RespondentSumTypeItem generateRespondent() {
        return null;
    }
}

// TODO: claimant work address different to respondent
// TODO: Aware of multiple Cases

// employment details
// TODO: Pension Scheme
// TODO: Another Job details

// Claim details
// TODO: Test discrimination grounds
// TODO: what is owed

// representative
// TODO: communication prefernces

// Additional Respondents
// TODO: 4 Respondents
// TODO: 5 Respondents

