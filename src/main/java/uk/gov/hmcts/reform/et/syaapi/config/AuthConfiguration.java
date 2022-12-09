package uk.gov.hmcts.reform.et.syaapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

/**
 * sets the configuration for {@link AuthTokenGeneratorFactory}.
 * This relies upon the following configurations to be set at an environment level:
 * <ul>
 *     <li>IDAM_S2S_AUTH_SECRET</li>
 *     <li>MICRO_SERVICE</li>
 * </ul>
 */
@Configuration
@Lazy
public class AuthConfiguration {

    /**
     * Creates a new auth token generator with the relevent attributes for sending request from this service.
     * @param secret service to service secret api key
     * @param microService name of the service using this generator
     * @param serviceAuthorisationApi API used to manage the created token
     * @return {@link AuthTokenGenerator} used to generate jwt for service calls
     */
    @Bean
    public AuthTokenGenerator serviceAuthTokenGenerator(
        @Value("${idam.s2s-auth.secret}") final String secret,
        @Value("${idam.s2s-auth.microservice}") final String microService,
        final ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, microService, serviceAuthorisationApi);
    }

    /**
     * Returns validator used to validate incoming jwts.
     * @param s2sApi authorization api for the jwt
     * @return {@link AuthTokenValidator} to validate the jwt
     */
    @Bean
    public AuthTokenValidator tokenValidator(ServiceAuthorisationApi s2sApi) {
        return new ServiceAuthTokenValidator(s2sApi);
    }
}
