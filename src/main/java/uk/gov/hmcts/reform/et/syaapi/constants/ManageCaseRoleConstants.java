package uk.gov.hmcts.reform.et.syaapi.constants;

import java.util.List;

/**
 * Defines case role management constants.
 */
public final class ManageCaseRoleConstants {
    // Exceptions
    public static final String EXCEPTION_AUTHORISATION_TOKEN_REGEX = "authToken regex exception";
    public static final String EXCEPTION_INVALID_MODIFICATION_TYPE = "Invalid modification type";
    public static final String EXCEPTION_CASE_DETAILS_NOT_FOUND =
        "Case details not found with the given caseId, %s";
    public static final String EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS =
        "Case details not found because caseId or caseTypeId value is empty";
    public static final String EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA =
        "Case details with Case Id, %s doesn't have case data values";
    public static final String EXCEPTION_EMPTY_RESPONDENT_COLLECTION =
        "Respondent collection not found for the case with id, %s";
    public static final String EXCEPTION_EMPTY_RESPONDENT_COLLECTION_NOT_ABLE_TO_ADD_RESPONDENT =
        "Unable to add respondent idam id because there is not respondent defined in the case case with id, %s";
    public static final String EXCEPTION_IDAM_ID_ALREADY_EXISTS =
        "Unable to add respondent idam id because case has already been assigned "
            + "caseId, %s";
    public static final String EXCEPTION_IDAM_ID_ALREADY_EXISTS_SAME_USER =
        "You have already been assigned to this case caseId, %s ";
    public static final String EXCEPTION_RESPONDENT_NOT_FOUND =
        "Unable to add respondent idam id because there isn't any respondent with name %s";
    public static final String EXCEPTION_NOTICE_OF_CHANGE_ANSWER_NOT_FOUND =
        "Notice of change answer not found for the respondent with name, %s for the case with id, %s";
    public static final String EXCEPTION_RESPONDENT_SOLICITOR_TYPE_NOT_FOUND =
        "Respondent solicitor type not found for case with id, %s and respondent organisation policy index, %s";
    public static final String EXCEPTION_INVALID_IDAM_ID = "Invalid Idam ID";
    public static final String EXCEPTION_CASE_USER_ROLES_NOT_FOUND = "Case user roles not found for caseId: %s";
    public static final String EXCEPTION_CASE_USER_ROLE_NOT_FOUND = "Case user role not found for caseId: %s";
    public static final String EXCEPTION_INVALID_RESPONDENT_INDEX
        = "Respondent index, %s is not valid for the case with id, %s";
    public static final String EXCEPTION_RESPONDENT_NOT_FOUND_WITH_INDEX = "There is no respondent with index: %s";
    public static final String EXCEPTION_RESPONDENT_NOT_EXISTS =
        "Respondent does not exist for case: %s";
    public static final String EXCEPTION_NO_RESPONDENT_DEFINED =
        "There is no respondent defined in the case with id: %s";
    public static final String EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND =
        "Respondent representative not found for case: %s";


    public static final String MODIFY_CASE_ROLE_EMPTY_REQUEST = "Request to modify roles is empty";
    public static final String MODIFY_CASE_USER_ROLE_ITEM_INVALID = "One of the case user role modification item is "
        + "invalid. Invalid Data is For CaseId: %s";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String HEADER_VALUE_APPLICATION_JSON = "application/json";
    public static final String AUTHORISATION_TOKEN_REGEX = "[a-zA-Z0-9._\\s\\S]+$";
    public static final String MODIFICATION_TYPE_ASSIGNMENT = "Assignment";
    public static final String MODIFICATION_TYPE_UPDATE = "update";
    public static final String MODIFICATION_TYPE_REVOKE = "Revoke";
    public static final String CASE_USER_ROLE_CREATOR = "[CREATOR]";
    public static final String CASE_USER_ROLE_DEFENDANT = "[DEFENDANT]";
    public static final String CASE_USER_ROLE_CLAIMANT_SOLICITOR = "[CLAIMANTSOLICITOR]";
    public static final String STRING_AMPERSAND = "&";
    public static final String STRING_EQUAL = "=";
    public static final String STRING_QUESTION_MARK = "?";
    public static final String STRING_PARAM_NAME_CASE_IDS = "case_ids";
    public static final String STRING_PARAM_NAME_USER_IDS = "user_ids";
    public static final String CASE_USERS_API_URL = "/case-users";
    public static final String STRING_LEFT_SQUARE_BRACKET = "[";
    public static final String STRING_RIGHT_SQUARE_BRACKET = "]";
    public static final String CASE_USER_ROLE_API_PARAMETER_NAME = "case_user_role";
    public static final String CASE_USER_ROLE_CCD_API_POST_METHOD_NAME = "/case-users/search";
    public static final String CASE_USERS_RETRIEVE_API = "%s/case-users?case_ids=%s";
    public static final String LINK_STATUS_NOT_STARTED_YET = "notStartedYet";
    public static final String ET3_STATUS_IN_PROGRESS = "inProgress";
    public static final String SECTION_STATUS_COMPLETED = "completed";
    public static final String SECTION_STATUS_IN_PROGRESS = "inProgress";
    // This status is implemented as a fake status. Normally
    // if any section status is completed we don't change it is status
    // But if on CYA(Check Your Answers) page if it is selected as No,
    // to section completed question, we update to inProgress again.
    // To have that discrimination, we use this option.
    public static final String SECTION_STATUS_IN_PROGRESS_CYA = "inProgressCya";
    public static final String LINK_STATUS_NOT_VIEWED_YET = "notViewedYet";
    public static final String LINK_STATUS_NOT_AVAILABLE_YET = "notAvailableYet";
    public static final String LINK_STATUS_OPTIONAL = "optional";
    public static final String LINK_STATUS_CANNOT_START_YET = "cannotStartYet";
    public static final String CASE_STATE_ACCEPTED = "Accepted";
    public static final String STRING_TRUE = "true";
    public static final String STRING_FALSE = "false";
    public static final List<String> UNAUTHORIZED_APIS = List.of("/et3/findCaseByEthosCaseReference");
    public static final String MODIFICATION_TYPE_SUBMIT = "submit";
    public static final String ET3_STATUS_SUBMITTED = "submitted";
    public static final String RESPONSE_STATUS_COMPLETED = "completed";
    public static final String ET3_RESPONSE_LANGUAGE_PREFERENCE_WELSH = "Welsh";
    public static final String INVALID_CASE_USER_ROLE = "Invalid case user role: %s";
    public static final String INVALID_NOTICE_OF_CHANGE_ANSWER_INDEX = "Invalid notice of change answer index, %s "
        + "entered for the case with id: %s";

    private ManageCaseRoleConstants() {
        // restrict instantiation
    }
}
