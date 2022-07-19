package uk.gov.hmcts.reform.et.syaapi.service;

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
import uk.gov.hmcts.et.common.model.ccd.types.NewEmploymentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.constants.PdfMapperConstants;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

@Slf4j
class PdfMapperServiceTest {
    private final Integer TOTAL_VALUES = 54;
    private PdfMapperService pdfMapperService;
    private CaseData caseData;

    @BeforeEach
    void setup() {
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
        assertNotNull(pdfMap.get(PdfMapperConstants.Q1_CONTACT_EMAIL));
    }

    @Test
    void givenPreferredContactAsPostReflectsInMap() throws PdfMapperException {
        ClaimantType claimantType = caseData.getClaimantType();
        claimantType.setClaimantContactPreference("Post");
        caseData.setClaimantType(claimantType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q1_CONTACT_POST));
    }

    @Test
    void givenAcasEarlyConciliationCertificateNumberReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentACASQuestion("Yes");
        respondentSumType.setRespondentACAS("1111");
        respondentSumType.setRespondentACASNo(null);
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_NUMBER, "2.3")));
    }

    @Test
    void withoutAcasEarlyCertficateWithReasonUnfairDismissalReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentACASQuestion("No");
        respondentSumType.setRespondentACAS(null);
        respondentSumType.setRespondentACASNo("Unfair Dismissal");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_A1, "2.3")));
    }
    @Test
    void withoutAcasEarlyCertficateWithReasonAnotherPersonReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentACASQuestion("No");
        respondentSumType.setRespondentACAS(null);
        respondentSumType.setRespondentACASNo("Another person");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_A2, "2.3")));
    }

    @Test
    void withoutAcasEarlyCertficateWithReasonNoPowerReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentACASQuestion("No");
        respondentSumType.setRespondentACAS(null);
        respondentSumType.setRespondentACASNo("No Power");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_A3, "2.3")));
    }

    @Test
    void withoutAcasEarlyCertficateWithReasonEmployerInTouchReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentACASQuestion("No");
        respondentSumType.setRespondentACAS(null);
        respondentSumType.setRespondentACASNo("Employer already in touch");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_A4, "2.3")));
    }

    @Test
    void givenTwoRespondentsReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumTypeA = caseData.getRespondentCollection().get(0).getValue();
        RespondentSumTypeItem respondentSumTypeItemA = new RespondentSumTypeItem();
        respondentSumTypeItemA.setValue(respondentSumTypeA);
        RespondentSumType respondentSumTypeB = respondentSumTypeA;
        RespondentSumTypeItem respondentSumTypeItemB = new RespondentSumTypeItem();
        respondentSumTypeItemB.setValue(respondentSumTypeB);

        caseData.setRespondentCollection(List.of(respondentSumTypeItemA, respondentSumTypeItemB));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q2_EMPLOYER_NAME));
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_NAME, "2.5 R2")));
    }

    @Test
    void givenThreeRespondentsReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumTypeA = caseData.getRespondentCollection().get(0).getValue();
        RespondentSumTypeItem respondentSumTypeItemA = new RespondentSumTypeItem();
        respondentSumTypeItemA.setValue(respondentSumTypeA);
        RespondentSumType respondentSumTypeB = respondentSumTypeA;
        RespondentSumTypeItem respondentSumTypeItemB = new RespondentSumTypeItem();
        respondentSumTypeItemB.setValue(respondentSumTypeB);
        RespondentSumType respondentSumTypeC = respondentSumTypeA;
        RespondentSumTypeItem respondentSumTypeItemC = new RespondentSumTypeItem();
        respondentSumTypeItemC.setValue(respondentSumTypeC);


        caseData.setRespondentCollection(List.of(respondentSumTypeItemA, respondentSumTypeItemB,
            respondentSumTypeItemC));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q2_EMPLOYER_NAME));
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_NAME, "2.5 R2")));
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_NAME, "2.7 R3")));;
    }

    @Test
    void givenClaimentDidntWorkForRespondentSkipsSection() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantEmployedFrom(null);
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNull(pdfMap.get(PdfMapperConstants.Q5_EMPLOYMENT_START));
    }

    @Test
    void givenContinuedEmploymentReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantEmployedCurrently("Yes");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q5_CONTINUING_YES));
        assertNull(pdfMap.get(PdfMapperConstants.Q5_EMPLOYMENT_END));
    }

    @Test
    void givenDiscontinuedEmploymentReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantEmployedCurrently("No");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q5_EMPLOYMENT_END));
        assertNull(pdfMap.get(PdfMapperConstants.Q5_CONTINUING_YES));
    }

    @Test
    void givenPensionContributionReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPensionContribution("Yes");
        claimantOtherType.setClaimantPensionWeeklyContribution("100");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_PENSION_YES));
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_PENSION_WEEKLY));
    }

    @Test
    void givenNoPensionContributionReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPensionContribution("No");
        claimantOtherType.setClaimantPensionWeeklyContribution(null);
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_PENSION_NO));
        assertNull(pdfMap.get(PdfMapperConstants.Q6_PENSION_WEEKLY));
    }

    @Test
    void givenNewEmploymentReflectsInMap() throws PdfMapperException {
        NewEmploymentType newEmploymentType = new NewEmploymentType();
        newEmploymentType.setNewlyEmployedFrom("26/09/2022");
        newEmploymentType.setNewPayBeforeTax("50000");
        caseData.setNewEmploymentType(newEmploymentType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q7_OTHER_JOB_YES));
    }

    @Test
    void givenNoNewEmploymentReflectsInMap() throws PdfMapperException {
        caseData.setNewEmploymentType(null);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q7_OTHER_JOB_NO));
    }
}

// TODO: claimant work address different to respondent
// TODO: Aware of multiple Cases
// TODO: Different Pay Cycles

// representative
// TODO: communication prefernces

// Additional Respondents
// TODO: 4 Respondents
// TODO: 5 Respondents

