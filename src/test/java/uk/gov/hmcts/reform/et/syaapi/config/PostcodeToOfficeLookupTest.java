package uk.gov.hmcts.reform.et.syaapi.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
@EnableConfigurationProperties({PostcodeToOfficeMappings.class})
class PostcodeToOfficeLookupTest {

    private static final String LEEDS = "Leeds";
    private static final String MANCHESTER = "Manchester";
    private static final String GLASGOW = "Glasgow";
    private static final String EDINGBURGH = "Edinburgh";

    @Autowired
    private PostcodeToOfficeMappings postcodeToOfficeLookup;

    @Test
    void whenFactoryProvidedThenYamlPropertiesInjected() {
        assertThat(postcodeToOfficeLookup.getPostcodes()).hasSizeGreaterThan(50);
    }

    @Test
    void mapMatchesPostcodePartialsToLeedsOffice() {
        assertThat(postcodeToOfficeLookup.getPostcodes()).containsEntry("BD", LEEDS);
    }

    @Test
    void mapMatchesPostcodePartialsToManchesterOffice() {
        assertThat(postcodeToOfficeLookup.getPostcodes()).containsEntry("M", MANCHESTER);
    }

    @Test
    void mapMatchesPostcodePartialsToGlasgowOffice() {
        assertThat(postcodeToOfficeLookup.getPostcodes()).containsEntry("G", GLASGOW);
    }

    @Test
    void mapMatchesPostcodePartialsToEdinburghOffice() {
        assertThat(postcodeToOfficeLookup.getPostcodes()).containsEntry("EH", EDINGBURGH);
    }

    @Test
    void shouldReturnNullForKeysThatDoNotExist() {
        assertThat(postcodeToOfficeLookup.getPostcodes().get("ABC")).isNull();
    }
}
