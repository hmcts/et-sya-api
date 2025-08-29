package uk.gov.hmcts.reform.et.syaapi.service;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserServiceTest {

    private static final String PARAM_CASEWORKER_USERNAME = "caseWorkerUserName=";
    private static final String CASEWORKER_USERNAME = "caseworker@hmcts.com";
    private static final String PARAM_CASEWORKER_PASSWORD = "caseWorkerPassword=";
    private static final String CASEWORKER_PASSWORD = "caseWorkerPassword";
    private static final String CONFIG_ADMIN_USER_TOKEN_TTL = "caching.adminUserTokenTTL=";
    private static final String ADMIN_USER_TTL = "60000";
    private static final String CACHE_NAME_ADMIN_USER_TOKEN = "adminUserToken";
    private static final String TOKEN_1 = "dummyToken1";
    private static final String TOKEN_2 = "dummyToken2";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withInitializer(ctx ->
                                 TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                                     ctx,
                                     PARAM_CASEWORKER_USERNAME + CASEWORKER_USERNAME,
                                     PARAM_CASEWORKER_PASSWORD + CASEWORKER_PASSWORD,
                                     CONFIG_ADMIN_USER_TOKEN_TTL + ADMIN_USER_TTL
                                 )
            );

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        IdamClient idamClient() {
            return Mockito.mock(IdamClient.class);
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(CACHE_NAME_ADMIN_USER_TOKEN);
        }

        @Bean
        AdminUserService adminUserService(IdamClient idamClient) {
            return new AdminUserService(idamClient);
        }
    }

    @Test
    void cacheShouldAvoidSecondCallAndEvictOnEmpty() {
        contextRunner.run(ctx -> {
            AdminUserService service = ctx.getBean(AdminUserService.class);
            IdamClient idam = ctx.getBean(IdamClient.class);

            when(idam.getAccessToken(CASEWORKER_USERNAME, CASEWORKER_PASSWORD))
                .thenReturn(TOKEN_1) // would be used for first call
                .thenReturn(TOKEN_2); // would be used only after eviction

            // 1) First call -> calls IDAM and caches return value
            String t1 = service.getAdminUserToken();
            assertThat(t1).isEqualTo(BEARER_PREFIX + TOKEN_1);
            verify(idam, times(NumberUtils.INTEGER_ONE)).getAccessToken(CASEWORKER_USERNAME, CASEWORKER_PASSWORD);

            // 2) Second call -> should come from cache; not calls IDAM
            String t2 = service.getAdminUserToken();
            assertThat(t2).isEqualTo(BEARER_PREFIX + TOKEN_1);
            verify(idam, times(NumberUtils.INTEGER_ONE)).getAccessToken(CASEWORKER_USERNAME, CASEWORKER_PASSWORD);

            // 3) Evict cache, then call again -> calls IDAM second time
            service.emptyAdminUserToken();
            String t3 = service.getAdminUserToken();
            assertThat(t3).isEqualTo(BEARER_PREFIX + TOKEN_2);
            verify(idam, times(NumberUtils.INTEGER_TWO)).getAccessToken(CASEWORKER_USERNAME, CASEWORKER_PASSWORD);
        });
    }
}
