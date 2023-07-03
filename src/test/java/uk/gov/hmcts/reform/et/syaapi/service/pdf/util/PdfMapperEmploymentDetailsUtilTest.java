package uk.gov.hmcts.reform.et.syaapi.service.pdf.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.NewEmploymentType;
import uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants;
import uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperEmploymentDetailsUtil;
import uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperServiceUtil;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.ANNUAL;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.MONTHS;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.NO;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.NOTICE;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.NO_LONGER_WORKING;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.NO_LOWERCASE;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.WEEKS;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.YES;

class PdfMapperEmploymentDetailsUtilTest {

    @ParameterizedTest
    @NullSource
    @MethodSource("uk.gov.hmcts.reform.et.syaapi.service.util.data.PdfMapperEmploymentDetailsUtilTestDataProvider#"
        + "generateClaimantOtherTypes")
    void putEmploymentDetails(CaseData caseData) {
        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        PdfMapperEmploymentDetailsUtil.putEmploymentDetails(caseData, printFields);
        if (ObjectUtils.isEmpty(caseData) || ObjectUtils.isEmpty(caseData.getClaimantOtherType())) {
            assertThat(printFields.get(PdfMapperConstants.Q4_EMPLOYED_BY_NO)).isNull();
            assertThat(printFields.get(PdfMapperConstants.Q4_EMPLOYED_BY_YES)).isNull();
        } else {
            ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
            if (YES.equals(claimantOtherType.getPastEmployer())) {
                checkPastEmploymentDetails(caseData, printFields);
            } else if (NO.equals(claimantOtherType.getPastEmployer())) {
                assertThat(printFields.get(PdfMapperConstants.Q4_EMPLOYED_BY_NO)).contains(YES);
            }
        }
    }

    private static void checkPastEmploymentDetails(CaseData caseData,
                                                   ConcurrentMap<String, Optional<String>> printFields) {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        assertThat(printFields.get(PdfMapperConstants.Q4_EMPLOYED_BY_YES)).contains(YES);
        assertThat(printFields.get(PdfMapperConstants.Q5_EMPLOYMENT_START)).contains(
            PdfMapperServiceUtil.formatDate(claimantOtherType.getClaimantEmployedFrom()));
        boolean isStillWorking = !NO_LONGER_WORKING.equals(claimantOtherType.getStillWorking());
        if (isStillWorking) {
            checkStillWorkingFields(claimantOtherType, printFields);
        } else {
            checkNoMoreWorkingFields(claimantOtherType, printFields);
        }
        assertThat(printFields.get(PdfMapperConstants.Q5_DESCRIPTION)).contains(
            claimantOtherType.getClaimantOccupation());
        checkPastEmploymentRemuneration(claimantOtherType, printFields);
        if (caseData.getNewEmploymentType() != null) {
            checkNewEmploymentFields(caseData, printFields);
        }
    }

    private static void checkStillWorkingFields(ClaimantOtherType claimantOtherType,
                                                ConcurrentMap<String, Optional<String>> printFields) {
        assertThat(printFields.get(PdfMapperConstants.Q5_CONTINUING_YES)).contains(YES);
        if (NOTICE.equals(claimantOtherType.getStillWorking())) {
            assertThat(printFields.get(PdfMapperConstants.Q5_CONTINUING_YES)).contains(YES);
            assertThat(printFields.get(PdfMapperConstants.Q5_NOT_ENDED)).contains(
                PdfMapperServiceUtil.formatDate(claimantOtherType.getClaimantEmployedNoticePeriod()));
        }
    }

    private static void checkNoMoreWorkingFields(ClaimantOtherType claimantOtherType,
                                                 ConcurrentMap<String, Optional<String>> printFields) {
        assertThat(printFields.get(PdfMapperConstants.Q5_CONTINUING_NO)).contains(NO_LOWERCASE);
        assertThat(printFields.get(PdfMapperConstants.Q5_EMPLOYMENT_END)).contains(
            PdfMapperServiceUtil.formatDate(claimantOtherType.getClaimantEmployedTo()));
    }

    private static void checkPastEmploymentRemuneration(ClaimantOtherType claimantOtherType,
                                                        ConcurrentMap<String, Optional<String>> printFields) {
        assertThat(printFields.get(PdfMapperConstants.Q6_HOURS)).contains(
            claimantOtherType.getClaimantAverageWeeklyHours());
        assertThat(printFields.get(PdfMapperConstants.Q6_GROSS_PAY)).contains(
            claimantOtherType.getClaimantPayBeforeTax());
        assertThat(printFields.get(PdfMapperConstants.Q6_NET_PAY)).contains(
            claimantOtherType.getClaimantPayAfterTax());
        checkClaimantPayCycle(claimantOtherType, printFields);
        checkClaimantNoticePeriod(claimantOtherType, printFields);
        checkClaimantPensionContribution(claimantOtherType, printFields);
        printFields.put(
            PdfMapperConstants.Q6_OTHER_BENEFITS,
            ofNullable(claimantOtherType.getClaimantBenefitsDetail())
        );
    }

    private static void checkClaimantPayCycle(ClaimantOtherType claimantOtherType,
                                              ConcurrentMap<String, Optional<String>> printFields) {
        if (claimantOtherType.getClaimantPayCycle() != null) {
            switch (claimantOtherType.getClaimantPayCycle()) {
                case WEEKS:
                    assertThat(printFields.get(PdfMapperConstants.Q6_GROSS_PAY_WEEKLY))
                        .contains(PdfMapperConstants.WEEKLY);
                    assertThat(printFields.get(PdfMapperConstants.Q6_NET_PAY_WEEKLY))
                        .contains(PdfMapperConstants.WEEKLY);
                    break;
                case MONTHS:
                    assertThat(printFields.get(PdfMapperConstants.Q6_GROSS_PAY_MONTHLY))
                        .contains(PdfMapperConstants.MONTHLY);
                    assertThat(printFields.get(PdfMapperConstants.Q6_NET_PAY_MONTHLY))
                        .contains(PdfMapperConstants.MONTHLY);
                    break;
                case ANNUAL:
                    assertThat(printFields.get(PdfMapperConstants.Q6_GROSS_PAY_MONTHLY))
                        .contains(PdfMapperConstants.ANNUALLY);
                    assertThat(printFields.get(PdfMapperConstants.Q6_NET_PAY_MONTHLY))
                        .contains(PdfMapperConstants.ANNUALLY);
                    break;
                default:
                    break;
            }
        }
    }

    public static void checkClaimantNoticePeriod(ClaimantOtherType claimantOtherType,
                                                 ConcurrentMap<String, Optional<String>> printFields) {
        // Section 6.3
        if (claimantOtherType.getClaimantNoticePeriod() != null
            && NO_LONGER_WORKING.equals(claimantOtherType.getStillWorking())) {
            if (YES.equals(claimantOtherType.getClaimantNoticePeriod())) {
                assertThat(printFields.get(PdfMapperConstants.Q6_PAID_NOTICE_YES)).contains(YES);
                String noticeUnit = claimantOtherType.getClaimantNoticePeriodUnit();
                if (WEEKS.equals(noticeUnit)) {
                    assertThat(printFields.get(PdfMapperConstants.Q6_NOTICE_WEEKS)).contains(
                        claimantOtherType.getClaimantNoticePeriodDuration());
                } else if (MONTHS.equals(noticeUnit)) {
                    assertThat(printFields.get(PdfMapperConstants.Q6_NOTICE_MONTHS)).contains(
                        claimantOtherType.getClaimantNoticePeriodDuration());
                }
            } else if (NO.equals(claimantOtherType.getClaimantNoticePeriod())) {
                assertThat(printFields.get(PdfMapperConstants.Q6_PAID_NOTICE_NO)).contains(NO);
            }
        }
    }

    public static void checkClaimantPensionContribution(ClaimantOtherType claimantOtherType,
                                                        ConcurrentMap<String, Optional<String>> printFields) {
        // Section 6.4
        if (claimantOtherType.getClaimantPensionContribution() != null) {
            String pensionContributionYesNo = claimantOtherType.getClaimantPensionContribution().isEmpty()
                ? NO
                : claimantOtherType.getClaimantPensionContribution();
            if (YES.equals(pensionContributionYesNo)) {
                assertThat(printFields.get(PdfMapperConstants.Q6_PENSION_YES)).contains(
                    claimantOtherType.getClaimantPensionContribution());
                assertThat(printFields.get(PdfMapperConstants.Q6_PENSION_WEEKLY)).contains(
                    claimantOtherType.getClaimantPensionWeeklyContribution());
            } else {
                if (NO.equals(pensionContributionYesNo)) {
                    assertThat(printFields.get(PdfMapperConstants.Q6_PENSION_NO)).contains(NO);
                }
            }
        }
    }

    private static void checkNewEmploymentFields(CaseData caseData,
                                                 ConcurrentMap<String, Optional<String>> printFields) {
        NewEmploymentType newEmploymentType = caseData.getNewEmploymentType();
        if (newEmploymentType.getNewJob() != null) {
            if (YES.equals(newEmploymentType.getNewJob())) {
                assertThat(printFields.get(PdfMapperConstants.Q7_OTHER_JOB_YES)).contains(YES);
                assertThat(printFields.get(PdfMapperConstants.Q7_START_WORK)).contains(
                    PdfMapperServiceUtil.formatDate(newEmploymentType.getNewlyEmployedFrom()));
                assertThat(printFields.get(PdfMapperConstants.Q7_EARNING)).contains(
                    PdfMapperServiceUtil.formatDate(newEmploymentType.getNewPayBeforeTax()));
                checkJobPayInterval(newEmploymentType, printFields);
            } else if (NO.equals(newEmploymentType.getNewJob())) {
                assertThat(printFields.get(PdfMapperConstants.Q7_OTHER_JOB_NO)).contains(NO);
            }
        }
    }

    private static void checkJobPayInterval(NewEmploymentType newEmploymentType,
                                          ConcurrentMap<String, Optional<String>> printFields) {
        if (WEEKS.equals(newEmploymentType.getNewJobPayInterval())) {
            assertThat(printFields.get(PdfMapperConstants.Q7_EARNING_WEEKLY)).contains(PdfMapperConstants.WEEKLY);
        } else if (MONTHS.equals(newEmploymentType.getNewJobPayInterval())) {
            assertThat(printFields.get(PdfMapperConstants.Q7_EARNING_WEEKLY)).contains(PdfMapperConstants.MONTHLY);
        } else if (ANNUAL.equals(newEmploymentType.getNewJobPayInterval())) {
            assertThat(printFields.get(PdfMapperConstants.Q7_EARNING_WEEKLY)).contains(PdfMapperConstants.ANNUALLY);
        }
    }
}
