package uk.gov.hmcts.reform.et.syaapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties("tribunal-offices")
@PropertySource(value = "classpath:defaults.yml", factory = YamlPropertySourceFactory.class)
public class PostcodeToOfficeLookup {

    private final Map<String, String> postcodes = new HashMap<>();
    public Map<String, String> getPostcodes() {
        return postcodes;
    }

}
