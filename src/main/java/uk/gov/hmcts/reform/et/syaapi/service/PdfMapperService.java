package uk.gov.hmcts.reform.et.syaapi.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.NewEmploymentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.constants.PdfMapperConstants;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps Case Data attributes to fields within the PDF template.
 *
 *
 */
@Service
public class PdfMapperService {
    public final static Map<String, String> TITLES = Map.of(
        "Mr", PdfMapperConstants.Q1_TITLE_MR,
        "Mrs", PdfMapperConstants.Q1_TITLE_MRS,
        "Miss", PdfMapperConstants.Q1_TITLE_MISS,
        "Ms", PdfMapperConstants.Q1_TITLE_MS
    );

    public static final Map<String, String> TITLE_MAP = Map.of(
        "Mr", "Mister",
        "Mrs", "Mrs",
        "Miss", "Mis",
        "Ms", "Ms"
    );

    public Map<String, String> mapHeadersToPdf(CaseData caseData) throws PdfMapperException {
        verifyCase(caseData);
        Map<String, String> printFields = new HashMap<>();
        printFields.put(PdfMapperConstants.TRIBUNAL_OFFICE, caseData.getManagingOffice());
        printFields.put(PdfMapperConstants.CASE_NUMBER, caseData.getCcdID());
        printFields.put(PdfMapperConstants.DATE_RECEIVED, caseData.getReceiptDate());
        printFields.putAll(printPersonalDetails(caseData));
        printFields.putAll(printRespondantDetails(caseData));
        // TODO: write other claims
        // TODO: make conditionals none case-sensitive
        printFields.putAll(printEmploymentDetails(caseData));
        printFields.putAll(printClaimDetails(caseData));
        return printFields;
    }

    private void verifyCase(CaseData caseData) throws PdfMapperException {
        if(caseData == null) {
            throw new PdfMapperException("pdfMapperService: Case provided was null");
        }
    }

    private Map<String, String> printPersonalDetails(CaseData caseData) {
        Map<String, String> printFields = new HashMap<>();
        if (!TITLES.get(caseData.getClaimantIndType().getClaimantTitle()).isEmpty()) {
            printFields.put(
                TITLES.get(caseData.getClaimantIndType().getClaimantTitle()),
                TITLE_MAP.get(caseData.getClaimantIndType().getClaimantTitle())
            );
        }
        printFields.put(PdfMapperConstants.Q1_FIRST_NAME, caseData.getClaimantIndType().getClaimantFirstNames());
        printFields.put(PdfMapperConstants.Q1_SURNAME, caseData.getClaimantIndType().getClaimantLastName());
        LocalDate dob = LocalDate.parse(caseData.getClaimantIndType().getClaimantDateOfBirth());
        printFields.put(PdfMapperConstants.Q1_DOB_DAY, StringUtils.leftPad(String.valueOf(dob.getDayOfMonth()), 2, "0"));
        printFields.put(PdfMapperConstants.Q1_DOB_MONTH, StringUtils.leftPad(String.valueOf(dob.getMonthValue()), 2, "0"));
        printFields.put(PdfMapperConstants.Q1_DOB_YEAR, String.valueOf(dob.getYear()));
        printFields.put(PdfMapperConstants.Q1_SEX, caseData.getClaimantIndType().getClaimantSex());
        printFields.put(String.format(PdfMapperConstants.QX_HOUSE_NUMBER, "1.5"),
            caseData.getClaimantType().getClaimantAddressUK().getAddressLine1());
        printFields.put(String.format(PdfMapperConstants.QX_STREET, "1.5"),
            caseData.getClaimantType().getClaimantAddressUK().getAddressLine2());
        printFields.put(String.format(PdfMapperConstants.QX_POST_TOWN, "1.5"),
            caseData.getClaimantType().getClaimantAddressUK().getPostTown());
        printFields.put(String.format(PdfMapperConstants.QX_COUNTY, "1.5"),
            caseData.getClaimantType().getClaimantAddressUK().getCounty());
        printFields.put(String.format(PdfMapperConstants.QX_POSTCODE, "1.5"),
            caseData.getClaimantType().getClaimantAddressUK().getPostCode());
        printFields.put(String.format(PdfMapperConstants.QX_PHONE_NUMBER, "1.6"),
            caseData.getClaimantType().getClaimantPhoneNumber());
        printFields.put(PdfMapperConstants.Q1_MOBILE_NUMBER, caseData.getClaimantType().getClaimantMobileNumber());
        printFields.put(PdfMapperConstants.Q1_EMAIL, caseData.getClaimantType().getClaimantEmailAddress());
        String contactPreference = caseData.getClaimantType().getClaimantContactPreference();
        if ("Email".equals(contactPreference)) {
            printFields.put(PdfMapperConstants.Q1_CONTACT_EMAIL, contactPreference);
        } else if ("Post".equals(contactPreference)) {
            printFields.put(PdfMapperConstants.Q1_CONTACT_POST, contactPreference);
        }
        return printFields;
    }

    private Map<String, String> printRespondantDetails(CaseData caseData) {
        Map<String, String> printFields = new HashMap<>();

        List<RespondentSumTypeItem> respondentSumTypeList = caseData.getRespondentCollection();

        RespondentSumType respondent = respondentSumTypeList.get(0).getValue();

        printFields.put(
            PdfMapperConstants.Q2_EMPLOYER_NAME, respondent.getRespondentName()
        );
        printFields.putAll(printRespondant(respondent, "2.2"));
        printFields.putAll(printRespondantAcas(respondent, "2.3"));

        if(caseData.getClaimantWorkAddress() != null) {
            Address claimantworkAddress = caseData.getClaimantWorkAddress().getClaimantWorkAddress();
            printFields.put(PdfMapperConstants.Q2_DIFFADDRESS_NUMBER, claimantworkAddress.getAddressLine1());
            printFields.put(PdfMapperConstants.Q2_DIFFADDRESS_STREET, claimantworkAddress.getAddressLine2());
            printFields.put(PdfMapperConstants.Q2_DIFFADDRESS_TOWN, claimantworkAddress.getPostTown());
            printFields.put(PdfMapperConstants.Q2_DIFFADDRESS_COUNTY, claimantworkAddress.getCounty());
            printFields.put(PdfMapperConstants.Q2_DIFFADDRESS_POSTCODE, claimantworkAddress.getPostCode());
        }

        if(respondentSumTypeList.size() > 1) {
            // TODO: need to add a tick?
            printFields.put(PdfMapperConstants.Q2_OTHER_RESPONDENTS, "YES");

            RespondentSumType secondRespondent = respondentSumTypeList.get(1).getValue();
            printFields.putAll(printRespondant(secondRespondent, "2.5"));
            printFields.putAll(printRespondantAcas(secondRespondent, "2.6"));

            if(respondentSumTypeList.size() > 2) {
                RespondentSumType thirdRespondent = respondentSumTypeList.get(2).getValue();
                printFields.putAll(printRespondant(thirdRespondent, "2.7"));
                printFields.putAll(printRespondantAcas(thirdRespondent, "2.8"));
            }
        }

        return printFields;
    }

    private Map<String, String> printRespondant(RespondentSumType respondent, String questionPrefix) {
        Map<String, String> printFields = new HashMap<>();
        printFields.put(String.format(PdfMapperConstants.QX_NAME, questionPrefix),
            respondent.getRespondentName());
        printFields.put(String.format(PdfMapperConstants.QX_HOUSE_NUMBER, questionPrefix),
            respondent.getRespondentAddress().getAddressLine1());
        printFields.put(String.format(PdfMapperConstants.QX_STREET, questionPrefix),
            respondent.getRespondentAddress().getAddressLine2());
        printFields.put(String.format(PdfMapperConstants.QX_POST_TOWN, questionPrefix),
            respondent.getRespondentAddress().getPostTown());
        printFields.put(String.format(PdfMapperConstants.QX_COUNTY, questionPrefix),
            respondent.getRespondentAddress().getCounty());
        printFields.put(String.format(PdfMapperConstants.QX_POSTCODE, questionPrefix),
            respondent.getRespondentAddress().getPostCode());
        return printFields;
    }

    private Map<String, String> printRespondantAcas(RespondentSumType respondent,
                                                    String questionPrefix) {
        Map<String, String> printFields = new HashMap<>();

        String acasYesNo = !respondent.getRespondentACASQuestion().isEmpty()
            ? respondent.getRespondentACASQuestion() : "No";

        if (acasYesNo.equals("Yes")) {
            printFields.put(String.format(PdfMapperConstants.QX_HAVE_ACAS_YES, questionPrefix),
                acasYesNo);
            printFields.put(String.format(PdfMapperConstants.QX_ACAS_NUMBER, questionPrefix),
                respondent.getRespondentACAS());
        } else {
            printFields.put(String.format(PdfMapperConstants.QX_HAVE_ACAS_NO, questionPrefix),
                "Yes");

            // TODO: 2.3 why dont you have this number
        }

        return printFields;
    }

    private Map<String, String> printEmploymentDetails(CaseData caseData) {
        Map<String, String> printFields = new HashMap<>();
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();
        String employerYesNo = claimantOtherType.getClaimantEmployedFrom() == null
            ? "No" : "Yes";
        if(employerYesNo.equals("Yes")) {
            printFields.put(PdfMapperConstants.Q4_EMPLOYED_BY_YES, employerYesNo);
            printFields.put(PdfMapperConstants.Q5_EMPLOYMENT_START, claimantOtherType.getClaimantEmployedFrom());
            String currentlyEmployedYesNo = !claimantOtherType.getClaimantEmployedCurrently().isEmpty()
                ? claimantOtherType.getClaimantEmployedCurrently() : "No";
            if("Yes".equals(currentlyEmployedYesNo)) {
                printFields.put(PdfMapperConstants.Q5_CONTINUING_YES, currentlyEmployedYesNo);
                printFields.put(PdfMapperConstants.Q5_NOT_ENDED, claimantOtherType.getClaimantEmployedTo());
            } else {
                printFields.put(PdfMapperConstants.Q5_CONTINUING_NO, "Yes");
                printFields.put(PdfMapperConstants.Q5_EMPLOYMENT_END,
                    claimantOtherType.getClaimantEmployedTo());
            }
            printFields.put(PdfMapperConstants.Q5_DESCRIPTION, claimantOtherType.getClaimantOccupation());
            printFields.putAll(printRenumeration(claimantOtherType));
            NewEmploymentType newEmploymentType = caseData.getNewEmploymentType();
            if(newEmploymentType != null) {
                printFields.put(PdfMapperConstants.Q7_OTHER_JOB_YES, "Yes");
                printFields.put(PdfMapperConstants.Q7_START_WORK, newEmploymentType.getNewlyEmployedFrom());
                printFields.put(PdfMapperConstants.Q7_EARNING, newEmploymentType.getNewPayBeforeTax());
                // TODO: need to consider weekly, monthly, annual
            } else {
                printFields.put(PdfMapperConstants.Q7_OTHER_JOB_NO, "Yes");
            }
        }
        printFields.put(PdfMapperConstants.Q4_EMPLOYED_BY_NO, employerYesNo);
        return printFields;
    }

    private Map<String, String> printRenumeration(ClaimantOtherType claimantOtherType) {
        Map<String, String> printFields = new HashMap<>();

        printFields.put(PdfMapperConstants.Q6_HOURS, claimantOtherType.getClaimantAverageWeeklyHours());
        printFields.put(PdfMapperConstants.Q6_GROSS_PAY, claimantOtherType.getClaimantPayBeforeTax());
        printFields.put(PdfMapperConstants.Q6_NET_PAY, claimantOtherType.getClaimantPayAfterTax());
        String cycle = claimantOtherType.getClaimantPayCycle();
        switch(cycle.toUpperCase()) {
            case "WEEKLY":
                printFields.put(PdfMapperConstants.Q6_GROSS_PAY_WEEKLY, "Yes");
                printFields.put(PdfMapperConstants.Q6_NET_PAY_WEEKLY, "Yes");
                break;
            case "MONTHLY":
                printFields.put(PdfMapperConstants.Q6_GROSS_PAY_MONTHLY, "Yes");
                printFields.put(PdfMapperConstants.Q6_NET_PAY_MONTHLY, "Yes");
                break;
            case "ANNUALLY":
                printFields.put(PdfMapperConstants.Q6_GROSS_PAY_ANNUAL, "Yes");
                printFields.put(PdfMapperConstants.Q6_NET_PAY_ANNUAL, "Yes");
                break;
        }
        printFields.put(PdfMapperConstants.Q6_NET_PAY, claimantOtherType.getClaimantPayAfterTax());

        // TODO: Notice period, need confirmation

        String pensionContributionYesNo = !claimantOtherType.getClaimantPensionContribution().isEmpty()
            ? claimantOtherType.getClaimantPensionContribution() : "No";
        if("Yes".equals(pensionContributionYesNo)) {
            printFields.put("6.4 Were you in your employer's pension scheme? Yes",
                claimantOtherType.getClaimantPensionContribution());
            printFields.put("6.4 If Yes, give your employers weekly contributions",
                claimantOtherType.getClaimantPensionWeeklyContribution());

        } else {
            printFields.put("6.4 Were you in your employer's pension scheme? No",
                claimantOtherType.getClaimantPensionContribution());
        }
        printFields.put("6.5 If you received any other benefits",
            claimantOtherType.getClaimantBenefitsDetail());

        return printFields;
    }

    private Map<String, String> printClaimDetails(CaseData caseData) {
        Map<String, String> printFields = new HashMap<>();

        // TODO: CLAIM DETAILS TO BE ADDED

        return printFields;
    }
}
