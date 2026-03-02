package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

/**
 * Service for validating user roles from IDAM tokens.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleValidationService {

    private final IdamClient idamClient;

    /**
     * Validates if a user has any of the required roles.
     *
     * @param authorization the authorization token (Bearer token)
     * @param requiredRoles the list of roles, any of which the user must have
     * @return true if the user has at least one of the required roles, false otherwise
     */
    public boolean hasAnyRole(String authorization, List<String> requiredRoles) {
        try {
            UserInfo userInfo = idamClient.getUserInfo(authorization);
            List<String> userRoles = userInfo.getRoles();
            
            if (userRoles == null || userRoles.isEmpty()) {
                log.warn("User {} has no roles", userInfo.getUid());
                return false;
            }

            return userRoles.stream()
                .anyMatch(requiredRoles::contains);
        } catch (Exception e) {
            log.error("Error validating user roles", e);
            return false;
        }
    }

}
