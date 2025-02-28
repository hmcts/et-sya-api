package uk.gov.hmcts.reform.et.syaapi.constants;

/**
 * Defines case role management constants.
 */
public final class ResponseConstants {

    public static final String EXCEPTION_AUTHORISATION_TOKEN_BLANK = "Authorisation token is blank";
    public static final String EXCEPTION_ET3_REQUEST_EMPTY = "ET3 request is empty";
    public static final String EXCEPTION_ET3_SUBMISSION_REFERENCE_BLANK = "Case submission reference is blank";
    public static final String EXCEPTION_ET3_CASE_TYPE_BLANK = "Case submission reference is blank";
    public static final String EXCEPTION_ET3_REQUEST_TYPE_BLANK = "ET3 request type is blank";
    public static final String EXCEPTION_ET3_RESPONDENT_EMPTY = "ET3 respondent is empty";
    public static final String EXCEPTION_ET3_RESPONDENT_IDAM_ID_IS_BLANK = "Respondent idam id is blank";
    public static final String EXCEPTION_ET3_RESPONDENT_CCD_ID_IS_BLANK = "Respondent ccd id is blank";
    public static final String EXCEPTION_UNABLE_TO_FIND_CASE_DETAILS =
        "Unable to find case details with case submission reference %s";
    public static final String EXCEPTION_RESPONDENT_COLLECTION_IS_EMPTY = "Respondent collection is empty";
    public static final String EXCEPTION_RESPONDENT_NOT_FOUND = "Respondent not found";

    public static final String INVALID_CASE_DETAILS_SECTION_ID = "Invalid case details links section id";
    public static final String INVALID_RESPONSE_HUB_SECTION_ID = "Invalid respondent hub section id";

    public static final String CASE_DETAILS_SECTION_PERSONAL_DETAILS = "personalDetails";
    public static final String CASE_DETAILS_SECTION_ET1_CLAIM_FORM = "et1ClaimForm";
    public static final String CASE_DETAILS_SECTION_RESPONDENT_RESPONSE = "respondentResponse";
    public static final String CASE_DETAILS_SECTION_HEARING_DETAILS = "hearingDetails";
    public static final String CASE_DETAILS_SECTION_RESPONDENT_REQUESTS_AND_APPLICATIONS =
        "respondentRequestsAndApplications";
    public static final String CASE_DETAILS_SECTION_CLAIMANT_APPLICATIONS = "claimantApplications";
    public static final String CASE_DETAILS_SECTION_OTHER_RESPONDENT_APPLICATIONS = "otherRespondentApplications";
    public static final String CASE_DETAILS_SECTION_CONTACT_TRIBUNAL = "contactTribunal";
    public static final String CASE_DETAILS_SECTION_TRIBUNAL_ORDERS = "tribunalOrders";
    public static final String CASE_DETAILS_SECTION_TRIBUNAL_JUDGEMENTS = "tribunalJudgements";
    public static final String CASE_DETAILS_SECTION_DOCUMENTS = "documents";

    public static final String RESPONSE_HUB_SECTION_CONTACT_DETAILS = "contactDetails";
    public static final String RESPONSE_HUB_SECTION_EMPLOYER_DETAILS = "employerDetails";
    public static final String RESPONSE_HUB_SECTION_CONCILIATION_AND_EMPLOYEE_DETAILS
        = "conciliationAndEmployeeDetails";
    public static final String RESPONSE_HUB_SECTION_PAY_PENSION_BENEFIT_DETAILS = "payPensionBenefitDetails";
    public static final String RESPONSE_HUB_SECTION_CONTEST_CLAIM = "contestClaim";
    public static final String RESPONSE_HUB_SECTION_EMPLOYERS_CONTRACT_CLAIM = "employersContractClaim";
    public static final String RESPONSE_HUB_SECTION_CHECK_YOR_ANSWERS = "checkYorAnswers";
    public static final String ET3_FORM_DOCUMENT_TYPE = "ET3";
    public static final String UNABLE_TO_UPLOAD_DOCUMENT = "Unable to upload document";



    private ResponseConstants() {
        // restrict instantiation
    }
}
