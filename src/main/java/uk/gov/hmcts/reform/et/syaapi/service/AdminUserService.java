package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;

/**
 * Service responsible for obtaining and caching an admin user token used for API calls.
 *
 * <p>
 * This service retrieves an authentication token from the {@link IdamClient} using the configured
 * caseworker credentials. The token is cached to reduce the number of authentication requests
 * sent to IDAM. The cache is cleared at a fixed interval, ensuring that the token is periodically
 * refreshed in line with its time-to-live (TTL).
 * </p>
 *
 * <h2>Configuration</h2>
 * <ul>
 *   <li><b>caseWorkerUserName</b>: Username for the caseworker user.</li>
 *   <li><b>caseWorkerPassword</b>: Password for the caseworker user.</li>
 *   <li><b>caching.adminUserTokenTTL</b>: Interval in milliseconds for scheduled cache eviction.</li>
 * </ul>
 *
 * <h2>Caching</h2>
 * The admin user token is cached under the {@code adminUserToken} cache name. It is automatically
 * evicted according to the configured TTL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"adminUserToken"})
public class AdminUserService {
    /**
     * Constant prefix for bearer tokens.
     */
    public static final String BEARER = "Bearer";
    private final IdamClient idamClient;

    @Value("${caseWorkerUserName}")
    private String apiCallUserName;

    @Value("${caseWorkerPassword}")
    private String apiCallUserPassword;

    /**
     * Retrieves the admin user access token from IDAM.
     *
     * <p>
     * If the token does not already include the {@code Bearer} prefix, it is prepended before
     * returning. The result is cached to avoid repeated calls to the IDAM service.
     * </p>
     *
     * @return a valid admin user bearer token
     */
    @Cacheable("adminUserToken")
    public String getAdminUserToken() {
        String adminAccessToken = idamClient.getAccessToken(apiCallUserName, apiCallUserPassword);
        if (StringUtils.contains(adminAccessToken, BEARER)) {
            return adminAccessToken;
        }
        return String.join(" ", BEARER, adminAccessToken);
    }

    /**
     * Clears the cached admin user token at a fixed interval.
     *
     * <p>
     * This method runs according to the schedule defined by {@code caching.adminUserTokenTTL}.
     * It ensures that the cached token does not exceed its intended time-to-live (TTL).
     * </p>
     */
    @CacheEvict(value = "adminUserToken", allEntries = true)
    @Scheduled(fixedRateString = "${caching.adminUserTokenTTL}")
    public void emptyAdminUserToken() {
        log.info("emptying adminUserToken cache");
    }
}
