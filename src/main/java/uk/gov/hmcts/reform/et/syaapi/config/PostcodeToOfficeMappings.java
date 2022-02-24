package uk.gov.hmcts.reform.et.syaapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a mappings class providing the mappings for all postcodes to appropriate tribunal offices.  Simply first past
 * of postcode to the term of the office known by.  E.g.
 *
 * <pre>
 *     RG10 - Reading
 *     CW7 - Manchester
 *     etc...
 * </pre>
 */
@Configuration
@ConfigurationProperties("tribunal-offices")
@PropertySource(value = "classpath:defaults.yml", factory = YamlPropertySourceFactory.class)
public class PostcodeToOfficeMappings {

    private final Map<String, String> postcodes = new HashMap<>();

    /**
     * Retrieves the map of postcode to office mappings
     *
     * @return a map of postcode to office mappings
     */
    public Map<String, String> getPostcodes() {
        return postcodes;
    }
}
