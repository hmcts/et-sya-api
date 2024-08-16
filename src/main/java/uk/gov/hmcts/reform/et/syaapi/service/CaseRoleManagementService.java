package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseRoleManagementException;

import java.io.IOException;

/**
 * Provides read and write access to cases stored by ET.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseRoleManagementService {

    private static final String MODIFY_CASE_ROLE_PRE_WORDING = "Received a request to modify roles:" + StringUtils.CR;
    private static final String MODIFY_CASE_ROLE_POST_WORDING = "Modified roles:" + StringUtils.CR;
    private static final String MODIFY_CASE_ROLE_EMPTY_REQUEST = "Request to modify roles is empty";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String HEADER_VALUE_APPLICATION_JSON = "application/json";
    private static final String AUTHORISATION_TOKEN_REGEX = "[a-zA-Z0-9._\\s\\S]+$";
    private static final String EXCEPTION_AUTHORISATION_TOKEN_REGEX = "authToken regex exception";
    private static final String EXCEPTION_INVALID_MODIFICATION_TYPE = "Invalid modification type";
    private static final String MODIFICATION_TYPE_ASSIGNMENT = "Assignment";
    private static final String MODIFICATION_TYPE_REVOKE = "Revoke";

    private String roleList;

    private final AdminUserService adminUserService;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;

    @Value("${core_case_data.api.url}")
    private String ccdDataStoreUrl;

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
                new HttpEntity<>(caseAssignmentUserRolesRequest, buildHeaders(userToken));
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

    private HttpMethod getHttpMethodByModificationType(String modificationType) {
        return MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType) ? HttpMethod.POST
            : MODIFICATION_TYPE_REVOKE.equals(modificationType) ? HttpMethod.DELETE
            : null;
    }

    private HttpHeaders buildHeaders(String authToken) throws IOException {
        if (authToken.matches(AUTHORISATION_TOKEN_REGEX)) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HEADER_AUTHORIZATION, authToken);
            headers.add(HEADER_SERVICE_AUTHORIZATION, this.authTokenGenerator.generate());
            headers.add(HEADER_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON);
            return headers;
        } else {
            throw new IOException(EXCEPTION_AUTHORISATION_TOKEN_REGEX);
        }
    }

}
