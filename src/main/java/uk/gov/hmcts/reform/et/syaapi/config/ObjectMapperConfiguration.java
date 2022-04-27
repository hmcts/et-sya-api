package uk.gov.hmcts.reform.et.syaapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The configuration used to create a ObjectMapper for injection purposes.
 */
@Configuration
public class ObjectMapperConfiguration {

    /**
     * Gets the ObjectMapper bean to be used/injected into project code where needed.
     *
     * @return the ObjectMapper to be injected into project modules using it
     */
    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
}