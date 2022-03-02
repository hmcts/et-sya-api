package uk.gov.hmcts.reform.et.syaapi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "idam")
@Data
public class IdamProperties {
    private Api api = new Api();

    @Data
    public static class Api {
        private String url;
        private String jwksUrl;
    }
}
