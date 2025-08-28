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

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"adminUserToken", "authorisationToken", "idamUserInfo"})
public class CachedIdamService {
    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;

    @Cacheable("adminUserToken")
    public String getAdminUserToken() {
        return adminUserService.getAdminUserToken();
    }

    @Cacheable("authorisationToken")
    public String getAuthorisationToken() {
        return authTokenGenerator.generate();
    }

    @Cacheable("idamUserInfo")
    public UserInfo getUserInfo(String token) {
        return idamClient.getUserInfo(token);
    }

    @CacheEvict(value = "adminUserToken", allEntries = true)
    @Scheduled(fixedRateString = "${caching.spring.idamTTL}")
    public void emptyAdminUserToken() {
        log.info("emptying adminUserToken cache");
    }

    @CacheEvict(value = "authorisationToken", allEntries = true)
    @Scheduled(fixedRateString = "${caching.spring.idamTTL}")
    public void emptyAuthorisationToken() {
        log.info("emptying authorisationToken cache");
    }

    @CacheEvict(value = "idamUserInfo", allEntries = true)
    @Scheduled(fixedRateString = "${caching.spring.idamTTL}")
    public void emptyUserInfo() {
        log.info("emptying idamUserInfo cache");
    }
}
