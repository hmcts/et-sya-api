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
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_STATE_ACCEPTED;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USERS_API_URL;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USERS_RETRIEVE_API;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CLAIMANT_SOLICITOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_IDAM_ID_ALREADY_EXISTS_SAME_USER;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFY_CASE_ROLE_EMPTY_REQUEST;
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

    /**
     * This service is used to create CaseAssignmentUserRoles request with the given ModifyCaseUserRoles request to
     * call case assignment service case-users of aac(assign case access API URrl) with POST method.
     * @param modifyCaseUserRolesRequest is the parameter that has the required fields for creating
     *                                   {@link CaseAssignmentUserRolesRequest} for modifying case role.
     * @return                           {@link CaseAssignmentUserRolesRequest} to use for assigning case user roles.
     */
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

    /**
     * This method is used to find all cases with the given case user roles. It is used to discriminate
     * CREATOR & DEFENDANT cases for the given user. For et-syr applications we are showing users only the cases that
     * they are DEFENDANT and on et-sya, CREATOR.
     * @param caseDetails               {@link CaseDetails} has the case data that needs to be compared with
     *                                  {@link CaseAssignmentUserRole} values
     * @param caseAssignmentUserRole    has the role value that needs to be checked with existing CaseDetails.
     * @return Case role value as DEFENDANT or CREATOR. If nothing found returns empty string.
     */
    public static String findCaseUserRole(CaseDetails caseDetails,
                                           CaseAssignmentUserRole caseAssignmentUserRole) {
        return ObjectUtils.isNotEmpty(caseDetails)
            && ObjectUtils.isNotEmpty(caseDetails.getId())
            && ObjectUtils.isNotEmpty(caseAssignmentUserRole)
            && StringUtils.isNotEmpty(caseAssignmentUserRole.getCaseDataId())
            && (caseDetails.getId().toString().equals(caseAssignmentUserRole.getCaseDataId()))
            && (CASE_USER_ROLE_CREATOR.equals(caseAssignmentUserRole.getCaseRole())
            ||  CASE_USER_ROLE_DEFENDANT.equals(caseAssignmentUserRole.getCaseRole()))
            ? caseAssignmentUserRole.getCaseRole() : StringUtils.EMPTY;
    }

    /**
     * Checks the data entered in {@link ModifyCaseUserRolesRequest} before starting user case role assignment.
     * @param modifyCaseUserRolesRequest is the data that has the required values to assign user case roles which are
     *                                   case_type_id, user_full_name, case_id, user_id, case_role.
     */
    public static void checkModifyCaseUserRolesRequest(ModifyCaseUserRolesRequest modifyCaseUserRolesRequest) {
        if (ObjectUtils.isEmpty(modifyCaseUserRolesRequest)
            || CollectionUtils.isEmpty(modifyCaseUserRolesRequest.getModifyCaseUserRoles())) {
            throw new ManageCaseRoleException(new Exception(
                MODIFY_CASE_ROLE_EMPTY_REQUEST));
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
            && StringUtils.isBlank(modifyCaseUserRole.getRespondentName())
            && isCaseRoleInvalid(modifyCaseUserRole.getCaseRole())) {
            throw new ManageCaseRoleException(
                new Exception(String.format(MODIFY_CASE_USER_ROLE_ITEM_INVALID, modifyCaseUserRole.getCaseDataId())));
        }
    }

    private static boolean isCaseRoleInvalid(String caseRole) {
        return StringUtils.isBlank(caseRole)
            || !CASE_USER_ROLE_DEFENDANT.equals(caseRole)
            && !CASE_USER_ROLE_CREATOR.equals(caseRole)
            && !CASE_USER_ROLE_CLAIMANT_SOLICITOR.equals(caseRole);
    }

    /**
     * Gets case details list and case assignment user roles as parameter to check if the case in case details list
     * has the required case user role.
     * @param caseDetailsList case details list received from core case data.
     * @param caseAssignmentUserRoles roles defined for the list of case details.
     * @param caseUserRole case user role to check if the case has the role or not.
     * @return case details list with the role given as parameter caseUserRole.
     */
    public static List<CaseDetails> getCaseDetailsByCaseUserRole(
        List<CaseDetails> caseDetailsList, List<CaseAssignmentUserRole> caseAssignmentUserRoles, String caseUserRole) {
        List<CaseDetails> caseDetailsListByRole = new ArrayList<>();
        if (CollectionUtils.isEmpty(caseDetailsList)
            || CollectionUtils.isEmpty(caseAssignmentUserRoles)
            || StringUtils.isBlank(caseUserRole)) {
            return caseDetailsListByRole;
        }
        for (CaseAssignmentUserRole caseAssignmentUserRole : caseAssignmentUserRoles) {
            for (CaseDetails caseDetails : caseDetailsList) {
                String tmpCaseUserRole = ManageCaseRoleServiceUtil.findCaseUserRole(
                    caseDetails, caseAssignmentUserRole);
                if (StringUtils.isNotBlank(tmpCaseUserRole) && tmpCaseUserRole.equals(caseUserRole)) {
                    caseDetailsListByRole.add(caseDetails);
                    break;
                }
            }
        }
        return caseDetailsListByRole;
    }

    public static boolean isCaseRoleAssignmentExceptionForSameUser(Exception exception) {
        return StringUtils.isNotBlank(exception.getMessage())
            && exception.getMessage().contains(
            EXCEPTION_IDAM_ID_ALREADY_EXISTS_SAME_USER
                .substring(0, EXCEPTION_IDAM_ID_ALREADY_EXISTS_SAME_USER.indexOf("%s")));
    }

    public static CaseDetails checkCaseDetailsList(List<CaseDetails> caseDetailsList) {
        if (CollectionUtils.isNotEmpty(caseDetailsList)) {
            CaseDetails caseDetails = caseDetailsList.getFirst();
            if (CASE_STATE_ACCEPTED.equals(caseDetails.getState())) {
                return caseDetails;
            }
        }
        return null;
    }

    /**
     * Builds a {@link CaseAssignmentUserRolesRequest} using the provided {@link UserInfo},
     * {@link CaseDetails}, and case role.
     *
     *  <p>
     *      This method constructs a single {@link CaseAssignmentUserRole} that associates the given user
     *      with the specified case and role. The constructed role assignment is wrapped in a
     *      {@link CaseAssignmentUserRolesRequest} and returned.
     *  </p>
     *
     * @param userIdamId  the user IDAM ID
     * @param caseDetails the case details, including the case ID
     * @param caseRole    the role to assign to the user for the given case (e.g., "[CLAIMANT]", "[DEFENDANT]")
     * @return a {@link CaseAssignmentUserRolesRequest} containing the role assignment
     */
    public static CaseAssignmentUserRolesRequest buildCaseUserRoleRequestByUserIdamIdCaseDetailsAndCaseRole(
        String userIdamId, CaseDetails caseDetails, String caseRole) {
        List<CaseAssignmentUserRole> caseAssignmentUserRoles = new ArrayList<>();
        caseAssignmentUserRoles.add(CaseAssignmentUserRole
                                        .builder()
                                        .caseDataId(String.valueOf(caseDetails.getId()))
                                        .userId(userIdamId)
                                        .caseRole(caseRole)
                                        .build());
        return CaseAssignmentUserRolesRequest.builder().caseAssignmentUserRoles(caseAssignmentUserRoles).build();
    }

    /**
     * Builds the URL used to retrieve case user access information from the CCD Data Store API.
     *
     * <p>This method formats a predefined endpoint URL by injecting the given CCD Data Store API base URL
     * and the case ID into the appropriate placeholders. The resulting URL is intended for retrieving
     * user assignments or access roles associated with the specified case.</p>
     *
     * @param ccdDataStoreApiBaseUrl the base URL of the CCD Data Store API
     * @param caseId the unique identifier of the case for which access details are being retrieved
     * @return a fully formed URL as a {@link String} to call the case user access retrieval API
     */
    public static String buildCaseAccessUrl(String ccdDataStoreApiBaseUrl,
                                            String caseId) {
        return String.format(CASE_USERS_RETRIEVE_API, ccdDataStoreApiBaseUrl, caseId);
    }
}
