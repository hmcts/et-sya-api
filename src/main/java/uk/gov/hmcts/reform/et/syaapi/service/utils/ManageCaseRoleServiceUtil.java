package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USERS_API_URL;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFY_CASE_USER_ROLE_ITEM_INVALID;
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

    public static CaseAssignmentUserRolesRequest generateCaseAssignmentUserRolesRequestByModifyCaseUserRolesRequest(
        ModifyCaseUserRolesRequest modifyCaseUserRolesRequest) {
        List<CaseAssignmentUserRole> caseAssignmentUserRoles = new ArrayList<>();
        for (ModifyCaseUserRole modifyCaseUserRole : modifyCaseUserRolesRequest.getModifyCaseUserRoles()) {
            caseAssignmentUserRoles.add(CaseAssignmentUserRole
                                            .builder()
                                            .caseDataId(modifyCaseUserRole.getCaseDataId())
                                            .userId(modifyCaseUserRole.getUserId())
                                            .caseRole(modifyCaseUserRole.getCaseRole())
                                            .build());
        }
        return CaseAssignmentUserRolesRequest.builder().caseAssignmentUserRoles(caseAssignmentUserRoles).build();
    }

    public static String findCaseUserRole(CaseDetails caseDetails,
                                           CaseAssignmentUserRole caseAssignmentUserRole) {
        return ObjectUtils.isNotEmpty(caseDetails)
            && ObjectUtils.isNotEmpty(caseDetails.getId())
            && ObjectUtils.isNotEmpty(caseAssignmentUserRole)
            && StringUtils.isNotEmpty(caseAssignmentUserRole.getCaseDataId())
            && (caseDetails.getId().toString().equals(caseAssignmentUserRole.getCaseDataId()))
            && (ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR.equals(caseAssignmentUserRole.getCaseRole())
            ||  ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT.equals(caseAssignmentUserRole.getCaseRole()))
            ? caseAssignmentUserRole.getCaseRole() : StringUtils.EMPTY;
    }

    public static void checkModifyCaseUserRolesRequest(ModifyCaseUserRolesRequest modifyCaseUserRolesRequest) {
        if (ObjectUtils.isEmpty(modifyCaseUserRolesRequest)
            || CollectionUtils.isEmpty(modifyCaseUserRolesRequest.getModifyCaseUserRoles())) {
            throw new ManageCaseRoleException(new Exception(
                ManageCaseRoleConstants.MODIFY_CASE_ROLE_EMPTY_REQUEST));
        }
        for (ModifyCaseUserRole modifyCaseUserRole : modifyCaseUserRolesRequest.getModifyCaseUserRoles()) {
            checkModifyCaseUserRole(modifyCaseUserRole);
        }
    }

    private static void checkModifyCaseUserRole(ModifyCaseUserRole modifyCaseUserRole) {
        if (ObjectUtils.isEmpty(modifyCaseUserRole)
            && StringUtils.isBlank(modifyCaseUserRole.getUserId())
            && StringUtils.isBlank(modifyCaseUserRole.getCaseTypeId())
            && StringUtils.isBlank(modifyCaseUserRole.getCaseDataId())
            && StringUtils.isBlank(modifyCaseUserRole.getUserFullName())
            && isCaseRoleInvalid(modifyCaseUserRole.getCaseRole())) {
            throw new ManageCaseRoleException(
                new Exception(String.format(MODIFY_CASE_USER_ROLE_ITEM_INVALID, modifyCaseUserRole.getCaseDataId())));
        }
    }

    private static boolean isCaseRoleInvalid(String caseRole) {
        return StringUtils.isBlank(caseRole)
            || !CASE_USER_ROLE_DEFENDANT.equals(caseRole)
            && !CASE_USER_ROLE_CREATOR.equals(caseRole);
    }
}
