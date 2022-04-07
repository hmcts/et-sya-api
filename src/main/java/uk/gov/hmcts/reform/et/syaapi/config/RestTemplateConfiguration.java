package uk.gov.hmcts.reform.et.syaapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * todo - The configuration of the
 */
@Configuration
public class RestTemplateConfiguration {

    /**
     * todo -
     * @return
     */
    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
