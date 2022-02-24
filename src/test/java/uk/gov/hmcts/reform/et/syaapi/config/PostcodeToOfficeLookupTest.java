package uk.gov.hmcts.reform.et.syaapi.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.et.syaapi.model.helper.TribunalOffice;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
@EnableConfigurationProperties({PostcodeToOfficeMappings.class})
class PostcodeToOfficeLookupTest {

//    private final static Object[][] TEST_CASES = new Object[][] {
//        { "M3", TribunalOffice.MANCHESTER.getOfficeName()  },
//        { "G2", TribunalOffice.GLASGOW.getOfficeName() },
//        { "AB", TribunalOffice.ABERDEEN.getOfficeName() },
//        { "DD", TribunalOffice.DUNDEE.getOfficeName() },
//        { "EH", TribunalOffice.EDINBURGH.getOfficeName() }
//    };

    @Autowired
    private PostcodeToOfficeMappings postcodeToOfficeLookup;

    @Test
    void whenFactoryProvidedThenYamlPropertiesInjected() {
        assertThat(postcodeToOfficeLookup.getPostcodes().size()).isGreaterThan(50);
    }

    @Test
    void mapMatchesPostcodePartialsToLeedsOffice(){
        assertThat(postcodeToOfficeLookup.getPostcodes().get("BD")).isEqualTo(TribunalOffice.LEEDS.getOfficeName());
    }

    @Test
    void mapMatchesPostcodePartialsToManchesterOffice(){
        assertThat(postcodeToOfficeLookup.getPostcodes().get("M")).isEqualTo(TribunalOffice.MANCHESTER.getOfficeName());
    }

    @Test
    void mapMatchesPostcodePartialsToGlasgowOffice(){
        assertThat(postcodeToOfficeLookup.getPostcodes().get("G")).isEqualTo(TribunalOffice.SCOTLAND.getOfficeName());
    }

    @Test
    void shouldReturnNullForKeysThatDoNotExist(){
        assertThat(postcodeToOfficeLookup.getPostcodes().get("ABC")).isNull();
    }
}
