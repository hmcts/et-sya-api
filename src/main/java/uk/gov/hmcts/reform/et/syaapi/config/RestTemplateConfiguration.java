package uk.gov.hmcts.reform.et.syaapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * The configuration used to create a RestTemplate for injection purposes.
 */
@Configuration
public class RestTemplateConfiguration {

    /**
     * Gets the RestTemplate bean to be used/injected into project code where needed.
     *
     * @return the RestTemplate to be injected into project modules using it.
     */
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
