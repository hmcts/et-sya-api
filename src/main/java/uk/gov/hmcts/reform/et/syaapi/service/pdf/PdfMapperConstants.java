package uk.gov.hmcts.reform.et.syaapi.service.pdf;

/**
 * Defines the input labels within the template 'Employment tribunal claim form' (ver. ET1_0922) document as constants.
 */
public final class PdfMapperConstants {
    public static final String TRIBUNAL_OFFICE =        "tribunal office";
    public static final String CASE_NUMBER =            "case number";
    public static final String DATE_RECEIVED =          "date received";
    public static final String Q1_TITLE_MR =            "1.1 Title Mr";
    public static final String Q1_TITLE_MRS =           "1.1 Title Mrs";
    public static final String Q1_TITLE_MISS =          "1.1 Title Miss";
    public static final String Q1_TITLE_MS =            "1.1 Title Ms";
    public static final String Q1_TITLE_OTHER =         "1.1 Title other";
    public static final String Q1_TITLE_OTHER_SPECIFY = "1.1 other specify";
    public static final String Q1_FIRST_NAME =          "1.2 first names";
    public static final String Q1_SURNAME =             "1.3 surname";
    public static final String Q1_DOB_DAY =             "1.4 DOB day";
    public static final String Q1_DOB_MONTH =           "1.4 DOB month";
    public static final String Q1_DOB_YEAR =            "1.4 DOB year";
    public static final String Q1_SEX_MALE =             "1.5 sex";
    public static final String Q1_SEX_FEMALE =          "1.5 sex female";
    public static final String Q1_SEX_PREFER_NOT_TO_SAY = "1.5 sex prefer not to say";
    public static final String Q1_MOBILE_NUMBER =       "1.7 mobile number";
    public static final String Q1_CONTACT_POST =        "1.8 How should we contact you - Post";
    public static final String Q1_CONTACT_EMAIL =       "1.8 How should we contact you - Email";
    public static final String Q1_EMAIL =               "1.9 email";
    public static final String I_CAN_TAKE_PART_IN_VIDEO_HEARINGS = "1.11 video";
    public static final String I_CAN_TAKE_PART_IN_PHONE_HEARINGS = "1.11 phone";
    public static final String I_CAN_TAKE_PART_IN_NO_HEARINGS = "1.11 no";
    public static final String I_CAN_TAKE_PART_IN_NO_HEARINGS_EXPLAIN = "1.11 explain";

    public static final String Q2_EMPLOYER_NAME =       "2.1 Give the name of your employer or the person or "
        + "organisation you are claiming against";
    public static final String Q2_DIFFADDRESS_NUMBER =  "2.4 Full, different working address - Number or name";
    public static final String Q2_DIFFADDRESS_STREET =  "2.4 Full, different working address - Street";
    public static final String Q2_DIFFADDRESS_TOWN =    "2.4 Full, different working address - Town or city";
    public static final String Q2_DIFFADDRESS_POSTCODE = "2.4 Full, different working address - postcode";
    public static final String Q2_DIFFADDRESS_COUNTY =  "2.4 Full, different working address - County";
    public static final String Q2_OTHER_RESPONDENTS =   "2.5 If there are other respondents please tick this box and "
        + "put their names and addresses here";
    public static final String Q3_MORE_CLAIMS_YES =     "3.1 Are you aware that your claim is one of a number of "
        + "claims against the same employer arising from the same, or similar, circumstances? Yes";
    public static final String Q3_MORE_CLAIMS_NO =      "3.1 Are you aware that your claim is one of a number of "
        + "claims against the same employer arising from the same, or similar, circumstances? No";
    public static final String Q4_EMPLOYED_BY_YES =     "4.1 yes";
    public static final String Q4_EMPLOYED_BY_NO =      "4.1 no";
    public static final String Q5_EMPLOYMENT_START =    "5.1 employment start";
    public static final String Q5_EMPLOYMENT_END =      "5.1 employment end";
    public static final String Q5_NOT_ENDED =           "5.1 not ended";
    public static final String Q5_CONTINUING_YES =      "5.1 Is your employment continuing? Yes";
    public static final String Q5_CONTINUING_NO =       "5.1 Is your employment continuing? No";
    public static final String Q5_DESCRIPTION =         "5.2 Please say what job you do or did";
    public static final String Q6_HOURS =               "6.1 How many hours on average do, or did you work each week "
        + "in the job this claim is about?";
    public static final String Q6_GROSS_PAY =           "6.2 pay before tax";
    public static final String Q6_GROSS_PAY_WEEKLY =    "6.2 pay before tax - weekly";
    public static final String Q6_GROSS_PAY_MONTHLY =   "6.2 pay before tax - monthly";
    public static final String Q6_GROSS_PAY_ANNUAL =    "6.2 pay before tax - annually";
    public static final String Q6_NET_PAY =             "6.2 normal pay";
    public static final String Q6_NET_PAY_WEEKLY =      "6.2 normal pay - weekly";
    public static final String Q6_NET_PAY_MONTHLY =     "6.2 normal pay - monthly";
    public static final String Q6_NET_PAY_ANNUAL =      "6.2 normal pay - annually";
    public static final String Q6_PAID_NOTICE_YES =     "6.3 If your employment has ended, did you work (or were "
        + "you paid for) a period of notice? Yes";
    public static final String Q6_PAID_NOTICE_NO =      "6.3 If your employment has ended, did you work (or were you "
        + "paid for) a period of notice? No";
    public static final String Q6_NOTICE_WEEKS =        "6.3 If Yes, how many weeks’ notice did you work, or were you"
        + " paid for?";
    public static final String Q6_NOTICE_MONTHS =       "6.3 If Yes, how many months’ notice did you work, or were "
        + "you paid for?";
    public static final String Q6_PENSION_YES =         "6.4 Were you in your employer’s pension scheme? Yes";
    public static final String Q6_PENSION_NO =          "6.4 Were you in your employer’s pension scheme? No";
    public static final String Q6_PENSION_WEEKLY =      "6.4 employers weekly contributions";
    public static final String Q6_OTHER_BENEFITS =      "6.5 If you received any other benefits, e.g. company car, "
        + "medical insurance, etc, from your employer, please give details";
    public static final String Q7_OTHER_JOB_YES =       "7.1 Have you got another job? Yes";
    public static final String Q7_OTHER_JOB_NO =        "7.1 Have you got another job? No";
    public static final String Q7_START_WORK =          "7.2 Please say when you started (or will start) work";
    public static final String Q7_EARNING =             "7.3 Please say how much you are now earning (or will earn)";
    public static final String Q7_EARNING_WEEKLY =      "7.3 weekly";
    public static final String Q7_EARNING_MONTHLY =     "7.3 monthly";
    public static final String Q7_EARNING_ANNUAL =      "7.3 annually";
    public static final String Q8_TYPE_OF_CLAIM_DISCRIMINATION = "8.1 I was discriminated against on the grounds of";
    public static final String Q8_TYPE_OF_CLAIM_UNFAIRLY_DISMISSED =
        "8.1 I was unfairly dismissed (including constructive dismissal)";
    public static final String Q8_TYPE_OF_CLAIM_WHISTLE_BLOWING = "8.1 whistleblowing";
    public static final String Q8_TYPE_OF_CLAIM_REDUNDANCY_PAYMENT = "8.1 I am claiming a redundancy payment";
    public static final String Q8_TYPE_OF_CLAIM_BREACH_OF_CONTRACT = "8.1 owed";
    public static final String Q8_TYPE_OF_CLAIM_OTHER_TYPES_OF_CLAIMS = "8.1 other type of claim";
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
    public static final String Q11_REP_NAME =           "11.1 Name of representative";
    public static final String Q11_REP_ORG =            "11.2 Name of organisation";
    public static final String Q11_REP_NUMBER =         "11.4 Representative's DX number (if known)";
    public static final String Q11_MOBILE_NUMBER =      "11.6 mobile number (if different)";
    public static final String Q11_REFERENCE =          "11.7 Their reference for correspondence";
    public static final String Q11_EMAIL =              "11.8 Email address";
    public static final String Q11_CONTACT_POST =       "11.9 How would you prefer us to communicate with them? Post";
    public static final String Q11_CONTACT_EMAIL =      "11.9 How would you prefer us to communicate with them? Email";
    public static final String QX_NAME =                "%s name";
    public static final String QX_HOUSE_NUMBER =        "%s number or name";
    public static final String RP2_HOUSE_NUMBER =        "%s number";
    public static final String QX_STREET =              "%s street";
    public static final String RP_POST_TOWN =           "%s town city";
    public static final String RP2_POST_TOWN =           "%s town";
    public static final String QX_POST_TOWN =           "%s town or city";

    public static final String QX_COUNTY =              "%s county";
    public static final String QX_POSTCODE =            "%s postcode";
    public static final String QX_PHONE_NUMBER =        "%s phone number";
    // R1 - "2.3"
    // R2 - "2.6"
    // R3 - "2.8"
    // R4 - "13 R4"
    // R5 - "13 R5"
    public static final String QX_HAVE_ACAS_YES =       "%s Do you have an Acas early conciliation certificate"
        + " number? Yes";
    public static final String QX_HAVE_ACAS_NO =        "%s Do you have an Acas early conciliation certificate "
        + "number? No";
    public static final String QX_ACAS_NUMBER =         "%s please give the Acas early conciliation certificate number";
    private static final String ACAS_NO_CERT =          "%s why don't you have an Acas early conciliation certificate"
        + " number?";
    public static final String QX_ACAS_A1 =             ACAS_NO_CERT + " My claim consists only of a complaint of"
        + " unfair dismissal which contains an application for interim relief. (See guidance)";
    public static final String QX_ACAS_A2 =             ACAS_NO_CERT + " Another person I'm making the claim with has"
        + " an Acas early conciliation certificate number";
    public static final String QX_ACAS_A3 =             ACAS_NO_CERT + " Acas doesn’t have the power to conciliate on"
        + " some or all of my claim";
    public static final String QX_ACAS_A4 =             ACAS_NO_CERT + " My employer has already been in touch with "
        + "Acas";

    private PdfMapperConstants() {
        // private due to being class of constants
    }
}
