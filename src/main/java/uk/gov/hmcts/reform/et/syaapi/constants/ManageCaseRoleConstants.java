package uk.gov.hmcts.reform.et.syaapi.constants;

import org.apache.commons.lang3.StringUtils;

/**
 * Defines case role management constants.
 */
public final class ManageCaseRoleConstants {
    public static final String MODIFY_CASE_ROLE_PRE_WORDING = "Received a request to modify roles:" + StringUtils.CR;
    public static final String MODIFY_CASE_ROLE_POST_WORDING = "Modified roles:" + StringUtils.CR;
    public static final String MODIFY_CASE_ROLE_EMPTY_REQUEST = "Request to modify roles is empty";
    public static final String MODIFY_CASE_USER_ROLE_ITEM_INVALID = "One of the case user role modification item is "
        + "invalid. Invalid Data is For CaseId: %s";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String HEADER_VALUE_APPLICATION_JSON = "application/json";
    public static final String AUTHORISATION_TOKEN_REGEX = "[a-zA-Z0-9._\\s\\S]+$";
    public static final String EXCEPTION_AUTHORISATION_TOKEN_REGEX = "authToken regex exception";
    public static final String EXCEPTION_INVALID_MODIFICATION_TYPE = "Invalid modification type";
    public static final String MODIFICATION_TYPE_ASSIGNMENT = "Assignment";
    public static final String MODIFICATION_TYPE_REVOKE = "Revoke";
    public static final int FIRST_INDEX = 0;
    public static final String CASE_USER_ROLE_SUCCESSFULLY_MODIFIED = "Successfully modified case user role";
    public static final String CASE_USER_ROLE_CREATOR = "[CREATOR]";
    public static final String CASE_USER_ROLE_DEFENDANT = "[DEFENDANT]";
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
    public static final String LINK_STATUS_NOT_STARTED_YET = "notStartedYet";
    public static final String LINK_STATUS_NOT_VIEWED_YET = "notViewedYet";
    public static final String LINK_STATUS_NOT_AVAILABLE_YET = "notAvailableYet";
    public static final String LINK_STATUS_OPTIONAL = "optional";
    public static final String LINK_STATUS_CANNOT_START_YET = "cannotStartYet";

    public static final String EXCEPTION_CASE_DETAILS_NOT_FOUND =
        "Case details not found with the given caseId, %s";
    public static final String EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS =
        "Case details not found because caseId or caseTypeId value is empty";
    public static final String EXCEPTION_CASE_DATA_NOT_FOUND =
        "Case details with Case Id, %s doesn't have case data values";
    public static final String EXCEPTION_EMPTY_RESPONDENT_COLLECTION =
        "Unable to add respondent idam id because there is not respondent defined in the case case with id, %s";
    public static final String EXCEPTION_IDAM_ID_ALREADY_EXISTS =
        "Unable to add respondent idam id because it has already been assigned to the respondent "
            + "with name %s, and caseId, %s";
    public static final String EXCEPTION_RESPONDENT_NOT_FOUND_WITH_RESPONDENT_NAME =
        "Unable to add respondent idam id because there isn't any respondent with name %s";
    public static final String EXCEPTION_INVALID_IDAM_ID = "Invalid Idam ID";

    private ManageCaseRoleConstants() {
        // restrict instantiation
    }
}
