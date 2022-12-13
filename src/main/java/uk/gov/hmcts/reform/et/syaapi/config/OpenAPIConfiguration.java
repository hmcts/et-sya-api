package uk.gov.hmcts.reform.et.syaapi.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.REMOTE_REPO;

/**
 * Configures the settings for OpenAPI for the project.
 */
@Configuration
public class OpenAPIConfiguration {

    /**
     * Configures the settings for OpenAPI for the project.
     * @return {@link OpenAPI} object with project specific information
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info().title("et-sya-api")
                      .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
            .externalDocs(new ExternalDocumentation()
                              .description("README")
                              .url(REMOTE_REPO));
    }

}
