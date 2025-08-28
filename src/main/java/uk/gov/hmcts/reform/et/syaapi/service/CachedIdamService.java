package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

/**
 * Service responsible for providing and caching IDAM (Identity and Access Management)
 * related tokens and user information. This class leverages Spring caching to reduce
 * the number of requests made to IDAM and authorization services by storing tokens
 * and user information for a configurable time-to-live (TTL).
 *
 * <p>Cached entities include:</p>
 * <ul>
 *   <li>Admin user token - retrieved from {@link AdminUserService}.</li>
 *   <li>Service authorisation token - generated using {@link AuthTokenGenerator}.</li>
 *   <li>User information - retrieved using {@link IdamClient}.</li>
 * </ul>
 *
 * <p>Cache eviction for all cached entities is performed periodically
 * according to the TTL defined in the application configuration
 * (property {@code caching.spring.idamTTL}).</p>
 *
 * <p>This ensures tokens and user information are regularly refreshed
 * and remain valid for downstream service calls.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"adminUserToken", "authorisationToken", "idamUserInfo"})
public class CachedIdamService {
    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;

    /**
     * Retrieves and caches the admin user token from the IDAM service.
     * Subsequent calls within the TTL will return the cached token.
     *
     * @return the admin user authentication token
     */
    @Cacheable("adminUserToken")
    public String getAdminUserToken() {
        return adminUserService.getAdminUserToken();
    }

    /**
     * Retrieves and caches the service-to-service authorisation token.
     * This token is used for authenticating communication between services.
     *
     * @return the generated authorisation token
     */
    @Cacheable("authorisationToken")
    public String getAuthorisationToken() {
        return authTokenGenerator.generate();
    }

    /**
     * Retrieves and caches the user information associated with the given token.
     * Subsequent requests for the same token within the TTL will return
     * the cached user information.
     *
     * @param token the IDAM access token of the user
     * @return the {@link UserInfo} corresponding to the provided token
     */
    @Cacheable("idamUserInfo")
    public UserInfo getUserInfo(String token) {
        return idamClient.getUserInfo(token);
    }

    /**
     * Clears the cached admin user token at fixed intervals.
     * This ensures that the token is refreshed periodically.
     */
    @CacheEvict(value = "adminUserToken", allEntries = true)
    @Scheduled(fixedRateString = "${caching.spring.idamTTL}")
    public void emptyAdminUserToken() {
        log.info("emptying adminUserToken cache");
    }

    /**
     * Clears the cached authorisation token at fixed intervals.
     * This ensures that the token is refreshed periodically.
     */
    @CacheEvict(value = "authorisationToken", allEntries = true)
    @Scheduled(fixedRateString = "${caching.spring.idamTTL}")
    public void emptyAuthorisationToken() {
        log.info("emptying authorisationToken cache");
    }

    /**
     * Clears the cached user information at fixed intervals.
     * This ensures that user information is refreshed periodically.
     */
    @CacheEvict(value = "idamUserInfo", allEntries = true)
    @Scheduled(fixedRateString = "${caching.spring.idamTTL}")
    public void emptyUserInfo() {
        log.info("emptying idamUserInfo cache");
    }
}
