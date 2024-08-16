package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    public static final String BEARER = "Bearer";
    private final IdamClient idamClient;

    @Value("${etcos.system.username}")
    private String systemUserName;

    @Value("${etcos.system.password}")
    private String systemUserPassword;

    public String getAdminUserToken() {
        String adminAccessToken = idamClient.getAccessToken(systemUserName, systemUserPassword);
        if (StringUtils.contains(adminAccessToken, BEARER)) {
            return adminAccessToken;
        }
        return String.join(" ", BEARER, adminAccessToken);
    }

    public UserDetails getUserDetails(String userId) {
        return idamClient.getUserByUserId(getAdminUserToken(), userId);
    }
}
