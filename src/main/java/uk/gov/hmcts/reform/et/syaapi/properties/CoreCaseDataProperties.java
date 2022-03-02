package uk.gov.hmcts.reform.et.syaapi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "core-case-data")
@Data
public class CoreCaseDataProperties {
    private IdamProperties.Api api = new IdamProperties.Api();
    private Search search = new Search();

    @Data
    private static class Search {
        Integer pageSize;
    }
}
