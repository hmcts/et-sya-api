package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;

import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.AUTHORISATION_TOKEN_REGEX;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.EXCEPTION_AUTHORISATION_TOKEN_REGEX;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.HEADER_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.HEADER_CONTENT_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.HEADER_SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.HEADER_VALUE_APPLICATION_JSON;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFICATION_TYPE_REVOKE;

@Slf4j
public final class CaseRoleManagementServiceUtil {
    private CaseRoleManagementServiceUtil() {
        // restrict instantiation
    }

    /**
     * returns user's full name by the given user info object which has name, given name family name attributes.
     * First checks for username if it exists puts username if not checks given name if it doesn't exist puts an
     * empty string for first name. For user's last name puts family name if exists. If not puts empty string for
     * the family name.
     * @param userInfo user info object that has name, family name and
     * @return user's full name
     */
    public static String getRespondentFullNameByUserInfo(UserInfo userInfo) {
        if (ObjectUtils.isEmpty(userInfo)) {
            return StringUtils.EMPTY;
        }
        return ((StringUtils.isNotBlank(userInfo.getName()) ? userInfo.getName()
            : StringUtils.isNotBlank(userInfo.getGivenName()) ? userInfo.getGivenName() : StringUtils.EMPTY)
            + StringUtils.SPACE
            + (StringUtils.isNotBlank(userInfo.getFamilyName()) ? userInfo.getFamilyName() : StringUtils.EMPTY)).trim();
    }

    /**
     * Generates HttpHeaders with the given user and service authorisation tokens.
     * @param authToken authorisation token of the claimant
     * @param serviceAuthorisation service authorisation created by authorisation token generator
     * @return org.springframework.http.HttpsHeaders to call remote APIs
     * @throws IOException Thrown exception when authorisation token does not match with the authorisation token regex
     *                     which is [a-zA-Z0-9._\s\S]+$
     */
    public static HttpHeaders buildHeaders(String authToken, String serviceAuthorisation) throws IOException {
        if (authToken.matches(AUTHORISATION_TOKEN_REGEX)) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HEADER_AUTHORIZATION, authToken);
            headers.add(HEADER_SERVICE_AUTHORIZATION, serviceAuthorisation);
            headers.add(HEADER_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON);
            return headers;
        } else {
            throw new IOException(EXCEPTION_AUTHORISATION_TOKEN_REGEX);
        }
    }

    /**
     * Returns HttpMethod by the given modification type. If modification type is Assignment then returns
     * HttpMethod POST else returns HttpMethod DELETE.
     * @param modificationType modification type received from client.
     * @return HttpMethod type by the given modification type
     */
    public static HttpMethod getHttpMethodByModificationType(String modificationType) {
        return MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType) ? HttpMethod.POST
            : MODIFICATION_TYPE_REVOKE.equals(modificationType) ? HttpMethod.DELETE
            : null;
    }
}
