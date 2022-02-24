package uk.gov.hmcts.reform.et.syaapi.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@ContextConfiguration
@EnableConfigurationProperties({PostcodeToOfficeLookup.class})
public class PostcodeToOfficeLookupTest {

    @Autowired
    private PostcodeToOfficeLookup postcodeToOfficeLookup;


    @Test
    public void whenFactoryProvidedThenYamlPropertiesInjected() {
        assertEquals(310, postcodeToOfficeLookup.getPostcodes().size());
        assertEquals("leeds", postcodeToOfficeLookup.getPostcodes().get("BD"));
    }
}
