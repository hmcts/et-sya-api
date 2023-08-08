package uk.gov.hmcts.reform.et.syaapi.service.utils.data;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.NewEmploymentType;

import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.AVERAGE_WEEKLY_HOURS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_BENEFITS_DETAIL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_EMPLOYED_FROM;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_EMPLOYED_TO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_OCCUPATION;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_PAY_AFTER_TAX;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_PAY_BEFORE_TAX;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_PENSION_WEEKLY_CONTRIBUTION;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NEW_PAY_BEFORE_TAX;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NO_LONGER_WORKING;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WEEKS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

public final class PdfMapperEmploymentDetailsUtilTestDataProvider {

    private PdfMapperEmploymentDetailsUtilTestDataProvider() {
        // Utility classes should not have a public or default constructor.
    }

    public static Stream<Arguments> generateClaimantOtherTypes() {
        // Past employer, not working anymore, Pay Cycle Weekly, Notice Period Yes, Notice Period Unit Week,
        // Pension Contribution Yes, Other benefits car
        ClaimantOtherType claimantOtherType = new ClaimantOtherType();
        claimantOtherType.setPastEmployer(YES);
        claimantOtherType.setClaimantEmployedFrom(CLAIMANT_EMPLOYED_FROM);
        claimantOtherType.setStillWorking(NO_LONGER_WORKING);
        claimantOtherType.setClaimantEmployedTo(CLAIMANT_EMPLOYED_TO);
        claimantOtherType.setClaimantOccupation(CLAIMANT_OCCUPATION);
        claimantOtherType.setClaimantAverageWeeklyHours(AVERAGE_WEEKLY_HOURS);
        claimantOtherType.setClaimantPayBeforeTax(CLAIMANT_PAY_BEFORE_TAX);
        claimantOtherType.setClaimantPayAfterTax(CLAIMANT_PAY_AFTER_TAX);
        claimantOtherType.setClaimantPayCycle(WEEKS);
        claimantOtherType.setClaimantNoticePeriod(YES);
        claimantOtherType.setClaimantNoticePeriodUnit(WEEKS);
        claimantOtherType.setClaimantNoticePeriodDuration("4");
        claimantOtherType.setClaimantPensionWeeklyContribution(CLAIMANT_PENSION_WEEKLY_CONTRIBUTION);
        claimantOtherType.setClaimantBenefitsDetail(CLAIMANT_BENEFITS_DETAIL);
        // New Employment Type is filled and NewJob is Yes, New Employment Type payment is weekly
        NewEmploymentType newEmploymentType = new NewEmploymentType();
        newEmploymentType.setNewJob(YES);
        newEmploymentType.setNewlyEmployedFrom(CLAIMANT_EMPLOYED_FROM);
        newEmploymentType.setNewPayBeforeTax(NEW_PAY_BEFORE_TAX);
        newEmploymentType.setNewJobPayInterval(WEEKS);
        CaseData caseData1 = new CaseData();
        caseData1.setClaimantOtherType(claimantOtherType);
        caseData1.setNewEmploymentType(newEmploymentType);

        return Stream.of(
            Arguments.of(caseData1)
        );
    }

}
