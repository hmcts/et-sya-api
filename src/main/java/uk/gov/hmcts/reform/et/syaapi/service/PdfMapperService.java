package uk.gov.hmcts.reform.et.syaapi.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

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
        "Mr", "1.1 Title Mr",
        "Mrs", "1.1 Title Mrs",
        "Miss", "1.1 Title Miss",
        "Ms", "1.1 Title Ms"
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
        printFields.put("tribunal office", caseData.getManagingOffice());
        printFields.put("case number", caseData.getCcdID());
        printFields.put("date received", caseData.getReceiptDate());
        printFields.putAll(printPersonalDetails(caseData));
        printFields.putAll(printRespondantDetails(caseData));

        // TODO: write other claims

        // TODO: make conditionals none case-sensitive

        printFields.putAll(printEmploymentDetails(caseData));

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
        printFields.put("1.2 first names", caseData.getClaimantIndType().getClaimantFirstNames());
        printFields.put("1.3 surname", caseData.getClaimantIndType().getClaimantLastName());
        LocalDate dob = LocalDate.parse(caseData.getClaimantIndType().getClaimantDateOfBirth());
        printFields.put("1.4 DOB day", StringUtils.leftPad(String.valueOf(dob.getDayOfMonth()), 2, "0"));
        printFields.put("1.4 DOB month", StringUtils.leftPad(String.valueOf(dob.getMonthValue()), 2, "0"));
        printFields.put("1.4 DOB year", String.valueOf(dob.getYear()));
        printFields.put("1.5 sex", caseData.getClaimantIndType().getClaimantSex());
        printFields.put("1.5 number", caseData.getClaimantType().getClaimantAddressUK().getAddressLine1());
        printFields.put("1.5 street", caseData.getClaimantType().getClaimantAddressUK().getAddressLine2());
        printFields.put("1.5 town city", caseData.getClaimantType().getClaimantAddressUK().getPostTown());
        printFields.put("1.5 county", caseData.getClaimantType().getClaimantAddressUK().getCounty());
        printFields.put("1.5 postcode", caseData.getClaimantType().getClaimantAddressUK().getPostCode());
        printFields.put("1.6 phone number", caseData.getClaimantType().getClaimantPhoneNumber());
        printFields.put("1.7 mobile number", caseData.getClaimantType().getClaimantMobileNumber());
        printFields.put("1.11 email", caseData.getClaimantType().getClaimantEmailAddress());
        String contactPreference = caseData.getClaimantType().getClaimantContactPreference();
        if ("Email".equals(contactPreference)) {
            printFields.put("1.8 How should we contact you - Email", contactPreference);
        } else if ("Post".equals(contactPreference)) {
            printFields.put("1.8 How should we contact you - Post", contactPreference);
        }
        return printFields;
    }

    private Map<String, String> printRespondantDetails(CaseData caseData) {
        Map<String, String> printFields = new HashMap<>();

        List<RespondentSumTypeItem> respondentSumTypeList = caseData.getRespondentCollection();

        RespondentSumType respondent = respondentSumTypeList.get(0).getValue();

        printFields.put(
            "2.1 Give the name of your employer or the person or organisation you are claiming against",
            respondent.getRespondentName()
        );
        printFields.putAll(printRespondantAddress(respondent, "2.2"));
        printFields.putAll(printRespondantAcas(respondent, "2.3"));

        // TODO: if worked at different address from 2.2 - add if statement

        if(caseData.getClaimantWorkAddress() != null) {
            Address claimantworkAddress = caseData.getClaimantWorkAddress().getClaimantWorkAddress();
            printFields.put("2.4 number", claimantworkAddress.getAddressLine1());
            printFields.put("2.4 street", claimantworkAddress.getAddressLine2());
            printFields.put("2.4 town", claimantworkAddress.getPostTown());
            printFields.put("2.4 county", claimantworkAddress.getCounty());
            printFields.put("2.4 postcode", claimantworkAddress.getPostCode());
        }

        if(respondentSumTypeList.size() > 1) {
            // TODO: need to add a tick?
            printFields.put("2.5 other respondents", "");

            RespondentSumType secondRespondent = respondentSumTypeList.get(1).getValue();
            printFields.put("2.5 name", secondRespondent.getRespondentName());
            printFields.putAll(printRespondantAddress(secondRespondent, "2.5"));
            printFields.putAll(printRespondantAcas(secondRespondent, "2.6"));

            if(respondentSumTypeList.size() > 2) {
                RespondentSumType thirdRespondent = respondentSumTypeList.get(2).getValue();
                printFields.put("2.7 name", thirdRespondent.getRespondentName());
                printFields.putAll(printRespondantAddress(thirdRespondent, "2.7"));
                printFields.putAll(printRespondantAcas(thirdRespondent, "2.8"));
            }
        }

        return printFields;
    }

    private Map<String, String> printRespondantAddress(RespondentSumType respondent, String questionPrefix) {
        Map<String, String> printFields = new HashMap<>();

        printFields.put(questionPrefix + " number", respondent.getRespondentAddress().getAddressLine1());
        printFields.put(questionPrefix + " street", respondent.getRespondentAddress().getAddressLine2());
        printFields.put(questionPrefix + " town city", respondent.getRespondentAddress().getPostTown());
        printFields.put(questionPrefix + " county", respondent.getRespondentAddress().getCounty());
        printFields.put(questionPrefix + " postcode", respondent.getRespondentAddress().getPostCode());

        return printFields;
    }

    private Map<String, String> printRespondantAcas(RespondentSumType respondent,
                                                    String questionPrefix) {
        Map<String, String> printFields = new HashMap<>();

        String acasYesNo = !respondent.getRespondentACASQuestion().isEmpty()
            ? respondent.getRespondentACASQuestion() : "No";

        if (acasYesNo.equals("Yes")) {
            printFields.put(questionPrefix + " Do you have an Acas early conciliation certificate number? Yes",
                acasYesNo);
            printFields.put(questionPrefix + " please give the Acas early conciliation certificate number",
                respondent.getRespondentACAS());
        }

        // TODO: 2.3 why dont you have this number

        return printFields;
    }

    private Map<String, String> printEmploymentDetails(CaseData caseData) {
        Map<String, String> printFields = new HashMap<>();
        ClaimantOtherType claimantOtherType = caseData.getClaimantOtherType();

        String employerYesNo = claimantOtherType.getClaimantEmployedFrom() == null
            ? "No" : "Yes";


        if(employerYesNo.equals("Yes")) {
            printFields.put("4.1 did you work for the respondent you're making your claim against? Yes",
                employerYesNo);

            printFields.put("5.1 when did your employment start?", claimantOtherType.getClaimantEmployedFrom());

            String currentlyEmployedYesNo = !claimantOtherType.getClaimantEmployedCurrently().isEmpty()
                ? claimantOtherType.getClaimantEmployedCurrently() : "No";

            if("Yes".equals(currentlyEmployedYesNo)) {
                printFields.put("5.1 is your employment continuing? Yes", currentlyEmployedYesNo);

                // TODO: Is this the correct place?
                printFields.put("5.1 if your employment has not ended", claimantOtherType.getClaimantEmployedTo());
            } else {
                printFields.put("5.1 is your employment continuing? No", currentlyEmployedYesNo);

                // TODO: Is this the correct place?
                printFields.put("5.1 if your employment has ended, when did it end?",
                    claimantOtherType.getClaimantEmployedTo());
            }

            printFields.put("5.2 Please say what job you do or did.", claimantOtherType.getClaimantOccupation());

            printFields.putAll(printRenumeration(claimantOtherType));
        }

        printFields.put("4.1 did you work for the respondent you're making your claim agaisnt No",
            employerYesNo);

        return printFields;
    }

    private Map<String, String> printRenumeration(ClaimantOtherType claimantOtherType) {
        Map<String, String> printFields = new HashMap<>();

        printFields.put("6.1 How many hours on average do, or did you work",
            claimantOtherType.getClaimantAverageWeeklyHours());

        printFields.put("6.2 Pay before tax", claimantOtherType.getClaimantPayBeforeTax());

        // TODO: implement cycle

        printFields.put("6.2 Normal take-home pay", claimantOtherType.getClaimantPayAfterTax());

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
}
