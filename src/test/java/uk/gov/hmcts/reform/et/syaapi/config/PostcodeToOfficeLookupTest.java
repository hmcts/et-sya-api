package uk.gov.hmcts.reform.et.syaapi.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.configuration.PostcodeToOfficeMappings;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
@EnableConfigurationProperties({PostcodeToOfficeMappings.class})
class PostcodeToOfficeLookupTest {

    private static final String LEEDS = "Leeds";
    private static final String MANCHESTER = "Manchester";
    private static final String GLASGOW = "Glasgow";

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

    /**
     * Any Scotland postcode should return Glasgow office.
     */
    @Test
    void mapMatchesPostcodePartialsEdinburghToGlasgowOffice() {
        assertThat(postcodeToOfficeLookup.getPostcodes()).containsEntry("EH", GLASGOW);
    }

    @ParameterizedTest
    @ValueSource(strings = {"LA", "LA1", "LA23"})
    void mapMatchesLaPostcodeToManchester(String postcode) {
        assertThat(postcodeToOfficeLookup.getPostcodes()).containsEntry(postcode, MANCHESTER);
    }

    @Test
    void shouldReturnNullForKeysThatDoNotExist() {
        assertThat(postcodeToOfficeLookup.getPostcodes().get("ABC")).isNull();
    }
}
