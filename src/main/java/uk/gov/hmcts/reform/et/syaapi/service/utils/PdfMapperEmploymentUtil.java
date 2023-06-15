package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.NewEmploymentType;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.ANNUAL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.MONTHS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.NO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.NOTICE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.NO_LONGER_WORKING;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.NO_LOWERCASE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.WEEKS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.YES;


public final class PdfMapperEmploymentUtil {

    private PdfMapperEmploymentUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static void putEmploymentDetails(CaseData caseData, ConcurrentMap<String, Optional<String>> printFields) {
        if (!ObjectUtils.isEmpty(caseData) && !ObjectUtils.isEmpty(caseData.getClaimantOtherType())) {
            try {
                ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
                if (YES.equals(claimantOtherType.getPastEmployer())) {
                    putPastEmploymentDetails(caseData, printFields);
                } else if (NO.equals(claimantOtherType.getPastEmployer())) {
                    printFields.put(
                        PdfMapperConstants.Q4_EMPLOYED_BY_NO, Optional.of(YES));
                }
            } catch (Exception e) {
                GenericServiceUtil.logException("An error occurred while printing employment details to pdf file",
                                                caseData.getEthosCaseReference(),
                                                e.getMessage(),
                                                "PdfMapperEmploymentUtil",
                                                "putEmploymentDetails");
            }
        }
    }

    private static void putPastEmploymentDetails(CaseData caseData,
                                                 ConcurrentMap<String, Optional<String>> printFields) {
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        printFields.put(PdfMapperConstants.Q4_EMPLOYED_BY_YES, Optional.of(YES));
        printFields.put(
            PdfMapperConstants.Q5_EMPLOYMENT_START,
            ofNullable(PdfMapperServiceUtil.formatDate(claimantOtherType.getClaimantEmployedFrom()))
        );
        boolean isStillWorking = !NO_LONGER_WORKING.equals(claimantOtherType.getStillWorking());
        if (isStillWorking) {
            putStillWorkingFields(claimantOtherType, printFields);
        } else {
            putNoMoreWorkingFields(claimantOtherType, printFields);
        }
        printFields.put(
            PdfMapperConstants.Q5_DESCRIPTION,
            ofNullable(claimantOtherType.getClaimantOccupation())
        );
        putPastEmploymentRemuneration(claimantOtherType, printFields);
        if (caseData.getNewEmploymentType() != null) {
            putNewEmploymentFields(caseData, printFields);
        }
    }

    private static void putStillWorkingFields(ClaimantOtherType claimantOtherType,
                                              ConcurrentMap<String, Optional<String>> printFields) {
        printFields.put(PdfMapperConstants.Q5_CONTINUING_YES, Optional.of(YES));
        if (NOTICE.equals(claimantOtherType.getStillWorking())) {
            printFields.put(
                PdfMapperConstants.Q5_NOT_ENDED,
                ofNullable(PdfMapperServiceUtil.formatDate(
                    claimantOtherType.getClaimantEmployedNoticePeriod()))
            );
        }
    }

    private static void putNoMoreWorkingFields(ClaimantOtherType claimantOtherType,
                                               ConcurrentMap<String, Optional<String>> printFields) {
        printFields.put(PdfMapperConstants.Q5_CONTINUING_NO, Optional.of(NO_LOWERCASE));
        printFields.put(
            PdfMapperConstants.Q5_EMPLOYMENT_END,
            ofNullable(PdfMapperServiceUtil.formatDate(claimantOtherType.getClaimantEmployedTo()))
        );
    }

    private static void putPastEmploymentRemuneration(ClaimantOtherType claimantOtherType,
                                                      ConcurrentMap<String, Optional<String>> printFields) {
        printFields.put(
            PdfMapperConstants.Q6_HOURS,
            ofNullable(claimantOtherType.getClaimantAverageWeeklyHours())
        );
        printFields.put(
            PdfMapperConstants.Q6_GROSS_PAY,
            ofNullable(claimantOtherType.getClaimantPayBeforeTax())
        );
        printFields.put(PdfMapperConstants.Q6_NET_PAY, ofNullable(claimantOtherType.getClaimantPayAfterTax()));
        putClaimantPayCycle(claimantOtherType, printFields);
        putClaimantNoticePeriod(claimantOtherType, printFields);
        putClaimantPensionContribution(claimantOtherType, printFields);
        printFields.put(
            PdfMapperConstants.Q6_OTHER_BENEFITS,
            ofNullable(claimantOtherType.getClaimantBenefitsDetail())
        );
    }

    private static void putClaimantPayCycle(ClaimantOtherType claimantOtherType,
                                     ConcurrentMap<String, Optional<String>> printFields) {
        if (claimantOtherType.getClaimantPayCycle() != null) {
            switch (claimantOtherType.getClaimantPayCycle()) {
                case WEEKS:
                    printFields.put(PdfMapperConstants.Q6_GROSS_PAY_WEEKLY, Optional.of(PdfMapperConstants.WEEKLY));
                    printFields.put(PdfMapperConstants.Q6_NET_PAY_WEEKLY, Optional.of(PdfMapperConstants.WEEKLY));
                    break;
                case MONTHS:
                    printFields.put(PdfMapperConstants.Q6_GROSS_PAY_MONTHLY, Optional.of(PdfMapperConstants.MONTHLY));
                    printFields.put(PdfMapperConstants.Q6_NET_PAY_MONTHLY, Optional.of(PdfMapperConstants.MONTHLY));
                    break;
                case ANNUAL:
                    printFields.put(PdfMapperConstants.Q6_GROSS_PAY_ANNUAL, Optional.of(PdfMapperConstants.ANNUALLY));
                    printFields.put(PdfMapperConstants.Q6_NET_PAY_ANNUAL, Optional.of(PdfMapperConstants.ANNUALLY));
                    break;
                default:
                    break;
            }
        }
    }

    public static void putClaimantNoticePeriod(ClaimantOtherType claimantOtherType,
                                               ConcurrentMap<String, Optional<String>> printFields) {
        // Section 6.3
        if (claimantOtherType.getClaimantNoticePeriod() != null
            && NO_LONGER_WORKING.equals(claimantOtherType.getStillWorking())) {
            if (YES.equals(claimantOtherType.getClaimantNoticePeriod())) {
                printFields.put(
                    PdfMapperConstants.Q6_PAID_NOTICE_YES, Optional.of(YES)
                );
                String noticeUnit = claimantOtherType.getClaimantNoticePeriodUnit();
                if (WEEKS.equals(noticeUnit)) {
                    printFields.put(
                        PdfMapperConstants.Q6_NOTICE_WEEKS,
                        ofNullable(claimantOtherType.getClaimantNoticePeriodDuration())
                    );
                } else if (MONTHS.equals(noticeUnit)) {
                    printFields.put(
                        PdfMapperConstants.Q6_NOTICE_MONTHS,
                        ofNullable(claimantOtherType.getClaimantNoticePeriodDuration())
                    );
                }
            } else if (NO.equals(claimantOtherType.getClaimantNoticePeriod())) {
                printFields.put(PdfMapperConstants.Q6_PAID_NOTICE_NO, Optional.of(NO));
            }
        }
    }

    public static void putClaimantPensionContribution(ClaimantOtherType claimantOtherType,
                                                      ConcurrentMap<String, Optional<String>> printFields) {
        // Section 6.4
        if (claimantOtherType.getClaimantPensionContribution() != null) {
            String pensionContributionYesNo = claimantOtherType.getClaimantPensionContribution().isEmpty()
                ? NO
                : claimantOtherType.getClaimantPensionContribution();
            if (YES.equals(pensionContributionYesNo)) {
                printFields.put(
                    PdfMapperConstants.Q6_PENSION_YES,
                    ofNullable(claimantOtherType.getClaimantPensionContribution())
                );
                printFields.put(
                    PdfMapperConstants.Q6_PENSION_WEEKLY,
                    ofNullable(claimantOtherType.getClaimantPensionWeeklyContribution())
                );

            } else {
                if (NO.equals(pensionContributionYesNo)) {
                    printFields.put(PdfMapperConstants.Q6_PENSION_NO, Optional.of(NO));
                }
            }
        }

    }

    private static void putNewEmploymentFields(CaseData caseData, ConcurrentMap<String, Optional<String>> printFields) {
        NewEmploymentType newEmploymentType = caseData.getNewEmploymentType();
        if (newEmploymentType.getNewJob() != null) {
            if (YES.equals(newEmploymentType.getNewJob())) {
                printFields.put(PdfMapperConstants.Q7_OTHER_JOB_YES, Optional.of(YES));
                printFields.put(
                    PdfMapperConstants.Q7_START_WORK,
                    ofNullable(PdfMapperServiceUtil.formatDate(newEmploymentType.getNewlyEmployedFrom()))
                );
                printFields.put(
                    PdfMapperConstants.Q7_EARNING,
                    ofNullable(newEmploymentType.getNewPayBeforeTax())
                );
                putJobPayInterval(newEmploymentType, printFields);
            } else if (NO.equals(newEmploymentType.getNewJob())) {
                printFields.put(PdfMapperConstants.Q7_OTHER_JOB_NO, Optional.of(NO));
            }
        }
    }

    private static void putJobPayInterval(NewEmploymentType newEmploymentType,
                                   ConcurrentMap<String, Optional<String>> printFields) {
        if (WEEKS.equals(newEmploymentType.getNewJobPayInterval())) {
            printFields.put(PdfMapperConstants.Q7_EARNING_WEEKLY, Optional.of(PdfMapperConstants.WEEKLY));
        } else if (MONTHS.equals(newEmploymentType.getNewJobPayInterval())) {
            printFields.put(PdfMapperConstants.Q7_EARNING_MONTHLY, Optional.of(PdfMapperConstants.MONTHLY));
        } else if (ANNUAL.equals(newEmploymentType.getNewJobPayInterval())) {
            printFields.put(PdfMapperConstants.Q7_EARNING_ANNUAL, Optional.of(PdfMapperConstants.ANNUALLY));
        }
    }
}
