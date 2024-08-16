package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.List;

/**
 * Provides read and write access to cases stored by ET.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseRoleManagementService {

    private final IdamClient idamClient;
    private final AdminUserService adminUserService;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;

    @Value("${core_case_data.api.url}")
    private String ccdDataStoreUrl;

    public void assignDefendant(String authorization, CaseRequest caseRequest) throws IOException {
        log.info("Adding defendant role to case {}", caseRequest.getCaseId());
        UserInfo respondentUser = idamClient.getUserInfo(authorization);
        CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole.builder()
            .caseDataId(caseRequest.getCaseId())
            .userId(respondentUser.getUid())
            .caseRole("[DEFENDANT]")
            .build();
        modifyCaseUserRoles(List.of(caseAssignmentUserRole), HttpMethod.POST);
        log.info("Successfully added [DEFENDANT] role to case {} ", caseRequest.getCaseId());
    }

    public void revokeDefendant(String authorization, CaseRequest caseRequest) throws IOException {
        log.info("Removing defendant role to case {}", caseRequest.getCaseId());
        UserInfo respondentUser = idamClient.getUserInfo(authorization);
        CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole.builder()
            .caseDataId(caseRequest.getCaseId())
            .userId(respondentUser.getUid())
            .caseRole("[DEFENDANT]")
            .build();
        modifyCaseUserRoles(List.of(caseAssignmentUserRole), HttpMethod.DELETE);
        log.info("Successfully removed [DEFENDANT] role from case {} ", caseRequest.getCaseId());
    }

    private void modifyCaseUserRoles(List<CaseAssignmentUserRole> caseAssignmentUserRoles, HttpMethod httpMethod)
        throws IOException {
        String userToken = adminUserService.getAdminUserToken();
        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRoles(caseAssignmentUserRoles)
            .build();
        HttpEntity<CaseAssignmentUserRolesRequest> requestEntity =
            new HttpEntity<>(caseAssignmentUserRolesRequest, buildHeaders(userToken));
        ResponseEntity<CaseAssignmentUserRolesResponse> response;
        try {
            response = restTemplate.exchange(
                ccdDataStoreUrl + "/case-users",
                httpMethod,
                requestEntity,
                CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException exception) {
            log.info("Error from CCD - {}", exception.getMessage());
            throw exception;
        }

        log.info("Add case user roles. Http status received from CCD API; {}",
                 response.getStatusCodeValue());
    }

    private HttpHeaders buildHeaders(String authToken) throws IOException {
        if (authToken.matches("[a-zA-Z0-9._\\s\\S]+$")) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", authToken);
            headers.add("ServiceAuthorization", this.authTokenGenerator.generate());
            headers.add("Content-Type", "application/json");
            return headers;
        } else {
            throw new IOException("authToken regex exception");
        }
    }

}
