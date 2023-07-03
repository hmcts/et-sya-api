package uk.gov.hmcts.reform.et.syaapi.service.util;

import java.util.Map;

/**
 * Defines the input labels within the template 'Employment tribunal claim form' (ver. ET1_0922) document as constants.
 */
public final class PdfMapperConstants {

    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String NO_LOWERCASE = "no";
    public static final String TRIBUNAL_OFFICE =  "tribunal office";
    public static final String CASE_NUMBER = "case number";
    public static final String DATE_RECEIVED = "date received";
    public static final String Q1_TITLE_MR = "1.1 Title Mr";
    public static final String Q1_TITLE_MRS = "1.1 Title Mrs";
    public static final String Q1_TITLE_MISS = "1.1 Title Miss";
    public static final String Q1_TITLE_MS = "1.1 Title Ms";
    public static final String Q1_TITLE_OTHER = "1.1 Title other";
    public static final String Q1_TITLE_OTHER_SPECIFY = "1.1 other specify";
    public static final String Q1_FIRST_NAME = "1.2 first names";
    public static final String Q1_SURNAME = "1.3 surname";
    public static final String Q1_DOB_DAY = "1.4 DOB day";
    public static final String Q1_DOB_MONTH = "1.4 DOB month";
    public static final String Q1_DOB_YEAR = "1.4 DOB year";
    public static final String Q1_SEX_MALE = "1.5 sex";
    public static final String Q1_SEX_FEMALE = "1.5 sex female";
    public static final String Q1_SEX_PREFER_NOT_TO_SAY = "1.5 sex prefer not to say";
    public static final String Q1_MOBILE_NUMBER = "1.7 mobile number";
    public static final String Q1_CONTACT_POST = "1.8 How should we contact you - Post";
    public static final String Q1_CONTACT_EMAIL = "1.8 How should we contact you - Email";
    public static final String Q1_EMAIL = "1.9 email";
    public static final String I_CAN_TAKE_PART_IN_VIDEO_HEARINGS = "1.11 video";
    public static final String I_CAN_TAKE_PART_IN_PHONE_HEARINGS = "1.11 phone";
    public static final String I_CAN_TAKE_PART_IN_NO_HEARINGS = "1.11 no";
    public static final String I_CAN_TAKE_PART_IN_NO_HEARINGS_EXPLAIN = "1.11 explain";
    public static final String Q2_4_DIFFERENT_WORK_ADDRESS =  "2.4 Full, different working address - Number or name";
    public static final String Q3_MORE_CLAIMS_YES = "3.1 Are you aware that your claim is one of a number of "
        + "claims against the same employer arising from the same, or similar, circumstances? Yes";
    public static final String Q3_MORE_CLAIMS_NO = "3.1 Are you aware that your claim is one of a number of "
        + "claims against the same employer arising from the same, or similar, circumstances? No";
    public static final String Q4_EMPLOYED_BY_YES = "4.1 yes";
    public static final String Q4_EMPLOYED_BY_NO = "4.1 no";
    public static final String Q5_EMPLOYMENT_START = "5.1 employment start";
    public static final String Q5_EMPLOYMENT_END = "5.1 employment end";
    public static final String Q5_NOT_ENDED = "5.1 not ended";
    public static final String Q5_CONTINUING_YES = "5.1 Is your employment continuing? Yes";
    public static final String Q5_CONTINUING_NO = "5.1 Is your employment continuing? No";
    public static final String Q5_DESCRIPTION = "5.2 Please say what job you do or did";
    public static final String Q6_HOURS = "6.1 How many hours on average do, or did you work each week "
        + "in the job this claim is about?";
    public static final String Q6_GROSS_PAY = "6.2 pay before tax";
    public static final String Q6_GROSS_PAY_WEEKLY = "6.2 pay before tax - weekly";
    public static final String Q6_GROSS_PAY_MONTHLY = "6.2 pay before tax - monthly";
    public static final String Q6_GROSS_PAY_ANNUAL = "6.2 pay before tax - annually";
    public static final String Q6_NET_PAY = "6.2 normal pay";
    public static final String Q6_NET_PAY_WEEKLY = "6.2 normal pay - weekly";
    public static final String Q6_NET_PAY_MONTHLY = "6.2 normal pay - monthly";
    public static final String Q6_NET_PAY_ANNUAL = "6.2 normal pay - annually";
    public static final String Q6_PAID_NOTICE_YES = "6.3 If your employment has ended, did you work (or were "
        + "you paid for) a period of notice? Yes";
    public static final String Q6_PAID_NOTICE_NO = "6.3 If your employment has ended, did you work (or were you "
        + "paid for) a period of notice? No";
    public static final String Q6_NOTICE_WEEKS = "6.3 If Yes, how many weeks’ notice did you work, or were you"
        + " paid for?";
    public static final String Q6_NOTICE_MONTHS = "6.3 If Yes, how many months’ notice did you work, or were "
        + "you paid for?";
    public static final String Q6_PENSION_YES = "6.4 Were you in your employer’s pension scheme? Yes";
    public static final String Q6_PENSION_NO = "6.4 Were you in your employer’s pension scheme? No";
    public static final String Q6_PENSION_WEEKLY = "6.4 employers weekly contributions";
    public static final String Q6_OTHER_BENEFITS = "6.5 If you received any other benefits, e.g. company car, "
        + "medical insurance, etc, from your employer, please give details";
    public static final String Q7_OTHER_JOB_YES = "7.1 Have you got another job? Yes";
    public static final String Q7_OTHER_JOB_NO = "7.1 Have you got another job? No";
    public static final String Q7_START_WORK = "7.2 Please say when you started (or will start) work";
    public static final String Q7_EARNING = "7.3 Please say how much you are now earning (or will earn)";
    public static final String Q7_EARNING_WEEKLY = "7.3 weekly";
    public static final String Q7_EARNING_MONTHLY = "7.3 monthly";
    public static final String Q7_EARNING_ANNUAL = "7.3 annually";
    public static final String Q8_TYPE_OF_CLAIM_DISCRIMINATION = "8.1 I was discriminated against on the grounds of";
    public static final String Q8_TYPE_OF_CLAIM_UNFAIRLY_DISMISSED =
        "8.1 I was unfairly dismissed (including constructive dismissal)";
    public static final String Q8_TYPE_OF_CLAIM_WHISTLE_BLOWING = "8.1 whistleblowing";
    public static final String Q8_TYPE_OF_CLAIM_REDUNDANCY_PAYMENT = "8.1 I am claiming a redundancy payment";
    public static final String Q8_TYPE_OF_CLAIM_I_AM_OWED = "8.1 owed";
    public static final String Q8_TYPE_OF_CLAIM_OTHER_TYPES_OF_CLAIMS = "8.1 another type of claim";
    public static final String Q8_ANOTHER_TYPE_OF_CLAIM_TEXT_AREA = "8.1 other type of claim";
    public static final String Q8_TYPE_OF_DISCRIMINATION_AGE = "8.1 age";
    public static final String Q8_TYPE_OF_DISCRIMINATION_GENDER_REASSIGNMENT = "8.1 gender reassignment";
    public static final String Q8_TYPE_OF_DISCRIMINATION_PREGNANCY_OR_MATERNITY = "8.1 pregnancy or maternity";
    public static final String Q8_TYPE_OF_DISCRIMINATION_SEXUAL_ORIENTATION = "8.1 sexual orientation";
    public static final String Q8_TYPE_OF_DISCRIMINATION_RELIGION_OR_BELIEF = "8.1 religion or belief";
    public static final String Q8_TYPE_OF_DISCRIMINATION_RACE = "8.1 race";
    public static final String Q8_TYPE_OF_DISCRIMINATION_DISABILITY = "8.1 disability";
    public static final String Q8_TYPE_OF_DISCRIMINATION_MARRIAGE_OR_CIVIL_PARTNERSHIP =
        "8.1 marriage or civil partnership";
    public static final String Q8_TYPE_OF_PAY_CLAIMS_NOTICE_PAY = "8.1 notice pay";
    public static final String Q8_TYPE_OF_PAY_CLAIMS_HOLIDAY_PAY = "8.1 holiday pay";
    public static final String Q8_TYPE_OF_PAY_CLAIMS_ARREARS = "8.1 arrears of pay";
    public static final String Q8_TYPE_OF_PAY_CLAIMS_OTHER_PAYMENTS = "8.1 other payments";
    public static final String Q8_TYPE_OF_DISCRIMINATION_SEX = "8.1 sex (including equal pay)";
    public static final String Q8_CLAIM_DESCRIPTION
        = "8.2 Please set out the background and details of your claim in the space below";
    public static final String Q9_CLAIM_SUCCESSFUL_REQUEST_OLD_JOB_BACK_AND_COMPENSATION =
        "9.1 If claiming unfair dismissal, to get your old job back and compensation (reinstatement)";
    public static final String Q9_CLAIM_SUCCESSFUL_REQUEST_ANOTHER_JOB =
        "9.1 If claiming unfair dismissal, to get another job with the same employer or "
            + "associated employer and compensation (re-engagement)";
    public static final String Q9_CLAIM_SUCCESSFUL_REQUEST_COMPENSATION = "9.1 Compensation only";
    public static final String Q9_CLAIM_SUCCESSFUL_REQUEST_DISCRIMINATION_RECOMMENDATION =
        "9.1 If claiming discrimination, a recommendation (see Guidance)";
    public static final String Q9_WHAT_COMPENSATION_REMEDY_ARE_YOU_SEEKING =
        "9.2 What compensation or remedy are you seeking?";
    public static final String Q10_WHISTLE_BLOWING =
        "10.1 If your claim consists of, or includes, a claim that you are making a "
            + "protected disclosure under the Employment Rights Act 1996 "
            + "(otherwise known as a ‘whistleblowing’ claim), "
            + "please tick the box if you want a copy of this form, or information from it, to be forwarded on your "
            + "behalf to a relevant regulator (known as a ‘prescribed person’ under the relevant legislation) by "
            + "tribunal staff. (See Guidance)";
    public static final String Q10_WHISTLE_BLOWING_REGULATOR = "10.1 name of relevant regulator";
    public static final String Q11_REP_NAME = "11.1 Name of representative";
    public static final String Q11_REP_ORG = "11.2 Name of organisation";
    public static final String Q11_REP_NUMBER = "11.4 Representative's DX number (if known)";
    public static final String Q11_MOBILE_NUMBER = "11.6 mobile number (if different)";
    public static final String Q11_PHONE_NUMBER = "11.5 Phone number";
    public static final String Q11_REFERENCE = "11.7 Their reference for correspondence";
    public static final String Q11_EMAIL = "11.8 Email address";
    public static final String Q11_CONTACT_POST = "11.9 How would you prefer us to communicate with them? Post";
    public static final String Q11_CONTACT_EMAIL = "11.9 How would you prefer us to communicate with them? Email";
    public static final String Q12_DISABILITY_YES = "12.1 Do you have a disability? Yes";
    public static final String Q12_DISABILITY_NO = "12.1 Do you have a disability? No";
    public static final String Q12_DISABILITY_DETAILS = "12.1 If you have a disability, give details";
    public static final String Q15_ADDITIONAL_INFORMATION = "15 Additional information";
    public static final String QX_NAME = "%s name";
    public static final String Q11_3_REPRESENTATIVE_ADDRESS = "11.3 Representative's address: number or name";
    public static final String Q11_3_REPRESENTATIVE_POSTCODE = "11.3 Representative's address: postcode";
    public static final String Q1_6_CLAIMANT_ADDRESS = "1.5 number";
    public static final String Q1_6_CLAIMANT_POSTCODE = "1.5 postcode";
    public static final String PDF_TEMPLATE_Q2_5_MULTIPLE_RESPONDENTS =
        "2.5 If there are other respondents please tick this box and put their names and addresses here";
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////// RESPONDENT FIELD NAMES /////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final String WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE =
        "why don't you have an Acas early conciliation certificate number? ";
    private static final String ACAS_NO_POWER_TO_CONCILIATE =
        "Acas doesn’t have the power to conciliate on some or all of my claim";
    private static final String EMPLOYER_ALREADY_CONTACTED_ACAS =
        "My employer has already been in touch with Acas";
    private static final String CLAIM_CONSISTS_ONLY_COMPLAINT_OF_UNFAIR_DISMISSAL =
        "My claim consists only of a complaint of unfair dismissal "
        + "which contains an application for interim relief. (See guidance)";
    private static final String ANOTHER_PERSON_HAS_AN_ACAS_EARLY_CONCILIATION_CERTIFICATE_NUMBER =
        "Another person I'm making the claim with has an Acas early conciliation certificate number";
    private static final String Q_2_3 = "2.3 ";
    private static final String Q_2_6 = "2.6 ";
    private static final String Q_2_8 = "2.8 ";
    private static final String Q_13_4 = "13 R4 ";
    private static final String Q_13_5 = "13 R5 ";
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////// FIRST RESPONDENT FIELD NAMES //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String Q2_EMPLOYER_NAME = "2.1 Give the name of your employer or the person or "
        + "organisation you are claiming against";
    public static final String PDF_TEMPLATE_Q2_2_1_FIRST_RESPONDENT_ADDRESS = "2.2 number";
    public static final String PDF_TEMPLATE_Q2_2_2_FIRST_RESPONDENT_POSTCODE = "2.2 postcode";
    public static final String PDF_TEMPLATE_Q2_3_1_1_FIRST_RESPONDENT_ACAS_CERTIFICATE_CHECK_YES =
        Q_2_3 + "Do you have an Acas early conciliation certificate number? Yes";
    public static final String PDF_TEMPLATE_Q2_3_1_2_FIRST_RESPONDENT_ACAS_CERTIFICATE_CHECK_NO =
        Q_2_3 + "Do you have an Acas early conciliation certificate number? No";
    public static final String PDF_TEMPLATE_Q2_3_2_FIRST_RESPONDENT_ACAS_CERTIFICATE_NUMBER =
        Q_2_3 + "please give the Acas early conciliation certificate number";
    public static final String PDF_TEMPLATE_Q2_3_3_1_FIRST_RESPONDENT_NO_ACAS_REASON_ANOTHER_PERSON =
        Q_2_3 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE + "Another person "
            + "I'm making the claim with has an Acas early conciliation certificate number";
    public static final String PDF_TEMPLATE_Q2_3_3_2_FIRST_RESPONDENT_NO_ACAS_REASON_NO_POWER_TO_CONCILIATE =
        Q_2_3 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + ACAS_NO_POWER_TO_CONCILIATE;
    public static final String PDF_TEMPLATE_Q2_3_3_3_FIRST_RESPONDENT_NO_ACAS_REASON_EMPLOYER_CONTACTED =
        Q_2_3 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + EMPLOYER_ALREADY_CONTACTED_ACAS;
    public static final String PDF_TEMPLATE_Q2_3_3_4_FIRST_RESPONDENT_NO_ACAS_REASON_UNFAIR_DISMISSAL =
        Q_2_3 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + CLAIM_CONSISTS_ONLY_COMPLAINT_OF_UNFAIR_DISMISSAL;
    public static final String PDF_TEMPLATE_Q2_4_1_CLAIMANT_WORK_ADDRESS =
        "2.4 Full, different working address - Number or name";
    public static final String PDF_TEMPLATE_Q2_4_2_CLAIMANT_WORK_POSTCODE =
        "2.4 Full, different working address - postcode";
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// END OF FIRST RESPONDENT FIELD NAMES //////////////////////////////////////////
    //////////////////////////////////////// SECOND RESPONDENT FIELD NAMES /////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String PDF_TEMPLATE_Q2_5_1_SECOND_RESPONDENT_NAME = "2.5 R2 name";
    public static final String PDF_TEMPLATE_Q2_5_2_SECOND_RESPONDENT_ADDRESS = "2.5 R2 number";
    public static final String PDF_TEMPLATE_Q2_5_3_SECOND_RESPONDENT_POSTCODE = "2.5 R2 postcode";
    public static final String PDF_TEMPLATE_Q2_6_1_1_SECOND_RESPONDENT_ACAS_CERTIFICATE_CHECK_YES =
        Q_2_6 + "Do you have an Acas early conciliation certificate number? Yes";
    public static final String PDF_TEMPLATE_Q2_6_1_2_SECOND_RESPONDENT_ACAS_CERTIFICATE_CHECK_NO =
        Q_2_6 + "Do you have an Acas early conciliation certificate number? No";
    public static final String PDF_TEMPLATE_Q2_6_2_SECOND_RESPONDENT_ACAS_CERTIFICATE_NUMBER =
        Q_2_6 + "please give the Acas early conciliation certificate number";
    public static final String PDF_TEMPLATE_Q2_6_3_1_SECOND_RESPONDENT_NO_ACAS_REASON_ANOTHER_PERSON =
        Q_2_6 + "why don't you have an Acas early conciliation certificate number? Another person "
            + "I'm making the claim with has an Acas early conciliation certificate number";
    public static final String PDF_TEMPLATE_Q2_6_3_2_SECOND_RESPONDENT_NO_ACAS_REASON_NO_POWER_TO_CONCILIATE =
        Q_2_6 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + ACAS_NO_POWER_TO_CONCILIATE;
    public static final String PDF_TEMPLATE_Q2_6_3_3_SECOND_RESPONDENT_NO_ACAS_REASON_EMPLOYER_CONTACTED =
        Q_2_6 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + EMPLOYER_ALREADY_CONTACTED_ACAS;
    public static final String PDF_TEMPLATE_Q2_6_3_4_SECOND_RESPONDENT_NO_ACAS_REASON_UNFAIR_DISMISSAL =
        Q_2_6 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + CLAIM_CONSISTS_ONLY_COMPLAINT_OF_UNFAIR_DISMISSAL;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// END OF SECOND RESPONDENT FIELD NAMES /////////////////////////////////////////
    //////////////////////////////////////// THIRD RESPONDENT FIELD NAMES //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String PDF_TEMPLATE_Q2_7_1_THIRD_RESPONDENT_NAME = "2.7 R3 name";
    public static final String PDF_TEMPLATE_Q2_7_2_THIRD_RESPONDENT_ADDRESS = "2.7 R3 number";
    public static final String PDF_TEMPLATE_Q2_7_3_THIRD_RESPONDENT_POSTCODE = "2.7 R3 postcode";
    public static final String PDF_TEMPLATE_Q2_8_1_1_THIRD_RESPONDENT_ACAS_CERTIFICATE_CHECK_YES = "2.8 yes";
    public static final String PDF_TEMPLATE_Q2_8_1_2_THIRD_RESPONDENT_ACAS_CERTIFICATE_CHECK_NO = "2.8 no";
    public static final String PDF_TEMPLATE_Q2_8_2_THIRD_RESPONDENT_ACAS_CERTIFICATE_NUMBER =
        "2.8 please give the Acas early conciliation certificate number";
    public static final String PDF_TEMPLATE_Q2_8_3_1_THIRD_RESPONDENT_NO_ACAS_REASON_ANOTHER_PERSON =
        Q_2_8 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + ANOTHER_PERSON_HAS_AN_ACAS_EARLY_CONCILIATION_CERTIFICATE_NUMBER;
    public static final String PDF_TEMPLATE_Q2_8_3_2_THIRD_RESPONDENT_NO_ACAS_REASON_NO_POWER_TO_CONCILIATE =
        Q_2_8 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + ACAS_NO_POWER_TO_CONCILIATE;
    public static final String PDF_TEMPLATE_Q2_8_3_3_THIRD_RESPONDENT_NO_ACAS_REASON_EMPLOYER_CONTACTED =
        Q_2_8 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + EMPLOYER_ALREADY_CONTACTED_ACAS;
    public static final String PDF_TEMPLATE_Q2_8_3_4_THIRD_RESPONDENT_NO_ACAS_REASON_UNFAIR_DISMISSAL =
           Q_2_8 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
               + CLAIM_CONSISTS_ONLY_COMPLAINT_OF_UNFAIR_DISMISSAL;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// END OF THIRD RESPONDENT FIELD NAMES //////////////////////////////////////////
    //////////////////////////////////////// FORTH RESPONDENT FIELD NAMES //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String PDF_TEMPLATE_Q13_1_1_FORTH_RESPONDENT_NAME = "13 R4 name";
    public static final String PDF_TEMPLATE_Q13_1_2_FORTH_RESPONDENT_ADDRESS = "13 R4 number";
    public static final String PDF_TEMPLATE_Q13_1_3_FORTH_RESPONDENT_POSTCODE = "13 R4 postcode";
    public static final String PDF_TEMPLATE_Q13_2_1_1_FORTH_RESPONDENT_ACAS_CERTIFICATE_CHECK_YES =
        "13 R4 Do you have an Acas early conciliation certificate number? Yes";
    public static final String PDF_TEMPLATE_Q13_2_1_2_FORTH_RESPONDENT_ACAS_CERTIFICATE_CHECK_NO =
        "13 R4 Do you have an Acas early conciliation certificate number? No";
    public static final String PDF_TEMPLATE_Q13_2_2_FORTH_RESPONDENT_ACAS_CERTIFICATE_NUMBER =
        "13 R4 please give the Acas early conciliation certificate number";
    public static final String PDF_TEMPLATE_Q13_2_3_1_FORTH_RESPONDENT_NO_ACAS_REASON_ANOTHER_PERSON =
        Q_13_4 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + ANOTHER_PERSON_HAS_AN_ACAS_EARLY_CONCILIATION_CERTIFICATE_NUMBER;
    public static final String PDF_TEMPLATE_Q13_2_3_2_FORTH_RESPONDENT_NO_ACAS_REASON_NO_POWER_TO_CONCILIATE =
        Q_13_4 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + ACAS_NO_POWER_TO_CONCILIATE;
    public static final String PDF_TEMPLATE_Q13_2_3_3_FORTH_RESPONDENT_NO_ACAS_REASON_EMPLOYER_CONTACTED =
        Q_13_4 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + EMPLOYER_ALREADY_CONTACTED_ACAS;
    public static final String PDF_TEMPLATE_Q13_2_3_4_FORTH_RESPONDENT_NO_ACAS_REASON_UNFAIR_DISMISSAL =
        Q_13_4 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + CLAIM_CONSISTS_ONLY_COMPLAINT_OF_UNFAIR_DISMISSAL;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// END OF FORTH RESPONDENT FIELD NAMES //////////////////////////////////////////
    //////////////////////////////////////// FIFTH RESPONDENT FIELD NAMES //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String PDF_TEMPLATE_Q13_3_1_FIFTH_RESPONDENT_NAME = "13 R5 name";
    public static final String PDF_TEMPLATE_Q13_3_2_FIFTH_RESPONDENT_ADDRESS = "13 R5 number";
    public static final String PDF_TEMPLATE_Q13_3_3_FIFTH_RESPONDENT_POSTCODE = "13 R5 postcode";
    public static final String PDF_TEMPLATE_Q13_4_1_1_FIFTH_RESPONDENT_ACAS_CERTIFICATE_CHECK_YES =
        "13 R5 Do you have an Acas early conciliation certificate number? Yes";
    public static final String PDF_TEMPLATE_Q13_4_1_2_FIFTH_RESPONDENT_ACAS_CERTIFICATE_CHECK_NO =
        "13 R5 Do you have an Acas early conciliation certificate number? No";
    public static final String PDF_TEMPLATE_Q13_4_2_FIFTH_RESPONDENT_ACAS_CERTIFICATE_NUMBER =
        "13 R5 please give the Acas early conciliation certificate number";
    public static final String PDF_TEMPLATE_Q13_4_3_1_FIFTH_RESPONDENT_NO_ACAS_REASON_ANOTHER_PERSON =
        Q_13_5 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + ANOTHER_PERSON_HAS_AN_ACAS_EARLY_CONCILIATION_CERTIFICATE_NUMBER;
    public static final String PDF_TEMPLATE_Q13_4_3_2_FIFTH_RESPONDENT_NO_ACAS_REASON_NO_POWER_TO_CONCILIATE =
        Q_13_5 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + ACAS_NO_POWER_TO_CONCILIATE;
    public static final String PDF_TEMPLATE_Q13_4_3_3_FIFTH_RESPONDENT_NO_ACAS_REASON_EMPLOYER_CONTACTED =
        Q_13_5 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + EMPLOYER_ALREADY_CONTACTED_ACAS;
    public static final String PDF_TEMPLATE_Q13_4_3_4_FIFTH_RESPONDENT_NO_ACAS_REASON_UNFAIR_DISMISSAL =
        Q_13_5 + WHY_DONT_HAVE_ACAS_CONCILIATION_CERTIFICATE
            + CLAIM_CONSISTS_ONLY_COMPLAINT_OF_UNFAIR_DISMISSAL;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// END OF FIFTH RESPONDENT FIELD NAMES //////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_UNFAIR_DISMISSAL = "Unfair Dismissal";
    public static final String PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_ANOTHER_PERSON = "Another person";
    public static final String PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_NO_POWER = "No Power";
    public static final String PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_EMPLOYER_ALREADY_IN_TOUCH =
        "Employer already in touch";

    public static final int PDF_TEMPLATE_MULTIPLE_RESPONDENTS_MIN_NUMBER = 2;
    public static final String QX_PHONE_NUMBER = "%s phone number";
    public static final String QX_ACAS_NUMBER = "%s please give the Acas early conciliation certificate number";
    private static final String ACAS_NO_CERT = "%s why don't you have an Acas early conciliation certificate"
        + " number?";
    public static final String QX_ACAS_A1 = ACAS_NO_CERT + " My claim consists only of a complaint of"
        + " unfair dismissal which contains an application for interim relief. (See guidance)";
    public static final String QX_ACAS_A2 = ACAS_NO_CERT + " Another person I'm making the claim with has"
        + " an Acas early conciliation certificate number";
    public static final String QX_ACAS_A3 = ACAS_NO_CERT + " Acas doesn’t have the power to conciliate on"
        + " some or all of my claim";
    public static final String QX_ACAS_A4 = ACAS_NO_CERT + " My employer has already been in touch with "
        + "Acas";
    public static final String OTHER = "Other";
    public static final String MR = "Mr";
    public static final String MRS = "Mrs";
    public static final String MISS = "Miss";
    public static final String MS = "Ms";
    public static final String OTHER_SPECIFY = "Other_Specify";
    public static final String PHONE_NUMBER_PREFIX = "1.6";
    public static final String SEX_MALE = "Male";
    public static final String SEX_FEMALE = "Female";
    public static final String SEX_FEMALE_LOWERCASE = "female";
    public static final String SEX_PREFER_NOT_TO_SAY = "Prefer not to say";
    public static final String SEX_PREFER_NOT_TO_SAY_LOWERCASE = "prefer not to say";
    public static final String INVALID_NO_ACAS_REASON = "Invalid No ACAS Certificate Reason Selected!...";
    public static final String PDF_CREATION_ERROR = "Error while creating PDF document!...";
    public static final int NUMERIC_THREE_INT_VALUE = 3;
    public static final int NUMERIC_FOUR_INT_VALUE = 4;
    public static final int MAX_NUMBER_OF_RESPONDENTS = 5;
    public static final String VIDEO = "Video";
    public static final String PHONE = "Phone";
    public static final String NO_LONGER_WORKING = "No longer working";
    public static final String NOTICE = "Notice";
    public static final String ANNUALLY = "annually";
    public static final String WEEKLY = "Weekly";
    public static final String MONTHLY = "Monthly";
    public static final String MONTHS = "Months";
    public static final String WEEKS = "Weeks";
    public static final String ANNUAL = "Annual";
    public static final Map<String, String> TITLES = Map.of(
        MR, PdfMapperConstants.Q1_TITLE_MR,
        MRS, PdfMapperConstants.Q1_TITLE_MRS,
        MISS, PdfMapperConstants.Q1_TITLE_MISS,
        MS, PdfMapperConstants.Q1_TITLE_MS,
        OTHER, PdfMapperConstants.Q1_TITLE_OTHER,
        OTHER_SPECIFY, PdfMapperConstants.Q1_TITLE_OTHER_SPECIFY

    );
    public static final Map<String, String> TITLE_MAP = Map.of(
        MR, "Mister",
        MRS, "Missus",
        MISS, "Miss",
        MS, "Miz",
        OTHER, "Miz"
    );
    public static final String EMAIL = "Email";
    public static final String POST = "Post";
    public static final String FAX = "Fax";

    private PdfMapperConstants() {
        // private due to being class of constants
    }

}
