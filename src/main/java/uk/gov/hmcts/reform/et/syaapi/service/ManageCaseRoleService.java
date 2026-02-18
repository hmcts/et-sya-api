package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignedUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.SearchCaseAssignedUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.exception.ProfessionalUserException;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.CaseAssignmentResponse;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;
import uk.gov.hmcts.reform.et.syaapi.search.ElasticSearchQueryBuilder;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ClaimantUtil;
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

import static uk.gov.hmcts.ecm.common.client.CcdClient.EXPERIMENTAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET_SYA_FRONTEND;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.JURISDICTION_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.NO;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CLAIMANT_SOLICITOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.REMOVE_OWN_REP_AS_CLAIMANT;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.REMOVE_OWN_REP_AS_RESPONDENT;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_ET3_FORM;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.RemoteServiceUtil.buildHeaders;

/**
 * Provides services for role modification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManageCaseRoleService {
    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final RestTemplate restTemplate;
    private final CoreCaseDataApi ccdApi;
    private final ET3Service et3Service;
    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final FeatureToggleService featureToggleService;

    @Value("${assign_case_access_api_url}")
    private String aacUrl;

    @Value("${core_case_data.api.url}")
    private String ccdApiUrl;

    /**
     * Fetches the user role assignments associated with a given case ID from the CCD Data Store API.
     *
     * <p>This method constructs the appropriate API endpoint URL, obtains an admin authorization token,
     * and sends a GET request to retrieve user assignments for the specified case. The response is deserialized
     * into a {@link uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData} object, which includes user-role mappings
     * for the case.</p>
     *
     * @param caseId the unique identifier of the case whose user assignments are to be retrieved
     * @return a {@link uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData} object containing the
     *      list of user assignments for the case
     * @throws java.io.IOException if there is a failure during URL construction or API communication
     */
    public CaseUserAssignmentData fetchCaseUserAssignmentsByCaseId(String caseId) throws IOException {
        String uri = ManageCaseRoleServiceUtil
            .buildCaseAccessUrl(ccdApiUrl, caseId);
        String authToken = adminUserService.getAdminUserToken();
        HttpHeaders httpHeaders = buildHeaders(authToken, authTokenGenerator.generate());
        httpHeaders.add(EXPERIMENTAL, "true");
        HttpEntity<String> request = new HttpEntity<>(httpHeaders);
        return restTemplate.exchange(uri, HttpMethod.GET, request, CaseUserAssignmentData.class).getBody();
    }

    /**
     * Gets case with the user entered details, caseId, respondentName, claimantFirstNames and claimantSurname.
     * Returns null when case not found. It searches for the cases with administrator user to find if the case
     * exists with the given parameters. Also makes a security check if the user entered valid values.
     * @param request It has the values caseId, respondentName, claimantFirstNames and
     *                                          claimantSurname values given by the respondent. Also contains the
     *                                          applicationName to determine which search logic to use.
     * @return null if no case is found, CaseDetails if any case is found in both scotland and england wales case
     *              types.
     */
    public CaseDetails findCaseForRoleModification(
        FindCaseForRoleModificationRequest request,
        String authorisation) throws IOException {
        log.info("Fetching case for role modification. Submission Reference: {}", request.getCaseSubmissionReference());

        String adminUserToken = adminUserService.getAdminUserToken();
        String elasticSearchQuery = ET_SYA_FRONTEND.equals(request.getApplicationName())
            ? ElasticSearchQueryBuilder.buildByFindCaseForRoleModificationRequestClaimant(request)
            : ElasticSearchQueryBuilder.buildByFindCaseForRoleModificationRequest(request);

        if (ET_SYA_FRONTEND.equals(request.getApplicationName())) {
            CaseDetails caseDetails = findCaseInSearchResults(adminUserToken, elasticSearchQuery, ENGLAND_CASE_TYPE);
            if (caseDetails != null) {
                return caseDetails;
            }
            return findCaseInSearchResults(adminUserToken, elasticSearchQuery, SCOTLAND_CASE_TYPE);
        }

        CaseDetails caseDetails = findCaseByCaseType(adminUserToken, ENGLAND_CASE_TYPE,
                                                     elasticSearchQuery, authorisation);
        if (caseDetails != null) {
            return caseDetails;
        }
        return findCaseByCaseType(adminUserToken, SCOTLAND_CASE_TYPE, elasticSearchQuery, authorisation);
    }

    private CaseDetails findCaseInSearchResults(String adminUserToken, String query, String caseType) {
        SearchResult searchResult = ccdApi.searchCases(adminUserToken, authTokenGenerator.generate(), caseType, query);
        if (ObjectUtils.isNotEmpty(searchResult) && CollectionUtils.isNotEmpty(searchResult.getCases())) {
            return searchResult.getCases().getFirst();
        }
        return null;
    }

    private CaseDetails findCaseByCaseType(String adminUserToken,
                                           String caseType,
                                           String elasticSearchQuery,
                                           String authorisation) throws IOException {
        List<CaseDetails> caseDetailsList = Optional.ofNullable(ccdApi.searchCases(
            adminUserToken,
            authTokenGenerator.generate(),
            caseType,
            elasticSearchQuery
        ).getCases()).orElse(Collections.emptyList());
        return checkIsUserCreator(authorisation, caseDetailsList)
            ? null
            : ManageCaseRoleServiceUtil.checkCaseDetailsList(caseDetailsList);
    }

    private boolean checkIsUserCreator(String authorisation, List<CaseDetails> caseDetailsList) throws IOException {
        return RespondentUtil
            .checkIsUserCreator(getCaseUserRolesByCaseAndUserIdsCcd(authorisation, caseDetailsList));
    }

    /**
     * Modifies user case roles by the given modification type. Gets case assignment user roles request which has
     * a list of case_users that contains case id, user id and case role to modify the case. For assigning a new role
     * to the case modification type should be Assignment, to revoke an existing role modification type should be
     * Revoke.
     * This is a wrapper method that checks the self-assignment feature flag and routes to the appropriate
     * implementation (old or new behavior).
     * @param authorisation Authorisation token of the user
     * @param modifyCaseUserRolesRequest This is the list of case roles that should be Revoked or Assigned to
     *                                   respondent. It also has the idam id of the respondent and the case id
     *                                   of the case that will be assigned
     * @param modificationType this value could be Assignment or Revoke.
     * @return CaseAssignmentResponse containing case details and assignment status.
     * @throws java.io.IOException Exception when any problem occurs while calling case assignment api (/case-users)
     */
    public CaseAssignmentResponse modifyUserCaseRoles(String authorisation,
                                                      ModifyCaseUserRolesRequest modifyCaseUserRolesRequest,
                                                      String modificationType)
        throws IOException {
        if (featureToggleService.isEt3SelfAssignmentEnabled()) {
            return modifyUserCaseRolesNew(authorisation, modifyCaseUserRolesRequest, modificationType);
        } else {
            return modifyUserCaseRolesOld(authorisation, modifyCaseUserRolesRequest, modificationType);
        }
    }

    /**
     * OLD BEHAVIOR: Original implementation before professional user detection and already-assigned status.
     * Used when self-assignment feature flag is OFF.
     */
    private CaseAssignmentResponse modifyUserCaseRolesOld(String authorisation,
                                                          ModifyCaseUserRolesRequest modifyCaseUserRolesRequest,
                                                          String modificationType)
        throws IOException {
        List<CaseDetails> caseDetailsList = new ArrayList<>();
        ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(modifyCaseUserRolesRequest);
        HttpMethod httpMethod = RemoteServiceUtil.getHttpMethodByCaseUserRoleModificationType(modificationType);

        if (ManageCaseRoleConstants.MODIFICATION_TYPE_REVOKE.equals(modificationType)) {
            caseDetailsList = updateAllRespondentsIdamIdAndDefaultLinkStatusesOld(authorisation,
                                                                               modifyCaseUserRolesRequest,
                                                                               modificationType);
        }

        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest =
            ManageCaseRoleServiceUtil.generateCaseAssignmentUserRolesRequestByModifyCaseUserRolesRequest(
                modifyCaseUserRolesRequest);
        log.info("assigning case");
        restCallToModifyUserCaseRolesOld(caseAssignmentUserRolesRequest, httpMethod);

        try {
            if (MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)) {
                caseDetailsList = updateAllRespondentsIdamIdAndDefaultLinkStatusesOld(authorisation,
                                                                                   modifyCaseUserRolesRequest,
                                                                                   modificationType);
            }
        } catch (Exception e) {
            if (!ManageCaseRoleServiceUtil.isCaseRoleAssignmentExceptionForSameUser(e)) {
                restCallToModifyUserCaseRolesOld(caseAssignmentUserRolesRequest, HttpMethod.DELETE);
            }
            throw new ManageCaseRoleException(e);
        }

        log.info("Case assignment successfully completed");
        return CaseAssignmentResponse.builder()
            .caseDetails(caseDetailsList)
            .status(CaseAssignmentResponse.AssignmentStatus.ASSIGNED)
            .build();
    }

    /**
     * NEW BEHAVIOR: Enhanced implementation with professional user detection and already-assigned status.
     * Used when self-assignment feature flag is ON.
     */
    private CaseAssignmentResponse modifyUserCaseRolesNew(String authorisation,
                                                         ModifyCaseUserRolesRequest modifyCaseUserRolesRequest,
                                                         String modificationType)
        throws IOException {
        // Checks modifyCaseUserRolesRequest parameter if it is empty or not and it's objects.
        // If there is any problem throws ManageCaseRoleException.
        ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(modifyCaseUserRolesRequest);
        // Gets httpMethod by modification type. If modification type is Assignment, method is POST, if Revoke, method
        // is DELETE. Null if modification type is different then Assignment and Revoke.
        HttpMethod httpMethod = RemoteServiceUtil.getHttpMethodByCaseUserRoleModificationType(modificationType);

        // If modification type revoke, removes idam id from Respondent. Because after revoking roles
        // from user, we will not be able to modify respondent data.
        if (ManageCaseRoleConstants.MODIFICATION_TYPE_REVOKE.equals(modificationType)) {
            List<CaseDetails> caseDetailsList = updateAllRespondentsIdamIdAndDefaultLinkStatuses(
                authorisation,
                modifyCaseUserRolesRequest,
                modificationType);

            CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest =
                ManageCaseRoleServiceUtil.generateCaseAssignmentUserRolesRequestByModifyCaseUserRolesRequest(
                    modifyCaseUserRolesRequest);
            log.info("Processing case revocation request");
            restCallToModifyUserCaseRoles(caseAssignmentUserRolesRequest, httpMethod);

            return CaseAssignmentResponse.builder()
                .caseDetails(caseDetailsList)
                .status(CaseAssignmentResponse.AssignmentStatus.ASSIGNED)
                .message("User role revoked successfully")
                .build();
        }

        // Handle assignment case
        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest =
            ManageCaseRoleServiceUtil.generateCaseAssignmentUserRolesRequestByModifyCaseUserRolesRequest(
                modifyCaseUserRolesRequest);
        log.info("Processing case assignment request");

        try {
            restCallToModifyUserCaseRoles(caseAssignmentUserRolesRequest, httpMethod);
        } catch (ProfessionalUserException e) {
            return CaseAssignmentResponse.builder()
                .status(CaseAssignmentResponse.AssignmentStatus.PROFESSIONAL_USER)
                .message(e.getMessage())
                .build();
        } catch (RestClientResponseException e) {
            log.error("RestClientResponseException during case assignment: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("IOException during case assignment: {}", e.getMessage());
            throw e;
        }

        // After assigning role, update respondent data with idam id, case details links statuses
        // and respondent hub links statuses.
        List<CaseDetails> caseDetailsList;
        boolean alreadyAssigned;
        try {
            List<CaseDetails> updatedCases = new ArrayList<>();
            boolean wasAlreadyAssigned = false;

            for (ModifyCaseUserRole modifyCaseUserRole : modifyCaseUserRolesRequest.getModifyCaseUserRoles()) {
                if (CASE_USER_ROLE_DEFENDANT.equals(modifyCaseUserRole.getCaseRole())) {
                    CaseDetails caseDetails =
                        et3Service.findCaseBySubmissionReference(modifyCaseUserRole.getCaseDataId());
                    boolean isAlreadyAssigned = RespondentUtil.setRespondentIdamIdAndDefaultLinkStatuses(
                        caseDetails,
                        modifyCaseUserRole.getRespondentName(),
                        modifyCaseUserRole.getUserId(),
                        modificationType,
                        idamClient.getUserInfo(authorisation)
                    );
                    wasAlreadyAssigned = wasAlreadyAssigned || isAlreadyAssigned;
                    updatedCases.add(
                        et3Service.updateSubmittedCaseWithCaseDetailsForCaseAssignment(authorisation,
                                                                                       caseDetails,
                                                                                       UPDATE_ET3_FORM));
                } else if (CASE_USER_ROLE_CREATOR.equals(modifyCaseUserRole.getCaseRole())) {
                    CaseDetails caseDetails =
                        ccdApi.getCase(authorisation, authTokenGenerator.generate(),
                                       modifyCaseUserRole.getCaseDataId());
                    boolean isAlreadyAssigned = ClaimantUtil.setClaimantIdamId(
                        caseDetails,
                        modifyCaseUserRole.getUserId(),
                        modificationType
                    );
                    wasAlreadyAssigned = wasAlreadyAssigned || isAlreadyAssigned;
                    updatedCases.add(
                        caseService.triggerEvent(authorisation,
                                                 caseDetails.getId().toString(),
                                                 CaseEvent.UPDATE_CASE_SUBMITTED,
                                                 caseDetails.getCaseTypeId(),
                                                 caseDetails.getData()));
                }
            }

            caseDetailsList = updatedCases;
            alreadyAssigned = wasAlreadyAssigned;
        } catch (Exception e) {
            // If unable to update existing respondent data with idamId, case details link statuses
            // and response hub links statuses after assigning user case role, revokes assigned role!....
            restCallToModifyUserCaseRoles(caseAssignmentUserRolesRequest, HttpMethod.DELETE);
            throw new ManageCaseRoleException(e);
        }

        if (alreadyAssigned) {
            log.info("User already assigned - returning existing case details");
            return CaseAssignmentResponse.builder()
                .caseDetails(caseDetailsList)
                .status(CaseAssignmentResponse.AssignmentStatus.ALREADY_ASSIGNED)
                .message("User was already assigned to this case")
                .build();
        }

        log.info("Case assignment successfully completed");
        return CaseAssignmentResponse.builder()
            .caseDetails(caseDetailsList)
            .status(CaseAssignmentResponse.AssignmentStatus.ASSIGNED)
            .message("User successfully assigned to case")
            .build();
    }

    private void restCallToModifyUserCaseRoles(
        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest, HttpMethod httpMethod)
        throws IOException {
        try {
            String adminToken = adminUserService.getAdminUserToken();
            HttpEntity<CaseAssignmentUserRolesRequest> requestEntity =
                new HttpEntity<>(caseAssignmentUserRolesRequest,
                                 buildHeaders(adminToken, authTokenGenerator.generate()));
            restTemplate.exchange(ccdApiUrl + ManageCaseRoleConstants.CASE_USERS_API_URL,
                                  httpMethod,
                                  requestEntity,
                                  CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException exception) {
            // Check if the error is due to a professional user (roleCategory: PROFESSIONAL)
            // The response contains double-escaped JSON (nested JSON in error message)
            String responseBody = exception.getResponseBodyAsString();
            if (responseBody != null && responseBody.contains("\\\"roleCategory\\\":\\\"PROFESSIONAL\\\"")) {
                log.info("Professional user detected - user will be redirected to use MyHMCTS instead.");
                throw new ProfessionalUserException(
                    "User is a professional user - user will be redirected to use MyHMCTS.");
            }
            log.error("Error from CCD - {}", exception.getMessage());
            throw exception;
        } catch (IOException exception) {
            log.error("Error from CCD - {}", exception.getMessage());
            throw exception;
        }
    }

    private List<CaseDetails> updateAllRespondentsIdamIdAndDefaultLinkStatuses(
        String authorisation, ModifyCaseUserRolesRequest modifyCaseUserRolesRequest, String modificationType) {
        List<CaseDetails> caseDetailsList = new ArrayList<>();
        for (ModifyCaseUserRole modifyCaseUserRole : modifyCaseUserRolesRequest.getModifyCaseUserRoles()) {
            if (CASE_USER_ROLE_DEFENDANT.equals(modifyCaseUserRole.getCaseRole())) {
                CaseDetails caseDetails =
                    et3Service.findCaseBySubmissionReference(modifyCaseUserRole.getCaseDataId());
                RespondentUtil.setRespondentIdamIdAndDefaultLinkStatuses(
                    caseDetails,
                    modifyCaseUserRole.getRespondentName(),
                    modifyCaseUserRole.getUserId(),
                    modificationType,
                    idamClient.getUserInfo(authorisation)
                );
                caseDetailsList.add(
                    et3Service.updateSubmittedCaseWithCaseDetailsForCaseAssignment(authorisation,
                                                                                   caseDetails,
                                                                                   UPDATE_ET3_FORM));
            } else if (CASE_USER_ROLE_CREATOR.equals(modifyCaseUserRole.getCaseRole())) {
                CaseDetails caseDetails =
                    ccdApi.getCase(authorisation, authTokenGenerator.generate(), modifyCaseUserRole.getCaseDataId());
                ClaimantUtil.setClaimantIdamId(
                    caseDetails,
                    modifyCaseUserRole.getUserId(),
                    modificationType
                );
                caseDetailsList.add(
                    caseService.triggerEvent(authorisation,
                                             caseDetails.getId().toString(),
                                             CaseEvent.UPDATE_CASE_SUBMITTED,
                                             caseDetails.getCaseTypeId(),
                                             caseDetails.getData()));
            }
        }
        return caseDetailsList;
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
                .respondentName(modifyCaseUserRole.getRespondentName())
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
     * @throws java.io.IOException throws when any error occurs while receiving case user roles.
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
                new HttpEntity<>(buildHeaders(authorization, authTokenGenerator.generate()));
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
                                 buildHeaders(authorization, authTokenGenerator.generate()));
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
     * With given caseId, gets the case details, by case user role and returns case details by filtering documents
     * with the given caseUserRole.
     *
     * @param authorization is used to get the {@link uk.gov.hmcts.reform.idam.client.models.UserInfo} for the request
     * @return the associated {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} for the ID provided
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
                new Exception("Unable to find user case by case id: " + caseId));
        }
        List<CaseDetails> caseDetailsListByCaseUserRole =
            getCasesByCaseDetailsListAuthorizationAndCaseUserRole(List.of(caseDetails), authorization, caseUserRole);
        return CollectionUtils.isNotEmpty(caseDetailsListByCaseUserRole)
            ? caseDetailsListByCaseUserRole.getFirst()
            : null;
    }

    /**
     * Given a user derived from the authorisation token in the request,
     * gets all cases {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} for that user and filters case documents.
     *
     * @param authorization is used to get the {@link uk.gov.hmcts.reform.idam.client.models.UserInfo} for the request
     * @return the associated {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} list for the authorization code
     *      of the user provided
     */
    // @Retryable({FeignException.class, RuntimeException.class}) --> No need to give exception classes as Retryable
    // covers all runtime exceptions.
    @Retryable
    public List<CaseDetails> getUserCasesByCaseUserRole(String authorization, String caseUserRole) {
        return getCasesByCaseDetailsListAuthorizationAndCaseUserRole(getCaseDetailsByCaseUserRole(authorization,
                                                                                                  caseUserRole),
                                                                     authorization,
                                                                     caseUserRole);
    }

    private List<CaseDetails> getCaseDetailsByCaseUserRole(String authorization, String caseUserRole) {
        // If defendant uses ET3 cases search because case service's all case search doesn't list all cases
        // immediately after assigning a new case
        log.info("CASE USER ROLE VALUE ON getCaseDetailsByCaseUserRole: {}", caseUserRole);
        return caseService.getAllUserCases(authorization);
    }

    private List<CaseDetails> getCasesByCaseDetailsListAuthorizationAndCaseUserRole(
        List<CaseDetails> caseDetailsList, String authorization, String caseUserRole) {
        List<CaseDetails> caseDetailsListByRole;
        try {
            CaseAssignedUserRolesResponse caseAssignedUserRolesResponse =
                getCaseUserRolesByCaseAndUserIdsCcd(authorization, caseDetailsList);
            caseDetailsListByRole = ManageCaseRoleServiceUtil
                .getCaseDetailsByCaseUserRole(caseDetailsList,
                                              caseAssignedUserRolesResponse.getCaseAssignedUserRoles(),
                                              caseUserRole);
            DocumentUtil.filterCasesDocumentsByCaseUserRole(caseDetailsListByRole, caseUserRole);
        } catch (IOException e) {
            throw new ManageCaseRoleException(e);
        }
        return caseDetailsListByRole;
    }

    /**
     * Revokes the claimant solicitor role from a case identified by the given submission reference.
     *
     * <p>This method performs the following steps:
     * <ul>
     *     <li>Logs the intention to revoke the claimant solicitor role for the specified submission reference.</li>
     *     <li>Retrieves the {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} where the current user
     *     holds the "creator" role.</li>
     *     <li>If the case is found, revokes the "[CLAIMANT_SOLICITOR]" role assigned to that case.</li>
     *     <li>Removes the claimant representative data from the case.</li>
     * </ul>
     * If the case is not found, a {@link uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException}
     *      is thrown.</p>
     *
     * @param authorisation the authorization token of the user performing the operation
     * @param caseSubmissionReference the unique reference used to locate the case
     * @return the updated {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} object with the claimant
     *      representative removed
     * @throws java.io.IOException if there is an error during retrieval or revocation of user roles
     * @throws uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException if no matching case is found
     *      for the provided reference
     */
    public CaseDetails revokeClaimantSolicitorRole(String authorisation, String caseSubmissionReference)
        throws IOException {
        log.info("Revoke claimant solicitor role for case submission reference: {}", caseSubmissionReference);
        CaseDetails caseDetails =
            getUserCaseByCaseUserRole(authorisation, caseSubmissionReference, CASE_USER_ROLE_CREATOR);
        if (ObjectUtils.isEmpty(caseDetails)) {
            throw new ManageCaseRoleException(new Exception(String.format(
                ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND, caseSubmissionReference)));
        }
        revokeCaseUserRole(caseDetails, CASE_USER_ROLE_CLAIMANT_SOLICITOR);
        return removeClaimantRepresentativeFromCaseData(authorisation, caseDetails);
    }

    /**
     * Revokes the case user role associated with a respondent solicitor and removes their representative
     * from the case data for the specified case.
     *
     * <p>
     * The method performs the following operations:
     * <ul>
     *   <li>Determines the case user role label for the given respondent index.</li>
     *   <li>Retrieves the {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} where that role is
     *      currently assigned using the provided authorisation
     *      token.</li>
     *   <li>Validates that the case was found and not already unlinked.</li>
     *   <li>Removes the representative associated with the respondent from the case data.</li>
     *   <li>Revokes the case user role for the respondent solicitor.</li>
     * </ul>
     * </p>
     *
     * @param authorisation           the authorisation token to authenticate the request
     * @param caseSubmissionReference the unique submission reference of the case
     * @param respondentIndex         the index (as a string) identifying which respondent's role is to be revoked
     * @return the updated {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} after the representative
     *      has been removed and the role revoked
     * @throws uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException    if case details could not be found or
     *      role revocation fails
     */
    public CaseDetails revokeRespondentSolicitorRole(String authorisation,
                                                     String caseSubmissionReference,
                                                     String respondentIndex) {
        log.info("Revoke respondent solicitor role for case submission reference: {}", caseSubmissionReference);
        CaseDetails caseDetails =
            getUserCaseByCaseUserRole(authorisation, caseSubmissionReference, CASE_USER_ROLE_DEFENDANT);
        if (ObjectUtils.isEmpty(caseDetails) || caseDetails.getId() == null) {
            throw new ManageCaseRoleException(new Exception(String.format(
                ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND, caseSubmissionReference)));
        }
        String caseUserRole = null;
        try {
            caseUserRole = ManageCaseRoleServiceUtil.getRespondentSolicitorType(
                caseDetails,
                respondentIndex
            ).getLabel();
        } catch (ManageCaseRoleException e) {
            log.info("Case user role not found for respondent index: {}, in case with submission reference: {}. "
                         + "This maybe because respondent representative does not have any organisation defined in "
                         + "ref data.",
                     respondentIndex, caseSubmissionReference);
        }
        if (StringUtils.isNotBlank(caseUserRole)) {
            try {
                revokeCaseUserRole(caseDetails, caseUserRole);
            } catch (IOException | ManageCaseRoleException e) {
                log.info(
                    "No case user role revoked for respondent index: {}, in case with submission reference: {}. "
                        + "This maybe because respondent representative does not have any organisation defined in "
                        + "ref data.",
                    respondentIndex, caseSubmissionReference
                );
            }
        }
        return removeRespondentRepresentativeFromCaseData(authorisation,
                                                          caseDetails,
                                                          respondentIndex,
                                                          caseUserRole);
    }

    /**
     * Revokes a specific user role from a case by removing the user's assignment for the given role.
     *
     * <p>This method locates the user assigned to the specified case role within the provided
     * {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails}. If the user assignment is found, it constructs a
     *      request to revoke the user's role and invokes an external API call to perform the revocation. If the role
     *      assignment is not found, a {@link uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException}
     *      is thrown.</p>
     *
     * @param caseDetails the case from which the user role should be revoked
     * @param role the case role to revoke (e.g., "[CLAIMANT_SOLICITOR]")
     * @throws java.io.IOException if an error occurs while retrieving user assignments or during the HTTP call
     * @throws uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException if no matching user-role assignment is
     *      found for the given case and role
     */
    public void revokeCaseUserRole(CaseDetails caseDetails, String role) throws IOException {
        List<CaseUserAssignment> caseUserAssignments = findCaseUserAssignmentsByRoleAndCase(
            role, caseDetails);
        if (CollectionUtils.isEmpty(caseUserAssignments)) {
            log.info("No case user assignment found for case {}, role: {}", caseDetails.getId(), role);
            return;
        }
        for (CaseUserAssignment caseUserAssignment : caseUserAssignments) {
            CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest = ManageCaseRoleServiceUtil
                .createCaseUserRoleRequest(
                    caseUserAssignment.getUserId(),
                    caseDetails,
                    role
                );
            HttpMethod httpMethod = RemoteServiceUtil.getHttpMethodByCaseUserRoleModificationType(
                ManageCaseRoleConstants.MODIFICATION_TYPE_REVOKE);
            restCallToModifyUserCaseRoles(caseAssignmentUserRolesRequest, httpMethod);
        }
    }

    /**
     * Finds a {@link uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment} that matches the specified case role within
     * the given case.
     *
     * <p>This method retrieves all user role assignments associated with the provided case ID
     * and searches for one that matches the specified case role. If no assignments are found
     * for the case or the response is empty, a
     *      {@link uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException} is thrown.
     * If no matching role is found among the assignments, {@code null} is returned.</p>
     *
     * @param caseRole the role to match (e.g., "[APPLICANT]", "[RESPONDENT]")
     * @param caseDetails the case from which to retrieve user assignments
     * @return the matching {@link uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment}, or {@code null} if no match
     *      is found
     * @throws java.io.IOException if an error occurs while retrieving user assignments
     * @throws uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException if no user assignments are found for
     *      the case
     */
    public List<CaseUserAssignment> findCaseUserAssignmentsByRoleAndCase(String caseRole, CaseDetails caseDetails)
        throws IOException {
        CaseUserAssignmentData caseUserAssignmentData =
            fetchCaseUserAssignmentsByCaseId(caseDetails.getId().toString());
        if (ObjectUtils.isEmpty(caseUserAssignmentData)
            || CollectionUtils.isEmpty(caseUserAssignmentData.getCaseUserAssignments())) {
            throw new ManageCaseRoleException(new Exception(
                String.format(ManageCaseRoleConstants.EXCEPTION_CASE_USER_ROLES_NOT_FOUND, caseDetails.getId())));
        }
        List<CaseUserAssignment> selectedCaseUserAssignments = new ArrayList<>();
        for (CaseUserAssignment caseAssignedUserRole : caseUserAssignmentData.getCaseUserAssignments()) {
            if (caseRole.equals(caseAssignedUserRole.getCaseRole())) {
                selectedCaseUserAssignments.add(caseAssignedUserRole);
            }
        }
        return selectedCaseUserAssignments;
    }

    /**
     * Removes the claimant representative information from the provided case data.
     *
     * <p>This method updates the given {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} object by:
     * <ul>
     *     <li>Deserializing the case data into a {@link uk.gov.hmcts.et.common.model.ccd.CaseData} object.</li>
     *     <li>Setting the claimant represented question to {@code NO}.</li>
     *     <li>Clearing the {@code representativeClaimantType} field.</li>
     *     <li>Serializing the updated {@code CaseData} back into the case details map structure.</li>
     * </ul>
     * Finally, it calls the ET3 service to persist the updated case details, returning the updated
     * {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} object.</p>
     *
     * @param authorisation the authorization token of the user performing the update
     * @param caseDetails the {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} object containing the case
     *                    to update
     * @return the updated {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} with claimant representative
     *      fields removed
     */
    public CaseDetails removeClaimantRepresentativeFromCaseData(String authorisation, CaseDetails caseDetails) {
        UserInfo userInfo = idamClient.getUserInfo(authorisation);
        StartEventResponse startEventResponse = ccdApi.startEventForCitizen(
            authorisation,
            authTokenGenerator.generate(),
            userInfo.getUid(),
            EMPLOYMENT,
            caseDetails.getCaseTypeId(),
            caseDetails.getId().toString(),
            REMOVE_OWN_REP_AS_CLAIMANT.name()
        );
        caseDetails = startEventResponse.getCaseDetails();
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        caseData.setClaimantRepresentedQuestion(NO);
        caseData.setClaimantRepresentativeRemoved(YES);
        ManageCaseRoleServiceUtil.resetOrganizationPolicy(caseData,
                                                          CASE_USER_ROLE_CLAIMANT_SOLICITOR,
                                                          caseDetails.getId().toString());
        return caseService.submitUpdate(
            authorisation,
            caseDetails.getId().toString(),
            caseDetailsConverter.caseDataContent(startEventResponse, caseData),
            caseDetails.getCaseTypeId()
        );
    }

    /**
     * Removes the representative associated with a specific respondent from the
     * given {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} object,
     * resets the organisation policy for that respondent, and submits the updated case for assignment.
     *
     * <p>
     * This method performs the following steps:
     * <ul>
     *   <li>Converts the raw case data map into a {@link uk.gov.hmcts.et.common.model.ccd.CaseData} object.</li>
     *   <li>Finds the respondent in the collection based on the provided {@code respondentIndex}.</li>
     *   <li>Identifies and removes the representative linked to that respondent.</li>
     *   <li>Resets the organisation policy for the respondent using the given {@code caseUserRole}.</li>
     *   <li>Maps the updated {@code CaseData} back into the {@code CaseDetails} object.</li>
     *   <li>Submits the updated case using the {@code et3Service}.</li>
     * </ul>
     * </p>
     *
     * @param authorisation   the authentication token used for case assignment
     * @param caseDetails     the {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} object containing case
     *                        metadata and data
     * @param respondentIndex a string index representing the respondent whose representative is to be removed
     * @param caseUserRole    the case user role (e.g., "[SOLICITORA]") used to determine which policy to reset
     * @return the updated {@link uk.gov.hmcts.reform.ccd.client.model.CaseDetails} after removing the representative
     *      and updating the organisation policy
     */
    public CaseDetails removeRespondentRepresentativeFromCaseData(String authorisation,
                                                                  CaseDetails caseDetails,
                                                                  String respondentIndex,
                                                                  String caseUserRole) {
        UserInfo userInfo = idamClient.getUserInfo(authorisation);
        StartEventResponse startEventResponse = ccdApi.startEventForCitizen(
            authorisation,
            authTokenGenerator.generate(),
            userInfo.getUid(),
            EMPLOYMENT,
            caseDetails.getCaseTypeId(),
            caseDetails.getId().toString(),
            REMOVE_OWN_REP_AS_RESPONDENT.name()
        );
        caseDetails = startEventResponse.getCaseDetails();
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        RespondentSumTypeItem respondentSumTypeItem =
            RespondentUtil.findRespondentSumTypeItemByIndex(caseData.getRespondentCollection(),
                                                            respondentIndex,
                                                            caseDetails.getId().toString());
        if (StringUtils.isNotBlank(caseUserRole)) {
            ManageCaseRoleServiceUtil.resetOrganizationPolicy(caseData, caseUserRole, caseDetails.getId().toString());
        }
        respondentSumTypeItem.getValue().setRepresentativeRemoved(YES);
        RepresentedTypeRItem representativeRItem =
            RespondentUtil.findRespondentRepresentative(respondentSumTypeItem,
                                                        caseData.getRepCollection(),
                                                        caseDetails.getId().toString());
        if (ObjectUtils.isNotEmpty(representativeRItem)) {
            caseData.setRepCollectionToRemove(List.of(representativeRItem));
        }
        caseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        CaseDataContent caseDataContent =  caseDetailsConverter.caseDataContent(startEventResponse, caseData);
        return ccdApi.submitEventForCitizen(authorisation,
                                       authTokenGenerator.generate(),
                                       userInfo.getUid(),
                                       JURISDICTION_ID,
                                       caseDetails.getCaseTypeId(),
                                       caseDetails.getId().toString(),
                                       true,
                                       caseDataContent);
    }

    /**
     * OLD BEHAVIOR: Rest call without professional user detection.
     * Used when self-assignment feature flag is OFF.
     */
    private void restCallToModifyUserCaseRolesOld(
        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest, HttpMethod httpMethod)
        throws IOException {
        try {
            String adminToken = adminUserService.getAdminUserToken();
            HttpEntity<CaseAssignmentUserRolesRequest> requestEntity =
                new HttpEntity<>(caseAssignmentUserRolesRequest,
                                 buildHeaders(adminToken, authTokenGenerator.generate()));
            restTemplate.exchange(ccdApiUrl + ManageCaseRoleConstants.CASE_USERS_API_URL,
                                  httpMethod,
                                  requestEntity,
                                  CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException | IOException exception) {
            log.info("Error from CCD - {}", exception.getMessage());
            throw exception;
        }
    }

    /**
     * OLD BEHAVIOR: Update respondents without boolean return (throws exception for already assigned).
     * Used when self-assignment feature flag is OFF.
     */
    private List<CaseDetails> updateAllRespondentsIdamIdAndDefaultLinkStatusesOld(
        String authorisation, ModifyCaseUserRolesRequest modifyCaseUserRolesRequest, String modificationType) {
        List<CaseDetails> caseDetailsList = new ArrayList<>();
        for (ModifyCaseUserRole modifyCaseUserRole : modifyCaseUserRolesRequest.getModifyCaseUserRoles()) {
            if (CASE_USER_ROLE_DEFENDANT.equals(modifyCaseUserRole.getCaseRole())) {
                CaseDetails caseDetails =
                    et3Service.findCaseBySubmissionReference(modifyCaseUserRole.getCaseDataId());
                // Old behavior: setRespondentIdamIdAndDefaultLinkStatuses throws exception for already assigned
                RespondentUtil.setRespondentIdamIdAndDefaultLinkStatuses(
                    caseDetails,
                    modifyCaseUserRole.getRespondentName(),
                    modifyCaseUserRole.getUserId(),
                    modificationType,
                    idamClient.getUserInfo(authorisation)
                );
                caseDetailsList.add(
                    et3Service.updateSubmittedCaseWithCaseDetailsForCaseAssignment(authorisation,
                                                                                   caseDetails,
                                                                                   UPDATE_ET3_FORM));
            }
        }
        return caseDetailsList;
    }
}
