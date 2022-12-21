package uk.gov.hmcts.reform.et.syaapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.RequestInterceptor;

/**
 * Configures the API to add intercepters for calls from external sources.
 */
@Component
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RequestInterceptor requestInterceptor;

    /**
     * This config excludes certain urls to the inteceptor.
     * these are endpoints that do not require authentication
     * @param registry {@link InterceptorRegistry} in which the exclusions for the inteceptor are added
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor)
            .excludePathPatterns(
                "/health",
                "/webjars/springfox-swagger-ui/**",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v2/**",
                "/favicon.ico",
                "/health",
                "/mappings",
                "/info",
                "/metrics",
                "/metrics/**",
                "/v3/**",
                "/"
            );
    }
}
