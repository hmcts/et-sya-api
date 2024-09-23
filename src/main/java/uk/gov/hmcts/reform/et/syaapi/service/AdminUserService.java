package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    public static final String BEARER = "Bearer";
    private final IdamClient idamClient;

    @Value("${caseWorkerUserName}")
    private String apiCallUserName;

    @Value("${caseWorkerPassword}")

    private String apiCallUserPassword;

    public String getAdminUserToken() {
        String adminAccessToken = idamClient.getAccessToken(apiCallUserName, apiCallUserPassword);
        if (StringUtils.contains(adminAccessToken, BEARER)) {
            return adminAccessToken;
        }
        return String.join(" ", BEARER, adminAccessToken);
    }
}
