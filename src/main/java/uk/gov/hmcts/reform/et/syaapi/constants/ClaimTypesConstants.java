package uk.gov.hmcts.reform.et.syaapi.constants;

/**
 * Defines the claim types as constants.
 */
public final class ClaimTypesConstants {
    //Claims types:
    public static final String BREACH_OF_CONTRACT = "breachOfContract";
    public static final String DISCRIMINATION = "discrimination";
    public static final String PAY_RELATED_CLAIM = "payRelated";
    public static final String UNFAIR_DISMISSAL = "unfairDismissal";
    public static final String WHISTLE_BLOWING = "whistleBlowing";
    public static final String OTHER_TYPES = "otherTypesOfClaims";

    //Discrimination types:
    public static final String AGE = "age";
    public static final String DISABILITY = "disability";
    public static final String ETHNICITY = "ethnicity";
    public static final String GENDER_REASSIGNMENT = "genderReassignment";
    public static final String MARRIAGE_OR_CIVIL_PARTNERSHIP = "marriageOrCivilPartnership";
    public static final String PREGNANCY_OR_MATERNITY = "pregnancyOrMaternity";
    public static final String RACE = "race";
    public static final String RELIGION_OR_BELIEF = "religionOrBelief";
    public static final String SEX = "Sex (Including equal pay)";
    public static final String SEXUAL_ORIENTATION = "sexualOrientation";

    //Payment types:
    public static final String ARREARS = "arrears";
    public static final String HOLIDAY_PAY = "holidayPay";
    public static final String NOTICE_PAY = "noticePay";
    public static final String REDUNDANCY_PAY = "redundancyPay";
    public static final String OTHER_PAYMENTS = "otherPayments";

    private ClaimTypesConstants() {
        // restrict instantiation
    }
}
