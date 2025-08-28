package uk.gov.hmcts.reform.et.syaapi.service;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CachedIdamService}.
 */
@SpringJUnitConfig(classes = CachedIdamServiceTest.Config.class)
class CachedIdamServiceTest {

    @EnableCaching
    @Import(TestBeans.class)
    @TestConfiguration
    static class Config {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("adminUserToken", "authorisationToken", "idamUserInfo");
        }

        @Bean
        CachedIdamService cachedIdamService(AdminUserService adminUserService,
                                            AuthTokenGenerator authTokenGenerator,
                                            IdamClient idamClient) {
            return new CachedIdamService(adminUserService, authTokenGenerator, idamClient);
        }
    }

    @TestConfiguration
    static class TestBeans {
        @Bean AdminUserService adminUserService() {
            return Mockito.mock(AdminUserService.class);
        }

        @Bean AuthTokenGenerator authTokenGenerator() {
            return Mockito.mock(AuthTokenGenerator.class);
        }

        @Bean IdamClient idamClient() {
            return Mockito.mock(IdamClient.class);
        }
    }

    private final CachedIdamService cachedIdamService;
    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;

    private static final String ADMIN_USER_TOKEN_FIRST = "adminUserTokenFirst";
    private static final String ADMIN_USER_TOKEN_SECOND = "adminUserTokenSecond";
    private static final String AUTHORISATION_TOKEN_FIRST = "authorisationTokenFirst";
    private static final String AUTHORISATION_TOKEN_SECOND = "authorisationTokenSecond";
    private static final String FIRST_USER_TOKEN = "bearer firstUserToken";
    private static final String SECOND_USER_TOKEN = "bearer secondUserToken";
    private static final String EVICTION_USER_TOKEN = "bearer EvictionUserToken";

    @Autowired
    CachedIdamServiceTest(CachedIdamService cachedIdamService,
                          AdminUserService adminUserService,
                          AuthTokenGenerator authTokenGenerator,
                          IdamClient idamClient) {
        this.cachedIdamService = cachedIdamService;
        this.adminUserService = adminUserService;
        this.authTokenGenerator = authTokenGenerator;
        this.idamClient = idamClient;
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(adminUserService, authTokenGenerator, idamClient);
    }

    @Nested
    class AdminUserTokenCache {
        @Test
        void cachesAndEvictsAdminUserToken() {
            when(adminUserService.getAdminUserToken())
                .thenReturn(ADMIN_USER_TOKEN_FIRST)
                .thenReturn(ADMIN_USER_TOKEN_SECOND);

            String getAdminUserTokenFirstToken = cachedIdamService.getAdminUserToken();
            String getAdminUserTokenSecondToken = cachedIdamService.getAdminUserToken();

            assertThat(getAdminUserTokenFirstToken).isEqualTo(ADMIN_USER_TOKEN_FIRST);
            assertThat(getAdminUserTokenSecondToken).isEqualTo(ADMIN_USER_TOKEN_FIRST);
            verify(adminUserService, times(NumberUtils.INTEGER_ONE)).getAdminUserToken();

            cachedIdamService.emptyAdminUserToken();
            String getAdminUserTokenThirdToken = cachedIdamService.getAdminUserToken();
            assertThat(getAdminUserTokenThirdToken).isEqualTo(ADMIN_USER_TOKEN_SECOND);
            verify(adminUserService, times(NumberUtils.INTEGER_TWO)).getAdminUserToken();
        }
    }

    @Nested
    class AuthorisationTokenCache {
        @Test
        void cachesAndEvictsAuthorisationToken() {
            when(authTokenGenerator.generate())
                .thenReturn(AUTHORISATION_TOKEN_FIRST)
                .thenReturn(AUTHORISATION_TOKEN_SECOND);

            String getAuthorisationTokenFirst = cachedIdamService.getAuthorisationToken();
            String getAuthorisationTokenSecond = cachedIdamService.getAuthorisationToken();

            assertThat(getAuthorisationTokenFirst).isEqualTo(AUTHORISATION_TOKEN_FIRST);
            assertThat(getAuthorisationTokenSecond).isEqualTo(AUTHORISATION_TOKEN_FIRST);
            verify(authTokenGenerator, times(NumberUtils.INTEGER_ONE)).generate();

            cachedIdamService.emptyAuthorisationToken();
            String getAuthorisationTokenThird = cachedIdamService.getAuthorisationToken();
            assertThat(getAuthorisationTokenThird).isEqualTo(AUTHORISATION_TOKEN_SECOND);
            verify(authTokenGenerator, times(NumberUtils.INTEGER_TWO)).generate();
        }
    }

    @Nested
    class UserInfoCache {
        @Test
        void cachesPerToken() {
            UserInfo userInfoForFirstToken = mock(UserInfo.class);
            UserInfo userInfoForSecondToken = mock(UserInfo.class);

            when(idamClient.getUserInfo(FIRST_USER_TOKEN)).thenReturn(userInfoForFirstToken);
            when(idamClient.getUserInfo(SECOND_USER_TOKEN)).thenReturn(userInfoForSecondToken);

            UserInfo a1 = cachedIdamService.getUserInfo(FIRST_USER_TOKEN);
            UserInfo b1 = cachedIdamService.getUserInfo(SECOND_USER_TOKEN);
            UserInfo a2 = cachedIdamService.getUserInfo(FIRST_USER_TOKEN);
            UserInfo b2 = cachedIdamService.getUserInfo(SECOND_USER_TOKEN);

            assertThat(a1).isSameAs(userInfoForFirstToken);
            assertThat(a2).isSameAs(userInfoForFirstToken);
            assertThat(b1).isSameAs(userInfoForSecondToken);
            assertThat(b2).isSameAs(userInfoForSecondToken);

            verify(idamClient, times(NumberUtils.INTEGER_ONE)).getUserInfo(FIRST_USER_TOKEN);
            verify(idamClient, times(NumberUtils.INTEGER_ONE)).getUserInfo(SECOND_USER_TOKEN);
        }

        @Test
        void evictsUserInfoCache() {
            UserInfo userInfoBeforeEviction1 = mock(UserInfo.class);
            UserInfo userInfoBeforeEviction2 = mock(UserInfo.class);

            when(idamClient.getUserInfo(EVICTION_USER_TOKEN))
                .thenReturn(userInfoBeforeEviction1)
                .thenReturn(userInfoBeforeEviction2);

            UserInfo first = cachedIdamService.getUserInfo(EVICTION_USER_TOKEN);
            UserInfo second = cachedIdamService.getUserInfo(EVICTION_USER_TOKEN);
            assertThat(first).isSameAs(userInfoBeforeEviction1);
            assertThat(second).isSameAs(userInfoBeforeEviction1);
            verify(idamClient, times(NumberUtils.INTEGER_ONE)).getUserInfo(EVICTION_USER_TOKEN);

            cachedIdamService.emptyUserInfo();
            UserInfo userInfoAfterEviction = cachedIdamService.getUserInfo(EVICTION_USER_TOKEN);
            assertThat(userInfoAfterEviction).isSameAs(userInfoBeforeEviction2);
            verify(idamClient, times(NumberUtils.INTEGER_TWO)).getUserInfo(EVICTION_USER_TOKEN);
        }
    }
}
