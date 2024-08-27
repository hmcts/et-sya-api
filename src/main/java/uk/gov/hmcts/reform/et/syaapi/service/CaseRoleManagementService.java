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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseRoleManagementException;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;
import uk.gov.hmcts.reform.et.syaapi.search.ElasticSearchQueryBuilder;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.EXCEPTION_INVALID_MODIFICATION_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.FIRST_INDEX;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFY_CASE_ROLE_EMPTY_REQUEST;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFY_CASE_ROLE_POST_WORDING;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFY_CASE_ROLE_PRE_WORDING;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.buildHeaders;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.getHttpMethodByModificationType;

/**
 * Provides read and write access to cases stored by ET.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseRoleManagementService {

    private String roleList;

    private final AdminUserService adminUserService;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi ccdApi;
    private final IdamClient idamClient;

    @Value("${core_case_data.api.url}")
    private String ccdDataStoreUrl;

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
        log.info(
            "Trying to receive case for role modification. Submission Reference: {}",
            findCaseForRoleModificationRequest.getCaseSubmissionReference()
        );
        String adminUserToken = adminUserService.getAdminUserToken();
        String elasticSearchQuery = ElasticSearchQueryBuilder
            .buildElasticSearchQueryForRoleModification(findCaseForRoleModificationRequest);
        List<CaseDetails> englandCases = Optional.ofNullable(ccdApi.searchCases(
            adminUserToken,
            authTokenGenerator.generate(),
            ENGLAND_CASE_TYPE,
            elasticSearchQuery
        ).getCases()).orElse(Collections.emptyList());
        if (CollectionUtils.isNotEmpty(englandCases)) {
            return englandCases.get(FIRST_INDEX);
        }

        List<CaseDetails> scotlandCases = Optional.ofNullable(ccdApi.searchCases(
            adminUserToken,
            authTokenGenerator.generate(),
            SCOTLAND_CASE_TYPE,
            elasticSearchQuery
        ).getCases()).orElse(Collections.emptyList());
        if (CollectionUtils.isNotEmpty(scotlandCases)) {
            return scotlandCases.get(FIRST_INDEX);
        }
        log.info(
            "Case not found for the parameters, submission reference: {}",
            findCaseForRoleModificationRequest.getCaseSubmissionReference()
        );
        return null;
    }

    /**
     * Modifies user case roles by the given modification type. Gets case assignment user roles request which has
     * a list of case_users that contains case id, user id and case role to modify the case. For assigning a new role
     * to the case modification type should be Assignment, to revoke an existing role modification type should be
     * Revoke
     * @param caseAssignmentUserRolesRequest This is the list of case roles that should be Revoked or Assigned
     * @param modificationType this value could be Assignment or Revoke.
     * @throws IOException Exception when any problem occurs while calling case assignment api (/case-users)
     */
    public void modifyUserCaseRoles(CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest,
                                    String modificationType) throws IOException {
        HttpMethod httpMethod = getHttpMethodByModificationType(modificationType);
        if (ObjectUtils.isEmpty(httpMethod)) {
            throw new CaseRoleManagementException(new Exception(EXCEPTION_INVALID_MODIFICATION_TYPE));
        }
        if (ObjectUtils.isEmpty(caseAssignmentUserRolesRequest)
            || CollectionUtils.isEmpty(caseAssignmentUserRolesRequest.getCaseAssignmentUserRoles())) {
            throw new CaseRoleManagementException(new Exception(MODIFY_CASE_ROLE_EMPTY_REQUEST));
        }
        log.info(getModifyUserCaseRolesLog(caseAssignmentUserRolesRequest, modificationType, true));
        String userToken = adminUserService.getAdminUserToken();
        ResponseEntity<CaseAssignmentUserRolesResponse> response;
        try {
            HttpEntity<CaseAssignmentUserRolesRequest> requestEntity =
                new HttpEntity<>(caseAssignmentUserRolesRequest,
                                 buildHeaders(userToken, this.authTokenGenerator.generate()));
            response = restTemplate.exchange(ccdDataStoreUrl + "/case-users",
                                             httpMethod,
                                             requestEntity,
                                             CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException | IOException exception) {
            log.info("Error from CCD - {}", exception.getMessage() + StringUtils.CR + roleList);
            throw exception;
        }
        log.info("{}" + StringUtils.CR + "Response status code: {} Response status code value: {}",
                 getModifyUserCaseRolesLog(caseAssignmentUserRolesRequest, modificationType, false),
                 response.getStatusCode(),
                 response.getStatusCodeValue()
        );
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
        return (isPreModify ? MODIFY_CASE_ROLE_PRE_WORDING : MODIFY_CASE_ROLE_POST_WORDING)
            + "Modification type is: " + modificationType + StringUtils.CR
            + "Roles: " + roleList;

    }

    /**
     * It generates new CaseAssignmentUserRolesRequest that has the CaseUserRoles which has caseId, userId, role fields.
     * Reason to implement this method is, if userId is not received from the client, it automatically gets userId
     * from IDAM and sets that id to all CaseAssignmentUserRoles' userId fields.
     * @param authorisation Authorisation token to receive user information from IDAM.
     * @param caseAssignmentUserRolesRequest CaseAssignmentUserRolesRequest that contains CaseUserRoles received from
     *                                       client.
     * @return new CaseAssignmentUserRolesRequest that has userId which is received from IDAM.
     */
    public CaseAssignmentUserRolesRequest generateCaseAssignmentUserRolesRequestWithUserIds(
        String authorisation, CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest) {
        UserInfo userInfo = idamClient.getUserInfo(authorisation);
        List<CaseAssignmentUserRole> tmpCaseAssignmentUserRoles = new ArrayList<>();
        for (CaseAssignmentUserRole caseAssignmentUserRole :
            caseAssignmentUserRolesRequest.getCaseAssignmentUserRoles()) {
            CaseAssignmentUserRole tmpCaseAssignmentUserRole = CaseAssignmentUserRole.builder()
                .caseDataId(caseAssignmentUserRole.getCaseDataId())
                .userId(StringUtils.isBlank(caseAssignmentUserRole.getUserId())
                            ? userInfo.getUid()
                            : caseAssignmentUserRole.getUserId())
                .caseRole(caseAssignmentUserRole.getCaseRole())
                .build();
            tmpCaseAssignmentUserRoles.add(tmpCaseAssignmentUserRole);
        }
        return CaseAssignmentUserRolesRequest.builder().caseAssignmentUserRoles(tmpCaseAssignmentUserRoles).build();
    }
}
