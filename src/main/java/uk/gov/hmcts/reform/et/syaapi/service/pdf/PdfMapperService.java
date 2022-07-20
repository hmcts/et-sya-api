package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.NewEmploymentType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * Maps Case Data attributes to fields within the PDF template.
 * Inputs that are accepted from the ET1 form can then be mapped to
 * the corresponding questions within the template PDF Provided
 */
@Service
public class PdfMapperService {
    private static final  Map<String, String> TITLES = Map.of(
        "Mr", PdfMapperConstants.Q1_TITLE_MR,
        "Mrs", PdfMapperConstants.Q1_TITLE_MRS,
        "Miss", PdfMapperConstants.Q1_TITLE_MISS,
        "Ms", PdfMapperConstants.Q1_TITLE_MS
    );
    private static final Map<String, String> TITLE_MAP = Map.of(
        "Mr", "Mister",
        "Mrs", "Mrs",
        "Miss", "Mis",
        "Ms", "Ms"
    );
    private static final String[] ADDRESS_PREFIX = {
        "2.2",
        "2.5 R2",
        "2.7 R3",
        "13 R4",
        "13 R5"
    };
    private static final String REP_ADDRESS_PREFIX = "11.3 Representative's address:";
    private static final String CLAIMANT_ADDRESS_PREFIX = "1.5";
    private static final int MULTIPLE_RESPONDENTS = 2;
    private static final int MAX_RESPONDENTS = 5;
    private static final String[] ACAS_PREFIX = {
        "2.3",
        "2.6",
        "2.8",
        "13 R4",
        "13 R5"
    };

    /**
     * Maps the parameters within case data to the inputs of the PDF Template.
     *
     * @param caseData          the case data that is to be mapped to the inputs in the PDF Template.
     * @return                  a Map containing the values from case data with the
     *                          corresponding PDF input labels as a key.
     */
    public Map<String, Optional<String>> mapHeadersToPdf(CaseData caseData) {
        ConcurrentHashMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        printFields.put(PdfMapperConstants.TRIBUNAL_OFFICE, Optional.ofNullable(caseData.getManagingOffice()));
        printFields.put(PdfMapperConstants.CASE_NUMBER, Optional.ofNullable(caseData.getCcdID()));
        printFields.put(PdfMapperConstants.DATE_RECEIVED, Optional.ofNullable(caseData.getReceiptDate()));
        printFields.putAll(printPersonalDetails(caseData));
        printFields.putAll(printRespondantDetails(caseData));
        printFields.putAll(printEmploymentDetails(caseData));
        printFields.putAll(printRepresentative(caseData.getRepresentativeClaimantType()));
        return printFields;
    }

    private ConcurrentHashMap<String, Optional<String>> printPersonalDetails(CaseData caseData) {
        ConcurrentHashMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        if (!TITLES.get(caseData.getClaimantIndType().getClaimantTitle()).isEmpty()) {
            printFields.put(
                TITLES.get(caseData.getClaimantIndType().getClaimantTitle()),
                Optional.ofNullable(TITLE_MAP.get(caseData.getClaimantIndType().getClaimantTitle()))
            );
        }
        printFields.put(PdfMapperConstants.Q1_FIRST_NAME,
            Optional.ofNullable(caseData.getClaimantIndType().getClaimantFirstNames()));
        printFields.put(PdfMapperConstants.Q1_SURNAME,
            Optional.ofNullable(caseData.getClaimantIndType().getClaimantLastName()));
        LocalDate dob = LocalDate.parse(caseData.getClaimantIndType().getClaimantDateOfBirth());
        printFields.put(PdfMapperConstants.Q1_DOB_DAY,
            Optional.ofNullable(StringUtils.leftPad(String.valueOf(dob.getDayOfMonth()),
                2, "0")));
        printFields.put(PdfMapperConstants.Q1_DOB_MONTH,
            Optional.ofNullable(StringUtils.leftPad(String.valueOf(dob.getMonthValue()),
                2, "0")));
        printFields.put(PdfMapperConstants.Q1_DOB_YEAR, Optional.of(String.valueOf(dob.getYear())));
        printFields.put(PdfMapperConstants.Q1_SEX, Optional.ofNullable(caseData.getClaimantIndType().getClaimantSex()));
        printFields.put(String.format(PdfMapperConstants.QX_HOUSE_NUMBER, CLAIMANT_ADDRESS_PREFIX),
            Optional.ofNullable(caseData.getClaimantType().getClaimantAddressUK().getAddressLine1()));
        printFields.put(String.format(PdfMapperConstants.QX_STREET, CLAIMANT_ADDRESS_PREFIX),
            Optional.ofNullable(caseData.getClaimantType().getClaimantAddressUK().getAddressLine2()));
        printFields.put(String.format(PdfMapperConstants.QX_POST_TOWN, CLAIMANT_ADDRESS_PREFIX),
            Optional.ofNullable(caseData.getClaimantType().getClaimantAddressUK().getPostTown()));
        printFields.put(String.format(PdfMapperConstants.QX_COUNTY, CLAIMANT_ADDRESS_PREFIX),
            Optional.ofNullable(caseData.getClaimantType().getClaimantAddressUK().getCounty()));
        printFields.put(String.format(PdfMapperConstants.QX_POSTCODE, CLAIMANT_ADDRESS_PREFIX),
            Optional.ofNullable(caseData.getClaimantType().getClaimantAddressUK().getPostCode()));
        printFields.put(String.format(PdfMapperConstants.QX_PHONE_NUMBER, "1.6"),
            Optional.ofNullable(caseData.getClaimantType().getClaimantPhoneNumber()));
        printFields.put(PdfMapperConstants.Q1_MOBILE_NUMBER,
            Optional.ofNullable(caseData.getClaimantType().getClaimantMobileNumber()));
        printFields.put(PdfMapperConstants.Q1_EMAIL,
            Optional.ofNullable(caseData.getClaimantType().getClaimantEmailAddress()));
        String contactPreference = caseData.getClaimantType().getClaimantContactPreference();
        if ("Email".equals(contactPreference)) {
            printFields.put(PdfMapperConstants.Q1_CONTACT_EMAIL, Optional.of(contactPreference));
        } else if ("Post".equals(contactPreference)) {
            printFields.put(PdfMapperConstants.Q1_CONTACT_POST, Optional.of(contactPreference));
        }
        return printFields;
    }

    private Map<String, Optional<String>> printRespondantDetails(CaseData caseData) {
        Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        List<RespondentSumTypeItem> respondentSumTypeList = caseData.getRespondentCollection();
        if (respondentSumTypeList.size() >= MULTIPLE_RESPONDENTS) {
            printFields.put(PdfMapperConstants.Q2_OTHER_RESPONDENTS, Optional.of(YES));
        }
        for (int i = 0; i < respondentSumTypeList.size(); i++) {
            if (i >= MAX_RESPONDENTS) {
                break;
            }
            RespondentSumType respondent = respondentSumTypeList.get(i).getValue();
            if (i == 0) {
                printFields.put(PdfMapperConstants.Q2_EMPLOYER_NAME,
                    Optional.ofNullable(respondent.getRespondentName()));
            } else {
                printFields.put(String.format(PdfMapperConstants.QX_NAME, ADDRESS_PREFIX[i]),
                    Optional.ofNullable(respondent.getRespondentName()));
            }
            printFields.putAll(printRespondant(respondent, ADDRESS_PREFIX[i]));
            printFields.putAll(printRespondantAcas(respondent, ACAS_PREFIX[i]));
        }
        if (caseData.getClaimantWorkAddress() != null) {
            Address claimantworkAddress = caseData.getClaimantWorkAddress().getClaimantWorkAddress();
            printFields.put(PdfMapperConstants.Q2_DIFFADDRESS_NUMBER,
                Optional.ofNullable(claimantworkAddress.getAddressLine1()));
            printFields.put(PdfMapperConstants.Q2_DIFFADDRESS_STREET,
                Optional.ofNullable(claimantworkAddress.getAddressLine2()));
            printFields.put(PdfMapperConstants.Q2_DIFFADDRESS_TOWN,
                Optional.ofNullable(claimantworkAddress.getPostTown()));
            printFields.put(PdfMapperConstants.Q2_DIFFADDRESS_COUNTY,
                Optional.ofNullable(claimantworkAddress.getCounty()));
            printFields.put(PdfMapperConstants.Q2_DIFFADDRESS_POSTCODE,
                Optional.ofNullable(claimantworkAddress.getPostCode()));
        }
        return printFields;
    }

    private Map<String, Optional<String>> printRespondant(RespondentSumType respondent, String questionPrefix) {
        Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        printFields.put(String.format(PdfMapperConstants.QX_HOUSE_NUMBER, questionPrefix),
            Optional.ofNullable(respondent.getRespondentAddress().getAddressLine1()));
        printFields.put(String.format(PdfMapperConstants.QX_STREET, questionPrefix),
            Optional.ofNullable(respondent.getRespondentAddress().getAddressLine2()));
        printFields.put(String.format(PdfMapperConstants.QX_POST_TOWN, questionPrefix),
            Optional.ofNullable(respondent.getRespondentAddress().getPostTown()));
        printFields.put(String.format(PdfMapperConstants.QX_COUNTY, questionPrefix),
            Optional.ofNullable(respondent.getRespondentAddress().getCounty()));
        printFields.put(String.format(PdfMapperConstants.QX_POSTCODE, questionPrefix),
            Optional.ofNullable(respondent.getRespondentAddress().getPostCode()));
        return printFields;
    }

    private Map<String, Optional<String>> printRespondantAcas(RespondentSumType respondent,
                                                    String questionPrefix) {
        Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        String acasYesNo = respondent.getRespondentACASQuestion().isEmpty() ? NO :
            respondent.getRespondentACASQuestion();
        if (YES.equals(acasYesNo)) {
            printFields.put(String.format(PdfMapperConstants.QX_HAVE_ACAS_YES, questionPrefix),
                Optional.of(acasYesNo));
            printFields.put(String.format(PdfMapperConstants.QX_ACAS_NUMBER, questionPrefix),
                Optional.ofNullable(respondent.getRespondentACAS()));
        } else {
            printFields.put(String.format(PdfMapperConstants.QX_HAVE_ACAS_NO, questionPrefix), Optional.of(YES));
            switch (respondent.getRespondentACASNo()) {
                case "Unfair Dismissal":
                    printFields.put(String.format(PdfMapperConstants.QX_ACAS_A1, questionPrefix), Optional.of(YES));
                    break;
                case "Another person":
                    printFields.put(String.format(PdfMapperConstants.QX_ACAS_A2, questionPrefix), Optional.of(YES));
                    break;
                case "No Power":
                    printFields.put(String.format(PdfMapperConstants.QX_ACAS_A3, questionPrefix), Optional.of(YES));
                    break;
                case "Employer already in touch":
                    printFields.put(String.format(PdfMapperConstants.QX_ACAS_A4, questionPrefix), Optional.of(YES));
                    break;
                default:
                    break;
            }
        }
        return printFields;
    }

    private Map<String, Optional<String>> printEmploymentDetails(CaseData caseData) {
        Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        String employerYesNo = claimantOtherType.getClaimantEmployedFrom() == null
            ? NO : YES;
        if (YES.equals(employerYesNo)) {
            printFields.put(PdfMapperConstants.Q4_EMPLOYED_BY_YES, Optional.of(employerYesNo));
            printFields.put(PdfMapperConstants.Q5_EMPLOYMENT_START,
                Optional.ofNullable(claimantOtherType.getClaimantEmployedFrom()));
            String currentlyEmployedYesNo = claimantOtherType.getClaimantEmployedCurrently().isEmpty() ? NO :
                claimantOtherType.getClaimantEmployedCurrently();
            if (YES.equals(currentlyEmployedYesNo)) {
                printFields.put(PdfMapperConstants.Q5_CONTINUING_YES, Optional.of(currentlyEmployedYesNo));
                printFields.put(PdfMapperConstants.Q5_NOT_ENDED,
                    Optional.ofNullable(claimantOtherType.getClaimantEmployedTo()));
            } else {
                printFields.put(PdfMapperConstants.Q5_CONTINUING_NO, Optional.of(YES));
                printFields.put(PdfMapperConstants.Q5_EMPLOYMENT_END,
                    Optional.ofNullable(claimantOtherType.getClaimantEmployedTo()));
            }
            printFields.put(PdfMapperConstants.Q5_DESCRIPTION,
                Optional.ofNullable(claimantOtherType.getClaimantOccupation()));
            printFields.putAll(printRenumeration(claimantOtherType));
            NewEmploymentType newEmploymentType = caseData.getNewEmploymentType();
            if (newEmploymentType == null) {
                printFields.put(PdfMapperConstants.Q7_OTHER_JOB_NO, Optional.of(YES));
            } else {
                printFields.put(PdfMapperConstants.Q7_OTHER_JOB_YES, Optional.of(YES));
                printFields.put(PdfMapperConstants.Q7_START_WORK,
                    Optional.ofNullable(newEmploymentType.getNewlyEmployedFrom()));
                printFields.put(PdfMapperConstants.Q7_EARNING,
                    Optional.ofNullable(newEmploymentType.getNewPayBeforeTax()));
            }
        }
        printFields.put(PdfMapperConstants.Q4_EMPLOYED_BY_NO, Optional.of(employerYesNo));
        return printFields;
    }

    private Map<String, Optional<String>> printRenumeration(ClaimantOtherType claimantOtherType) {
        Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        printFields.put(PdfMapperConstants.Q6_HOURS,
            Optional.ofNullable(claimantOtherType.getClaimantAverageWeeklyHours()));
        printFields.put(PdfMapperConstants.Q6_GROSS_PAY,
            Optional.ofNullable(claimantOtherType.getClaimantPayBeforeTax()));
        printFields.put(PdfMapperConstants.Q6_NET_PAY, Optional.ofNullable(claimantOtherType.getClaimantPayAfterTax()));
        switch (claimantOtherType.getClaimantPayCycle()) {
            case "Weekly":
                printFields.put(PdfMapperConstants.Q6_GROSS_PAY_WEEKLY, Optional.of(YES));
                printFields.put(PdfMapperConstants.Q6_NET_PAY_WEEKLY, Optional.of(YES));
                break;
            case "Monthly":
                printFields.put(PdfMapperConstants.Q6_GROSS_PAY_MONTHLY, Optional.of(YES));
                printFields.put(PdfMapperConstants.Q6_NET_PAY_MONTHLY, Optional.of(YES));
                break;
            case "Annually":
            default:
                printFields.put(PdfMapperConstants.Q6_GROSS_PAY_ANNUAL, Optional.of(YES));
                printFields.put(PdfMapperConstants.Q6_NET_PAY_ANNUAL, Optional.of(YES));
                break;
        }
        printFields.put(PdfMapperConstants.Q6_NET_PAY, Optional.ofNullable(claimantOtherType.getClaimantPayAfterTax()));
        String noticePeriodYesNo = claimantOtherType.getClaimantNoticePeriod().isEmpty() ? NO :
            claimantOtherType.getClaimantNoticePeriod();
        if (YES.equals(noticePeriodYesNo)) {
            printFields.put(PdfMapperConstants.Q6_PAID_NOTICE_YES,
                Optional.ofNullable(claimantOtherType.getClaimantEmployedNoticePeriod()));
            String noticeUnit = claimantOtherType.getClaimantNoticePeriodUnit();
            if ("Weeks".equals(noticeUnit)) {
                printFields.put(PdfMapperConstants.Q6_NOTICE_WEEKS,
                    Optional.ofNullable(claimantOtherType.getClaimantNoticePeriodDuration()));
            } else {
                printFields.put(PdfMapperConstants.Q6_NOTICE_MONTHS,
                    Optional.ofNullable(claimantOtherType.getClaimantNoticePeriodDuration()));
            }
        } else {
            printFields.put(PdfMapperConstants.Q6_PAID_NOTICE_NO, Optional.of(YES));
        }
        String pensionContributionYesNo = claimantOtherType.getClaimantPensionContribution().isEmpty() ? NO :
            claimantOtherType.getClaimantPensionContribution();
        if (YES.equals(pensionContributionYesNo)) {
            printFields.put(PdfMapperConstants.Q6_PENSION_YES,
                Optional.ofNullable(claimantOtherType.getClaimantPensionContribution()));
            printFields.put(PdfMapperConstants.Q6_PENSION_WEEKLY,
                Optional.ofNullable(claimantOtherType.getClaimantPensionWeeklyContribution()));

        } else {
            printFields.put(PdfMapperConstants.Q6_PENSION_NO, Optional.of(YES));
        }
        printFields.put(PdfMapperConstants.Q6_OTHER_BENEFITS,
            Optional.ofNullable(claimantOtherType.getClaimantBenefitsDetail()));
        return printFields;
    }

    private Map<String, Optional<String>> printRepresentative(RepresentedTypeC representativeClaimantType) {
        if (representativeClaimantType != null) {
            Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();
            printFields.put(PdfMapperConstants.Q11_REP_NAME,
                Optional.ofNullable(representativeClaimantType.getNameOfRepresentative()));
            printFields.put(PdfMapperConstants.Q11_REP_ORG,
                Optional.ofNullable(representativeClaimantType.getNameOfOrganisation()));
            printFields.put(PdfMapperConstants.Q11_REP_NUMBER, Optional.of(""));
            Address repAddress = representativeClaimantType.getRepresentativeAddress();
            printFields.put(String.format(PdfMapperConstants.QX_HOUSE_NUMBER, REP_ADDRESS_PREFIX),
                Optional.ofNullable(repAddress.getAddressLine1()));
            printFields.put(String.format(PdfMapperConstants.QX_STREET, REP_ADDRESS_PREFIX),
                Optional.ofNullable(repAddress.getAddressLine2()));
            printFields.put(String.format(PdfMapperConstants.QX_POST_TOWN, REP_ADDRESS_PREFIX),
                Optional.ofNullable(repAddress.getPostTown()));
            printFields.put(String.format(PdfMapperConstants.QX_COUNTY, REP_ADDRESS_PREFIX),
                Optional.ofNullable(repAddress.getCounty()));
            printFields.put(String.format(PdfMapperConstants.QX_POSTCODE, REP_ADDRESS_PREFIX),
                Optional.ofNullable(repAddress.getPostCode()));
            printFields.put(String.format(PdfMapperConstants.QX_PHONE_NUMBER, REP_ADDRESS_PREFIX),
                Optional.ofNullable(representativeClaimantType.getRepresentativePhoneNumber()));
            printFields.put(PdfMapperConstants.Q11_MOBILE_NUMBER,
                Optional.ofNullable(representativeClaimantType.getRepresentativeMobileNumber()));
            printFields.put(PdfMapperConstants.Q11_EMAIL,
                Optional.ofNullable(representativeClaimantType.getRepresentativeEmailAddress()));
            printFields.put(PdfMapperConstants.Q11_REFERENCE,
                Optional.ofNullable(representativeClaimantType.getRepresentativeReference()));
            String representativePreference = representativeClaimantType.getRepresentativePreference();
            if ("Email".equals(representativePreference)) {
                printFields.put(PdfMapperConstants.Q11_CONTACT_EMAIL, Optional.of(YES));
            } else {
                printFields.put(PdfMapperConstants.Q11_CONTACT_POST, Optional.of(YES));
            }
            return printFields;
        }
        return new HashMap<>();
    }
}
