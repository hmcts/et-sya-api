package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;

import java.io.IOException;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.AUTHORISATION_TOKEN_REGEX;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_AUTHORISATION_TOKEN_REGEX;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.HEADER_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.HEADER_CONTENT_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.HEADER_SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.HEADER_VALUE_APPLICATION_JSON;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_REVOKE;

public final class RemoteServiceUtil {

    private RemoteServiceUtil() {
        // restrict instantiation
    }


    /**
     * Returns HttpMethod by the given modification type. If modification type is Assignment then returns
     * HttpMethod POST else returns HttpMethod DELETE. Throws {@link ManageCaseRoleException} when modification
     * type is empty or not Assignment or Revoke
     * @param caseUserRoleModificationType modification type received from client.
     * @return HttpMethod type by the given modification type
     */
    public static HttpMethod getHttpMethodByCaseUserRoleModificationType(String caseUserRoleModificationType) {
        HttpMethod httpMethod = getHttpMethodByModificationType(caseUserRoleModificationType);
        if (ObjectUtils.isEmpty(httpMethod)) {
            throw new ManageCaseRoleException(
                new Exception(ManageCaseRoleConstants.EXCEPTION_INVALID_MODIFICATION_TYPE));
        }
        return httpMethod;
    }

    private static HttpMethod getHttpMethodByModificationType(String modificationType) {
        return MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType) ? HttpMethod.POST
            : MODIFICATION_TYPE_REVOKE.equals(modificationType) ? HttpMethod.DELETE
            : null;
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
}
