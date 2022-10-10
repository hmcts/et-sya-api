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
    public static final String AGE = "Age";
    public static final String DISABILITY = "Disability";
    public static final String ETHNICITY = "Ethnicity";
    public static final String GENDER_REASSIGNMENT = "Gender reassignment";
    public static final String MARRIAGE_OR_CIVIL_PARTNERSHIP = "Marriage or civil partnership";
    public static final String PREGNANCY_OR_MATERNITY = "Pregnancy or maternity";
    public static final String RACE = "Race";
    public static final String RELIGION_OR_BELIEF = "Religion or belief";
    public static final String SEX = "Sex";
    public static final String SEXUAL_ORIENTATION = "Sexual orientation";

    //Payment types:
    public static final String ARREARS = "Arrears";
    public static final String HOLIDAY_PAY = "Holiday pay";
    public static final String NOTICE_PAY = "Notice pay";
    public static final String REDUNDANCY_PAY = "Redundancy pay";
    public static final String OTHER_PAYMENTS = "Other payments";

    //Outcome types
    public static final String COMPENSATION_ONLY = "compensation";
    public static final String TRIBUNAL = "tribunal";
    public static final String OLD_JOB = "oldJob";
    public static final String ANOTHER_JOB = "anotherJob";

    private ClaimTypesConstants() {
        // restrict instantiation
    }
}
