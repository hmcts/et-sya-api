package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.NewEmploymentType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@SuppressWarnings({"PMD.TooManyMethods"})
class PdfMapperServiceTest {
    private static final Integer TOTAL_VALUES = 61;
    private PdfMapperService pdfMapperService;
    private CaseData caseData;
    private static final String ACAS_PREFIX = "2.3";

    @BeforeEach
    void setup() {
        pdfMapperService = new PdfMapperService();

        caseData = ResourceLoader.fromString(
            "responses/pdfMapperCaseDetails.json", CaseData.class
        );
    }

    @Test
    void givenCaseProducesPdfHeaderMap() {
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertEquals(TOTAL_VALUES, pdfMap.size());
    }

    @Test
    void givenPreferredContactAsEmailReflectsInMap() {
        ClaimantType claimantType = caseData.getClaimantType();
        claimantType.setClaimantContactPreference("Email");
        caseData.setClaimantType(claimantType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q1_CONTACT_EMAIL));
    }

    @Test
    void givenPreferredContactAsPostReflectsInMap() {
        ClaimantType claimantType = caseData.getClaimantType();
        claimantType.setClaimantContactPreference("Post");
        caseData.setClaimantType(claimantType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q1_CONTACT_POST));
    }

    @Test
    void givenAcasEarlyConciliationCertificateNumberReflectsInMap() {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentAcasQuestion(YES);
        respondentSumType.setRespondentAcas("1111");
        respondentSumType.setRespondentAcasNo(null);
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_NUMBER, ACAS_PREFIX)));
    }

    @Test
    void withoutAcasEarlyCertficateWithReasonUnfairDismissalReflectsInMap() {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentAcasQuestion(NO);
        respondentSumType.setRespondentAcas(null);
        respondentSumType.setRespondentAcasNo("Unfair Dismissal");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_A1, ACAS_PREFIX)));
    }

    @Test
    void withoutAcasEarlyCertficateWithReasonAnotherPersonReflectsInMap() {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentAcasQuestion(NO);
        respondentSumType.setRespondentAcas(null);
        respondentSumType.setRespondentAcasNo("Another person");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_A2, ACAS_PREFIX)));
    }

    @Test
    void withoutAcasEarlyCertficateWithReasonNoPowerReflectsInMap() {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentAcasQuestion(NO);
        respondentSumType.setRespondentAcas(null);
        respondentSumType.setRespondentAcasNo("No Power");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_A3, ACAS_PREFIX)));
    }

    @Test
    void withoutAcasEarlyCertficateWithReasonEmployerInTouchReflectsInMap() {
        RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
        respondentSumType.setRespondentAcasQuestion(NO);
        respondentSumType.setRespondentAcas(null);
        respondentSumType.setRespondentAcasNo("Employer already in touch");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_ACAS_A4, ACAS_PREFIX)));
    }

    @Test
    void givenDifferentWorkingAddressProducesClaimantWorkAddress() {
        Address claimantAddress = new Address();
        claimantAddress.setAddressLine1("Test");
        claimantAddress.setPostCode("MK1 1AY");
        ClaimantWorkAddressType claimantWorkAddressType = new ClaimantWorkAddressType();
        claimantWorkAddressType.setClaimantWorkAddress(claimantAddress);
        caseData.setClaimantWorkAddress(claimantWorkAddressType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q2_DIFFADDRESS_POSTCODE));
    }

    @Test
    void givenTwoRespondentsReflectsInMap() {
        caseData.setRespondentCollection(createRespondentList(2));
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q2_EMPLOYER_NAME));
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_NAME, "2.5 R2")));
    }

    @Test
    void givenThreeRespondentsReflectsInMap() {
        caseData.setRespondentCollection(createRespondentList(3));
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q2_EMPLOYER_NAME));
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_NAME, "2.5 R2")));
        assertNotNull(pdfMap.get(String.format(PdfMapperConstants.QX_NAME, "2.7 R3")));
    }

    @Test
    void givenClaimentDidntWorkForRespondentSkipsSection() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantEmployedFrom(null);
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNull(pdfMap.get(PdfMapperConstants.Q5_EMPLOYMENT_START));
    }

    @Test
    void givenContinuedEmploymentReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantEmployedCurrently(YES);
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q5_CONTINUING_YES));
        assertNull(pdfMap.get(PdfMapperConstants.Q5_EMPLOYMENT_END));
    }

    @Test
    void givenDiscontinuedEmploymentReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantEmployedCurrently(NO);
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q5_EMPLOYMENT_END));
        assertNull(pdfMap.get(PdfMapperConstants.Q5_CONTINUING_YES));
    }

    @Test
    void givenWeeklyPaymentCycleReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPayCycle("Weekly");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_GROSS_PAY_WEEKLY));
    }

    @Test
    void givenMonthlyPaymentCycleReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPayCycle("Monthly");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_GROSS_PAY_MONTHLY));
    }

    @Test
    void givenPensionContributionReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPensionContribution(YES);
        claimantOtherType.setClaimantPensionWeeklyContribution("100");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_PENSION_YES));
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_PENSION_WEEKLY));
    }

    @Test
    void givenNoPensionContributionReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPensionContribution(NO);
        claimantOtherType.setClaimantPensionWeeklyContribution(null);
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_PENSION_NO));
        assertNull(pdfMap.get(PdfMapperConstants.Q6_PENSION_WEEKLY));
    }

    @Test
    void givenNoticePeriodInWeeksReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantNoticePeriod(YES);
        claimantOtherType.setClaimantNoticePeriodUnit("Weeks");
        claimantOtherType.setClaimantNoticePeriodDuration("1");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_NOTICE_WEEKS));
    }

    @Test
    void givenNoticePeriodInMonthsReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantNoticePeriod(YES);
        claimantOtherType.setClaimantNoticePeriodUnit("Months");
        claimantOtherType.setClaimantNoticePeriodDuration("1");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_NOTICE_MONTHS));
    }

    @Test
    void givenNewEmploymentReflectsInMap() {
        NewEmploymentType newEmploymentType = new NewEmploymentType();
        newEmploymentType.setNewlyEmployedFrom("26/09/2022");
        newEmploymentType.setNewPayBeforeTax("50000");
        caseData.setNewEmploymentType(newEmploymentType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q7_OTHER_JOB_YES));
    }

    @Test
    void givenNoNewEmploymentReflectsInMap() {
        caseData.setNewEmploymentType(null);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q7_OTHER_JOB_NO));
    }

    @Test
    void givenNoRepresentativeReflectsInMap() {
        caseData.setRepresentativeClaimantType(null);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNull(pdfMap.get(PdfMapperConstants.Q11_REP_NAME));
    }

    @Test
    void givenRepresentativePostPreferenceRelectsInMap() {
        RepresentedTypeC representedTypeC = caseData.getRepresentativeClaimantType();
        representedTypeC.setRepresentativePreference("Post");
        caseData.setRepresentativeClaimantType(representedTypeC);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q11_CONTACT_POST));
    }

    @Test
    void shouldThrowExceptionWhenPrintPersonalDetails() {
        PdfMapperService pdfMapperService1 = Mockito.mock(PdfMapperService.class);
        when(pdfMapperService1.printPersonalDetails(caseData)).thenThrow(new NullPointerException("Test Exception"));
        assertDoesNotThrow(() -> pdfMapperService1.mapHeadersToPdf(caseData));
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

