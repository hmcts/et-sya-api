package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.AUTHORISATION_TOKEN_REGEX;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USERS_API_URL;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_AUTHORISATION_TOKEN_REGEX;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.HEADER_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.HEADER_CONTENT_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.HEADER_SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.HEADER_VALUE_APPLICATION_JSON;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_REVOKE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.STRING_AMPERSAND;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.STRING_EQUAL;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.STRING_PARAM_NAME_CASE_IDS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.STRING_PARAM_NAME_USER_IDS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.STRING_QUESTION_MARK;

@Slf4j
public final class ManageCaseRoleServiceUtil {
    private ManageCaseRoleServiceUtil() {
        // restrict instantiation
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

    /**
     * This method is implemented because SpringUtils of creation of URI components(UriComponentsBuilder)
     * has vulnerability (<a href="https://nvd.nist.gov/vuln/detail/CVE-2024-22243">CVE-2024-22243</a>).
     * On the date of implementation(1st of September 2024) that vulnerability was not resolved.
     * Gets aacUrl, caseDetailsList and userInfoList to create aacApiUrl to search case users.
     * AAC stands for manage case assignment project.
     * @param aacUrl is the url value of aac host.
     * @param caseDetailsList list of case details of the users which have all the roles
     * @param userInfoList list of user details for cases user search.
     * @return aacApiUri to call case user search api of aac.
     */
    public static String createAacSearchCaseUsersUriByCaseAndUserIds(String aacUrl,
                                                                     List<CaseDetails> caseDetailsList,
                                                                     List<UserInfo> userInfoList) {
        if (StringUtils.isBlank(aacUrl) || CollectionUtils.isEmpty(caseDetailsList)) {
            return StringUtils.EMPTY;
        }
        String caseIdsUri = generateUriByCaseDetailsList(caseDetailsList);
        if (caseIdsUri.isEmpty()) {
            return StringUtils.EMPTY;
        }
        String aacApiUriAsString = aacUrl + CASE_USERS_API_URL + STRING_QUESTION_MARK + caseIdsUri;
        aacApiUriAsString = aacApiUriAsString + generateUriByUserInfoList(userInfoList);
        aacApiUriAsString = StringUtils.removeEnd(aacApiUriAsString, STRING_AMPERSAND);
        return aacApiUriAsString;
    }

    private static String generateUriByCaseDetailsList(List<CaseDetails> caseDetailsList) {
        StringBuilder caseIdsUri = new StringBuilder(StringUtils.EMPTY);
        for (CaseDetails caseDetails : caseDetailsList) {
            if (ObjectUtils.isNotEmpty(caseDetails) && ObjectUtils.isNotEmpty(caseDetails.getId())) {
                caseIdsUri.append(STRING_PARAM_NAME_CASE_IDS)
                    .append(STRING_EQUAL).append(caseDetails.getId().toString()).append(STRING_AMPERSAND);
            }
        }
        return caseIdsUri.toString();
    }

    private static String generateUriByUserInfoList(List<UserInfo> userInfoList) {
        StringBuilder userIdsUri = new StringBuilder(StringUtils.EMPTY);
        if (CollectionUtils.isNotEmpty(userInfoList)) {
            for (UserInfo userInfo : userInfoList) {
                if (ObjectUtils.isNotEmpty(userInfo) && StringUtils.isNotBlank(userInfo.getUid())) {
                    userIdsUri
                        .append(STRING_PARAM_NAME_USER_IDS)
                        .append(STRING_EQUAL)
                        .append(userInfo.getUid())
                        .append(STRING_AMPERSAND);
                }
            }
        }
        return userIdsUri.toString();
    }
}
