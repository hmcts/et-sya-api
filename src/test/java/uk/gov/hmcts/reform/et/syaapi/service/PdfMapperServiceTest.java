package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.NewEmploymentType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.constants.PdfMapperConstants;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"PMD.TooManyMethods"})
class PdfMapperServiceTest {
    private static final Integer TOTAL_VALUES = 56;
    private PdfMapperService pdfMapperService;
    private CaseData caseData;
    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String ACAS_PREFIX = "2.3";

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
        respondentSumType.setRespondentACASQuestion(YES);
        respondentSumType.setRespondentACAS("1111");
        respondentSumType.setRespondentACASNo(null);
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_NUMBER, ACAS_PREFIX)));
    }

    @Test
    void withoutAcasEarlyCertficateWithReasonUnfairDismissalReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentACASQuestion(NO);
        respondentSumType.setRespondentACAS(null);
        respondentSumType.setRespondentACASNo("Unfair Dismissal");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_A1, ACAS_PREFIX)));
    }

    @Test
    void withoutAcasEarlyCertficateWithReasonAnotherPersonReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentACASQuestion(NO);
        respondentSumType.setRespondentACAS(null);
        respondentSumType.setRespondentACASNo("Another person");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_A2, ACAS_PREFIX)));
    }

    @Test
    void withoutAcasEarlyCertficateWithReasonNoPowerReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentACASQuestion(NO);
        respondentSumType.setRespondentACAS(null);
        respondentSumType.setRespondentACASNo("No Power");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_A3, ACAS_PREFIX)));
    }

    @Test
    void withoutAcasEarlyCertficateWithReasonEmployerInTouchReflectsInMap() throws PdfMapperException {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentACASQuestion(NO);
        respondentSumType.setRespondentACAS(null);
        respondentSumType.setRespondentACASNo("Employer already in touch");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_A4, ACAS_PREFIX)));
    }

    @Test
    void givenDifferentWorkingAddressProducesClaimantWorkAddress() throws PdfMapperException {
        Address claimantAddress = new Address();
        claimantAddress.setAddressLine1("Test");
        claimantAddress.setPostCode("MK1 1AY");
        ClaimantWorkAddressType claimantWorkAddressType = new ClaimantWorkAddressType();
        claimantWorkAddressType.setClaimantWorkAddress(claimantAddress);
        caseData.setClaimantWorkAddress(claimantWorkAddressType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q2_DIFFADDRESS_POSTCODE));
    }

    @Test
    void givenTwoRespondentsReflectsInMap() throws PdfMapperException {
        caseData.setRespondentCollection(createRespondentList(2));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q2_EMPLOYER_NAME));
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_NAME, "2.5 R2")));
    }

    @Test
    void givenThreeRespondentsReflectsInMap() throws PdfMapperException {
        caseData.setRespondentCollection(createRespondentList(3));
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q2_EMPLOYER_NAME));
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_NAME, "2.5 R2")));
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_NAME, "2.7 R3")));
    }

    @Test
    void givenSixRespondentsProducesPdfException() {
        caseData.setRespondentCollection(createRespondentList(6));
        assertThrows(
            PdfMapperException.class,
            () -> pdfMapperService.mapHeadersToPdf(caseData));
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
        claimantOtherType.setClaimantEmployedCurrently(YES);
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q5_CONTINUING_YES));
        assertNull(pdfMap.get(PdfMapperConstants.Q5_EMPLOYMENT_END));
    }

    @Test
    void givenDiscontinuedEmploymentReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantEmployedCurrently(NO);
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q5_EMPLOYMENT_END));
        assertNull(pdfMap.get(PdfMapperConstants.Q5_CONTINUING_YES));
    }

    @Test
    void givenWeeklyPaymentCycleReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPayCycle("Weekly");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_GROSS_PAY_WEEKLY));
    }

    @Test
    void givenMonthlyPaymentCycleReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPayCycle("Monthly");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_GROSS_PAY_MONTHLY));
    }

    @Test
    void givenPensionContributionReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPensionContribution(YES);
        claimantOtherType.setClaimantPensionWeeklyContribution("100");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_PENSION_YES));
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_PENSION_WEEKLY));
    }

    @Test
    void givenNoPensionContributionReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPensionContribution(NO);
        claimantOtherType.setClaimantPensionWeeklyContribution(null);
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_PENSION_NO));
        assertNull(pdfMap.get(PdfMapperConstants.Q6_PENSION_WEEKLY));
    }

    @Test
    void givenNoticePeriodInWeeksReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantNoticePeriod(YES);
        claimantOtherType.setClaimantNoticePeriodUnit("Weeks");
        claimantOtherType.setClaimantNoticePeriodDuration("1");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_NOTICE_WEEKS));
    }

    @Test
    void givenNoticePeriodInMonthsReflectsInMap() throws PdfMapperException {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantNoticePeriod(YES);
        claimantOtherType.setClaimantNoticePeriodUnit("Months");
        claimantOtherType.setClaimantNoticePeriodDuration("1");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_NOTICE_MONTHS));
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

    @Test
    void givenNoRepresentativeReflectsInMap() throws PdfMapperException {
        caseData.setRepresentativeClaimantType(null);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNull(pdfMap.get(PdfMapperConstants.Q11_REP_NAME));
    }

    @Test
    void givenRepresentativePostPreferenceRelectsInMap() throws PdfMapperException {
        RepresentedTypeC representedTypeC = caseData.getRepresentativeClaimantType();
        representedTypeC.setRepresentativePreference("Post");
        caseData.setRepresentativeClaimantType(representedTypeC);
        Map<String, String> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q11_CONTACT_POST));
    }

    private List<RespondentSumTypeItem> createRespondentList(int count) {
        List<RespondentSumTypeItem> returnList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
            returnList.add(createResponent(respondentSumType));
        }
        return returnList;
    }

    private RespondentSumTypeItem createResponent(RespondentSumType respondentSumType) {
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        return respondentSumTypeItem;
    }
}

