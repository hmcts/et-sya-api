package uk.gov.hmcts.reform.et.syaapi.constants;

import org.apache.commons.lang3.StringUtils;

/**
 * Defines case role management constants.
 */
public final class CaseRoleManagementConstants {
    public static final String MODIFY_CASE_ROLE_PRE_WORDING = "Received a request to modify roles:" + StringUtils.CR;
    public static final String MODIFY_CASE_ROLE_POST_WORDING = "Modified roles:" + StringUtils.CR;
    public static final String MODIFY_CASE_ROLE_EMPTY_REQUEST = "Request to modify roles is empty";
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
    public static final String CASE_DETAILS_NOT_FOUND_EXCEPTION = "Unable to find case with id:%s";
    public static final String CASE_USER_ROLE_SUCCESSFULLY_MODIFIED = "Successfully modified case user role";
    public static final String MODIFY_ROLE_NOTIFICATION_TITLE =
        "Your role modification request successfully completed";
    public static final String MODIFY_ROLE_NOTIFICATION_SUBJECT =
        "Role modification, %s successfully completed for the user with id, %s, to the claim with reference id, %s";
    public static final String MODIFY_ROLE_NOTIFICATION_PARTY = "Respondent";
    public static final String MODIFY_ROLE_NOTIFICATION_DETAILS =
        "Role modification type, %s of the role, %s completed for user, %s to case with submission reference, %s";
    public static final String MODIFY_ROLE_NOTIFICATION_SENT_FROM = "hmcts@hmcts.net";

    private CaseRoleManagementConstants() {
        // restrict instantiation
    }
}
