package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignedUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.SearchCaseAssignedUserRolesRequest;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;
import uk.gov.hmcts.reform.et.syaapi.search.ElasticSearchQueryBuilder;
import uk.gov.hmcts.reform.et.syaapi.service.utils.DocumentUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ManageCaseRoleServiceUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.RemoteServiceUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.RespondentUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_REVOKE;

/**
 * Provides services for role modification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManageCaseRoleService {

    private String roleList;

    private final AdminUserService adminUserService;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi ccdApi;
    private final IdamClient idamClient;
    private final ET3Service et3Service;
    private final CaseService caseService;

    @Value("${assign_case_access_api_url}")
    private String aacUrl;

    @Value("${core_case_data.api.url}")
    private String ccdApiUrl;

    /**
     * Gets case with the user entered details, caseId, respondentName, claimantFirstNames and claimantSurname.
     * Returns null when case not found. It searches for the cases with administrator user to find if the case
     * exists with the given parameters. Also makes a security check if the user entered valid values.
     * @param findCaseForRoleModificationRequest It has the values caseId, respondentName, claimantFirstNames and
     *                                           claimantSurname values given by the respondent.
     * @return null if no case is found, CaseDetails if any case is found in both scotland and england wales case
     *              types.
     */
    public CaseDetails findCaseForRoleModification(
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest) {
        log.info("Trying to receive case for role modification. Submission Reference: {}",
                 findCaseForRoleModificationRequest.getCaseSubmissionReference());
        String adminUserToken = adminUserService.getAdminUserToken();
        String elasticSearchQuery = ElasticSearchQueryBuilder
            .buildByFindCaseForRoleModificationRequest(findCaseForRoleModificationRequest);
        List<CaseDetails> englandCases = Optional.ofNullable(ccdApi.searchCases(
            adminUserToken,
            authTokenGenerator.generate(),
            ENGLAND_CASE_TYPE,
            elasticSearchQuery
        ).getCases()).orElse(Collections.emptyList());
        if (CollectionUtils.isNotEmpty(englandCases)) {
            return englandCases.get(ManageCaseRoleConstants.FIRST_INDEX);
        }

        List<CaseDetails> scotlandCases = Optional.ofNullable(ccdApi.searchCases(
            adminUserToken,
            authTokenGenerator.generate(),
            SCOTLAND_CASE_TYPE,
            elasticSearchQuery
        ).getCases()).orElse(Collections.emptyList());
        if (CollectionUtils.isNotEmpty(scotlandCases)) {
            return scotlandCases.get(ManageCaseRoleConstants.FIRST_INDEX);
        }
        log.info("Case not found for the parameters, submission reference: {}",
                 findCaseForRoleModificationRequest.getCaseSubmissionReference());
        return null;
    }

    /**
     * Modifies user case roles by the given modification type. Gets case assignment user roles request which has
     * a list of case_users that contains case id, user id and case role to modify the case. For assigning a new role
     * to the case modification type should be Assignment, to revoke an existing role modification type should be
     * Revoke
     * @param authorisation Authorisation token of the user
     * @param modifyCaseUserRolesRequest This is the list of case roles that should be Revoked or Assigned to
     *                                   respondent. It also has the idam id of the respondent and the case id
     *                                   of the case that will be assigned
     * @param modificationType this value could be Assignment or Revoke.
     * @throws IOException Exception when any problem occurs while calling case assignment api (/case-users)
     */
    public void modifyUserCaseRoles(String authorisation,
                                    ModifyCaseUserRolesRequest modifyCaseUserRolesRequest,
                                    String modificationType)
        throws IOException {
        // Gets httpMethod by modification type. If modification type is Assignment, method is POST, if Revoke, method
        // is DELETE. Null if modification type is different then Assignment and Revoke.
        HttpMethod httpMethod = RemoteServiceUtil.getHttpMethodByCaseUserRoleModificationType(modificationType);
        // Checks modifyCaseUserRolesRequest parameter if it is empty or not and it's objects.
        // If there is any problem throws ManageCaseRoleException.
        ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(modifyCaseUserRolesRequest);
        if (MODIFICATION_TYPE_REVOKE.equals(modificationType)) {
            setAllRespondentsIdamIdAndDefaultLinkStatuses(authorisation, modifyCaseUserRolesRequest, modificationType);
        }
        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest =
            ManageCaseRoleServiceUtil.generateCaseAssignmentUserRolesRequestByModifyCaseUserRolesRequest(
                modifyCaseUserRolesRequest);
        log.info(getModifyUserCaseRolesLog(caseAssignmentUserRolesRequest, modificationType, true));
        ResponseEntity<CaseAssignmentUserRolesResponse> response;
        try {
            String adminToken = adminUserService.getAdminUserToken();
            HttpEntity<CaseAssignmentUserRolesRequest> requestEntity =
                new HttpEntity<>(caseAssignmentUserRolesRequest,
                                 RemoteServiceUtil.buildHeaders(adminToken, this.authTokenGenerator.generate()));
            response = restTemplate.exchange(ccdApiUrl + ManageCaseRoleConstants.CASE_USERS_API_URL,
                                             httpMethod,
                                             requestEntity,
                                             CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException | IOException exception) {
            log.info("Error from CCD - {}", exception.getMessage() + StringUtils.CR + roleList);
            throw exception;
        }
        if (MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)) {
            setAllRespondentsIdamIdAndDefaultLinkStatuses(authorisation, modifyCaseUserRolesRequest, modificationType);
        }
        log.info("{}" + StringUtils.CR + "Response status code: {} Response status code value: {}",
                 getModifyUserCaseRolesLog(caseAssignmentUserRolesRequest, modificationType, false),
                 response.getStatusCode(),
                 response.getStatusCodeValue());
    }

    private void setAllRespondentsIdamIdAndDefaultLinkStatuses(String authorisation,
                                                               ModifyCaseUserRolesRequest modifyCaseUserRolesRequest,
                                                               String modificationType) {
        for (ModifyCaseUserRole modifyCaseUserRole : modifyCaseUserRolesRequest.getModifyCaseUserRoles()) {
            if (CASE_USER_ROLE_DEFENDANT.equals(modifyCaseUserRole.getCaseRole())) {
                CaseDetails caseDetails =
                    et3Service.findCaseBySubmissionReference(modifyCaseUserRole.getCaseDataId());
                RespondentUtil.setRespondentIdamIdAndDefaultLinkStatuses(
                    caseDetails,
                    modifyCaseUserRole.getUserFullName(),
                    modifyCaseUserRole.getUserId(),
                    modificationType
                );
                et3Service.updateSubmittedCaseWithCaseDetails(authorisation, caseDetails);
            }
        }
    }

    private String getModifyUserCaseRolesLog(CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest,
                                            String modificationType,
                                            boolean isPreModify) {
        roleList = StringUtils.EMPTY;
        for (CaseAssignmentUserRole caseAssignmentUserRole
            : caseAssignmentUserRolesRequest.getCaseAssignmentUserRoles()) {
            roleList = "Case Id: " + caseAssignmentUserRole.getCaseDataId()
                + ", User Id: " + caseAssignmentUserRole.getUserId()
                + " Role: " + caseAssignmentUserRole.getCaseRole()
                + StringUtils.CR;
        }
        return (isPreModify
            ? ManageCaseRoleConstants.MODIFY_CASE_ROLE_PRE_WORDING
            : ManageCaseRoleConstants.MODIFY_CASE_ROLE_POST_WORDING)
            + "Modification type is: " + modificationType + StringUtils.CR
            + "Roles: " + roleList;

    }

    /**
     * It generates new CaseAssignmentUserRolesRequest that has the CaseUserRoles which has caseId, userId, role fields.
     * Reason to implement this method is, if userId is not received from the client, it automatically gets userId
     * from IDAM and sets that id to all CaseAssignmentUserRoles' userId fields.
     * @param authorisation Authorisation token to receive user information from IDAM.
     * @param modifyCaseUserRolesRequest ModifyCaseUserRolesRequest that contains ModifyCaseUserRoles
     *                                   received from client.
     * @return new CaseAssignmentUserRolesRequest that has userId which is received from IDAM.
     */
    public ModifyCaseUserRolesRequest generateModifyCaseUserRolesRequest(
        String authorisation, ModifyCaseUserRolesRequest modifyCaseUserRolesRequest) {
        UserInfo userInfo = idamClient.getUserInfo(authorisation);
        List<ModifyCaseUserRole> tmpModifyCaseUserRoles = new ArrayList<>();
        for (ModifyCaseUserRole modifyCaseUserRole : modifyCaseUserRolesRequest.getModifyCaseUserRoles()) {
            ModifyCaseUserRole tmpModifyCaseUserRole = ModifyCaseUserRole.builder()
                .caseTypeId(modifyCaseUserRole.getCaseTypeId())
                .caseDataId(modifyCaseUserRole.getCaseDataId())
                .userFullName(modifyCaseUserRole.getUserFullName())
                .caseRole(modifyCaseUserRole.getCaseRole())
                .userId(StringUtils.isBlank(modifyCaseUserRole.getUserId())
                            ? userInfo.getUid()
                            : modifyCaseUserRole.getUserId())
                .build();
            tmpModifyCaseUserRoles.add(tmpModifyCaseUserRole);
        }
        return ModifyCaseUserRolesRequest.builder().modifyCaseUserRoles(tmpModifyCaseUserRoles).build();
    }

    /**
     * Gets list of case user roles with the given case details list and authorization parameter.
     * @param authorization is used to get user info from IDAM.
     * @param caseDetailsList is used to get case user roles from core case data service.
     * @return list of case user roles.
     * @throws IOException throws when any error occurs while receiving case user roles.
     */
    public CaseAssignedUserRolesResponse getCaseUserRolesByCaseAndUserIdsAac(
        String authorization, List<CaseDetails> caseDetailsList) throws IOException {
        UserInfo userInfo = idamClient.getUserInfo(authorization);
        String aacApiUri = ManageCaseRoleServiceUtil
            .createAacSearchCaseUsersUriByCaseAndUserIds(aacUrl, caseDetailsList, List.of(userInfo));
        if (StringUtils.isBlank(aacApiUri)) {
            throw new ManageCaseRoleException(
                new Exception("Unable to get user cases because not able to create aacApiUrl with the given "
                                  + "caseDetails and authorization data"));
        }
        ResponseEntity<CaseAssignedUserRolesResponse> response;
        try {
            HttpEntity<Object> requestEntity =
                new HttpEntity<>(RemoteServiceUtil.buildHeaders(authorization, this.authTokenGenerator.generate()));
            response = restTemplate.exchange(
                aacApiUri,
                HttpMethod.GET,
                requestEntity,
                CaseAssignedUserRolesResponse.class);
        } catch (RestClientResponseException | IOException exception) {
            log.info("Error while getting user roles from CCD - {}", exception.getMessage());
            throw exception;
        }
        return response.getBody();
    }

    /**
     * Gets list of case user roles with the given case details list and authorization parameter by POST method.
     * When there are too many cases GET method URL exceeds max size(8192 byte or 8KB). That is why this method is
     * implemented.
     * @param authorization is used to get user info from IDAM.
     * @param caseDetailsList is used to get case user roles from core case data service.
     * @return list of case user roles.
     */
    public CaseAssignedUserRolesResponse getCaseUserRolesByCaseAndUserIdsCcd(
        String authorization, List<CaseDetails> caseDetailsList) throws IOException {
        if (CollectionUtils.isEmpty(caseDetailsList)) {
            return CaseAssignedUserRolesResponse.builder().build();
        }
        List<String> caseIds = new ArrayList<>();
        for (CaseDetails caseDetails : caseDetailsList) {
            if (ObjectUtils.isNotEmpty(caseDetails) && ObjectUtils.isNotEmpty(caseDetails.getId())) {
                caseIds.add(caseDetails.getId().toString());
            }
        }
        if (CollectionUtils.isEmpty(caseIds)) {
            return CaseAssignedUserRolesResponse.builder().build();
        }
        UserInfo userInfo = idamClient.getUserInfo(authorization);
        SearchCaseAssignedUserRolesRequest searchCaseAssignedUserRolesRequest = SearchCaseAssignedUserRolesRequest
            .builder()
            .caseIds(caseIds)
            .userIds(List.of(userInfo.getUid()))
            .build();
        CaseAssignedUserRolesResponse response;
        try {
            HttpEntity<SearchCaseAssignedUserRolesRequest> requestHttpEntity =
                new HttpEntity<>(searchCaseAssignedUserRolesRequest,
                                 RemoteServiceUtil.buildHeaders(authorization, this.authTokenGenerator.generate()));
            response = restTemplate
                .postForObject(ccdApiUrl + ManageCaseRoleConstants.CASE_USER_ROLE_CCD_API_POST_METHOD_NAME,
                               requestHttpEntity,
                               CaseAssignedUserRolesResponse.class);
        } catch (RestClientResponseException | IOException exception) {
            log.info("Error while getting user roles from CCD by POST method - {}", exception.getMessage());
            throw exception;
        }
        return response;
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

    /**
     * With given caseId, gets the case details, by case user role and returns case details by filtering documents
     * with the given caseUserRole.
     *
     * @param authorization is used to get the {@link UserInfo} for the request
     * @return the associated {@link CaseDetails} for the ID provided
     */
    // @Retryable({FeignException.class, RuntimeException.class}) --> No need to give exception classes as Retryable
    // covers all runtime exceptions.
    @Retryable
    public CaseDetails getUserCaseByCaseUserRole(String authorization,
                                                 String caseId,
                                                 String caseUserRole) {
        CaseDetails caseDetails = ccdApi.getCase(authorization, authTokenGenerator.generate(), caseId);
        if (ObjectUtils.isEmpty(caseDetails)) {
            throw new ManageCaseRoleException(
                new Exception("Unable to find user case by case id: " + caseDetails.getId()));
        }
        List<CaseDetails> caseDetailsListByCaseUserRole =
            getCasesByCaseDetailsListAuthorizationAndCaseUserRole(List.of(caseDetails), authorization, caseUserRole);
        return CollectionUtils.isNotEmpty(caseDetailsListByCaseUserRole)
            ? caseDetailsListByCaseUserRole.get(ManageCaseRoleConstants.FIRST_INDEX)
            : null;
    }

    /**
     * Given a user derived from the authorisation token in the request,
     * gets all cases {@link CaseDetails} for that user and filters case documents.
     *
     * @param authorization is used to get the {@link UserInfo} for the request
     * @return the associated {@link CaseDetails} list for the authorization code of the user provided
     */
    // @Retryable({FeignException.class, RuntimeException.class}) --> No need to give exception classes as Retryable
    // covers all runtime exceptions.
    @Retryable
    public List<CaseDetails> getUserCasesByCaseUserRole(String authorization, String caseUserRole) {
        List<CaseDetails> caseDetailsList = caseService.getAllUserCases(authorization);
        return getCasesByCaseDetailsListAuthorizationAndCaseUserRole(caseDetailsList, authorization, caseUserRole);
    }

    private List<CaseDetails> getCasesByCaseDetailsListAuthorizationAndCaseUserRole(
        List<CaseDetails> caseDetailsList, String authorization, String caseUserRole) {
        List<CaseDetails> caseDetailsListByRole;
        try {
            CaseAssignedUserRolesResponse caseAssignedUserRolesResponse =
                getCaseUserRolesByCaseAndUserIdsCcd(authorization, caseDetailsList);
            caseDetailsListByRole = ManageCaseRoleService
                .getCaseDetailsByCaseUserRole(caseDetailsList,
                                              caseAssignedUserRolesResponse.getCaseAssignedUserRoles(),
                                              caseUserRole);
            DocumentUtil.filterCasesDocumentsByCaseUserRole(caseDetailsListByRole, caseUserRole);
        } catch (IOException e) {
            throw new ManageCaseRoleException(e);
        }
        return caseDetailsListByRole;
    }
}
