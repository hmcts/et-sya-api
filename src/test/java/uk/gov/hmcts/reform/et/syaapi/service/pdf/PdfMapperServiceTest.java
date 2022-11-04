package uk.gov.hmcts.reform.et.syaapi.service.pdf;

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
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperConstants.Q1_DOB_DAY;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperConstants.Q1_DOB_MONTH;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperConstants.Q1_DOB_YEAR;

@SuppressWarnings({"PMD.TooManyMethods"})
class PdfMapperServiceTest {
    private static final Integer TOTAL_VALUES = 82;
    private PdfMapperService pdfMapperService;
    private CaseData caseData;
    private static final String ACAS_PREFIX = "2.3";

    private static final String POST = "Post";

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
        claimantType.setClaimantContactPreference(POST);
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
    void withoutAcasEarlyCertificateWithReasonUnfairDismissalReflectsInMap() {
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
    void withoutAcasEarlyCertificateWithReasonAnotherPersonReflectsInMap() {
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
    void withoutAcasEarlyCertificateWithReasonNoPowerReflectsInMap() {
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
    void withoutAcasEarlyCertificateWithReasonEmployerInTouchReflectsInMap() {
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
    void givenClaimantDidntWorkForRespondentSkipsSection() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setPastEmployer(NO);
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNull(pdfMap.get(PdfMapperConstants.Q5_EMPLOYMENT_START));
    }

    @Test
    void givenContinuedEmploymentReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setStillWorking("Working");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q5_CONTINUING_YES));
        assertNull(pdfMap.get(PdfMapperConstants.Q5_EMPLOYMENT_END));
    }

    @Test
    void givenDiscontinuedEmploymentReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q5_EMPLOYMENT_END));
        assertNull(pdfMap.get(PdfMapperConstants.Q5_CONTINUING_YES));
    }

    @Test
    void givenWeeklyPaymentCycleReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPayCycle("Weeks");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_GROSS_PAY_WEEKLY));
        assertNull(pdfMap.get(PdfMapperConstants.Q6_GROSS_PAY_ANNUAL));
    }

    @Test
    void givenMonthlyPaymentCycleReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPayCycle("Months");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_GROSS_PAY_MONTHLY));
        assertNull(pdfMap.get(PdfMapperConstants.Q6_GROSS_PAY_ANNUAL));
    }

    @Test
    void givenAnnualPaymentCycleReflectsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setClaimantPayCycle("Annual");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q6_GROSS_PAY_ANNUAL));
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
    void givenNoticePeriodEndDateAppearsInMap() {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        claimantOtherType.setStillWorking("Notice");
        claimantOtherType.setClaimantEmployedNoticePeriod("2018-02-05");
        caseData.setClaimantOtherType(claimantOtherType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q5_NOT_ENDED));
    }

    @Test
    void givenNewEmploymentReflectsInMap() {
        NewEmploymentType newEmploymentType = new NewEmploymentType();
        newEmploymentType.setNewJob("Yes");
        newEmploymentType.setNewlyEmployedFrom("26/09/2022");
        newEmploymentType.setNewPayBeforeTax("50000");
        caseData.setNewEmploymentType(newEmploymentType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q7_OTHER_JOB_YES));
    }

    @Test
    void givenNewEmploymentWithNoNewJobReflectsInMap() {
        NewEmploymentType newEmploymentType = new NewEmploymentType();
        newEmploymentType.setNewJob("No");
        caseData.setNewEmploymentType(newEmploymentType);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q7_OTHER_JOB_NO));
    }

    @Test
    void givenNoNewEmploymentReflectsInMap() {
        caseData.setNewEmploymentType(null);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNull(pdfMap.get(PdfMapperConstants.Q7_OTHER_JOB_NO));
        assertNull(pdfMap.get(PdfMapperConstants.Q7_OTHER_JOB_YES));
    }

    @Test
    void givenNoRepresentativeReflectsInMap() {
        caseData.setRepresentativeClaimantType(null);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNull(pdfMap.get(PdfMapperConstants.Q11_REP_NAME));
    }

    @Test
    void givenRepresentativePostPreferenceReflectsInMap() {
        RepresentedTypeC representedTypeC = caseData.getRepresentativeClaimantType();
        representedTypeC.setRepresentativePreference(POST);
        caseData.setRepresentativeClaimantType(representedTypeC);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(caseData);
        assertNotNull(pdfMap.get(PdfMapperConstants.Q11_CONTACT_POST));
    }

    // Todo the test is throwing but passing still. Quite a few of these tests might be doing the same.
    @Test
    void shouldNotThrowExceptionWhenClaimantIndTypeIsNull() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.setClaimantIndType(null);
        assertDoesNotThrow(() -> pdfMapperService.mapHeadersToPdf(exceptionCaseData));
    }

    @Test
    void shouldNotThrowExceptionWhenRepresentativeAddressIsNull() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getRepresentativeClaimantType().setRepresentativeAddress(null);
        assertDoesNotThrow(() -> pdfMapperService.mapHeadersToPdf(exceptionCaseData));
    }

    @Test
    void shouldNotThrowExceptionWhenClaimantSexIsNull() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getClaimantIndType().setClaimantSex(null);
        assertDoesNotThrow(() -> pdfMapperService.mapHeadersToPdf(exceptionCaseData));
    }

    @Test
    void shouldSetFemaleWhenClaimantSexIsFemale() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getClaimantIndType().setClaimantSex("Female");
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(exceptionCaseData);
        assertEquals(pdfMap.get(PdfMapperConstants.Q1_SEX_FEMALE), Optional.of("female"));
    }

    @Test
    void shouldSetPreferNotToSayWhenClaimantSexIsPreferNotToSay() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getClaimantIndType().setClaimantSex("Prefer not to say");
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(exceptionCaseData);
        assertEquals(pdfMap.get(PdfMapperConstants.Q1_SEX_PREFER_NOT_TO_SAY), Optional.of("prefer not to say"));
    }

    @Test
    void shouldNotThrowExceptionWhenClaimantTypeIsNull() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.setClaimantType(null);
        assertDoesNotThrow(() -> pdfMapperService.mapHeadersToPdf(exceptionCaseData));
    }

    @Test
    void shouldNotThrowExceptionWhenClaimantAddressUkIsNull() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getClaimantType().setClaimantAddressUK(null);
        assertDoesNotThrow(() -> pdfMapperService.mapHeadersToPdf(exceptionCaseData));
    }

    @Test
    void shouldSetContactPreferencePostWhenClaimantClaimantContactPreferenceIsPost() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getClaimantType().setClaimantContactPreference(POST);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(exceptionCaseData);
        assertEquals(pdfMap.get(PdfMapperConstants.Q1_CONTACT_POST), Optional.of(POST));
    }

    @Test
    void shouldNotThrowExceptionWhenClaimantHearingPreferenceIsNull() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.setClaimantHearingPreference(null);
        assertDoesNotThrow(() -> pdfMapperService.mapHeadersToPdf(exceptionCaseData));
    }

    @Test
    void shouldNotThrowExceptionWhenHearingPreferencesIsNull() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getClaimantHearingPreference().setHearingPreferences(null);
        assertDoesNotThrow(() -> pdfMapperService.mapHeadersToPdf(exceptionCaseData));
    }

    @Test
    void shouldNotThrowExceptionWhenHearingPreferencesIsEmpty() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getClaimantHearingPreference().setHearingPreferences(new ArrayList<>());
        assertDoesNotThrow(() -> pdfMapperService.mapHeadersToPdf(exceptionCaseData));
    }

    @Test
    void shouldNotThrowWhenDobIsNull() {
        caseData.getClaimantIndType().setClaimantDateOfBirth(null);
        Map<String, Optional<String>> pdfFields = pdfMapperService.mapHeadersToPdf(caseData);

        assertNull(pdfFields.get(Q1_DOB_DAY));
        assertNull(pdfFields.get(Q1_DOB_MONTH));
        assertNull(pdfFields.get(Q1_DOB_YEAR));
    }

    @Test
    void shouldSetICanTakePartInNoHearingsWhenVideoAndPhoneNotSelected() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getClaimantHearingPreference().setHearingPreferences(new ArrayList<>());
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(exceptionCaseData);
        assertEquals(pdfMap.get(PdfMapperConstants.I_CAN_TAKE_PART_IN_NO_HEARINGS), Optional.of(YES));
    }

    @Test
    void shouldSetICanTakePartInVideoHearingsWhenVideoSelected() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getClaimantHearingPreference().setHearingPreferences(List.of("Video"));
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(exceptionCaseData);
        assertEquals(pdfMap.get(PdfMapperConstants.I_CAN_TAKE_PART_IN_VIDEO_HEARINGS), Optional.of(YES));
    }

    @Test
    void shouldSetICanTakePartInPhoneHearingsWhenPhoneSelected() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getClaimantHearingPreference().setHearingPreferences(List.of("Phone"));
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(exceptionCaseData);
        assertEquals(pdfMap.get(PdfMapperConstants.I_CAN_TAKE_PART_IN_PHONE_HEARINGS), Optional.of(YES));
    }

    @Test
    void shouldSetDisabilityYesWhenReasonableAdjustmentsYes() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getClaimantHearingPreference().setReasonableAdjustments(YES);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(exceptionCaseData);
        assertEquals(pdfMap.get(PdfMapperConstants.Q12_DISABILITY_YES), Optional.of(YES));
    }

    @Test
    void shouldSetDisabilityNoWhenReasonableAdjustmentsNo() {
        CaseData exceptionCaseData = new TestData().getCaseData();
        exceptionCaseData.getClaimantHearingPreference().setReasonableAdjustments(NO);
        Map<String, Optional<String>> pdfMap = pdfMapperService.mapHeadersToPdf(exceptionCaseData);
        assertEquals(pdfMap.get(PdfMapperConstants.Q12_DISABILITY_NO), Optional.of("no"));
    }

    @Test
    void shouldReturnEmptyMapWhenCaseDataIsNull() {
        assertEquals(pdfMapperService.mapHeadersToPdf(null), new ConcurrentHashMap<>());
    }

    private List<RespondentSumTypeItem> createRespondentList(int count) {
        List<RespondentSumTypeItem> returnList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
            returnList.add(createRespondent(respondentSumType));
        }
        return returnList;
    }

    private RespondentSumTypeItem createRespondent(RespondentSumType respondentSumType) {
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        return respondentSumTypeItem;
    }
}

