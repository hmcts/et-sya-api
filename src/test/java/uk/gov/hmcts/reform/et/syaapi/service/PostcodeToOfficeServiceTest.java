package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.reform.et.syaapi.config.PostcodeToOfficeLookup;
import uk.gov.hmcts.reform.et.syaapi.model.helper.TribunalOffice;


import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
@SpringBootTest(classes = {
    PostcodeToOfficeService.class,
})
@EnableConfigurationProperties({ PostcodeToOfficeLookup.class})

class PostcodeToOfficeServiceTest {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    PostcodeToOfficeService postcodeToOfficeService;

    private final static Object[][] TEST_CASES = new Object[][] {
        { "M3 2JA", TribunalOffice.MANCHESTER.getOfficeName()  },
        { "M3 2JA",TribunalOffice.MANCHESTER.getOfficeName() },
        { "M3 2JA",TribunalOffice.MANCHESTER.getOfficeName() },
        { "G2 8GT",TribunalOffice.GLASGOW.getOfficeName() },
        { "G2 8GT",TribunalOffice.GLASGOW.getOfficeName() },
        { "G2 8GT",TribunalOffice.GLASGOW.getOfficeName() },
        { "AB10 1SH",TribunalOffice.ABERDEEN.getOfficeName() },
        { "AB10 1SH",TribunalOffice.ABERDEEN.getOfficeName() },
        { "AB10 1SH",TribunalOffice.ABERDEEN.getOfficeName() },
        { "DD1 4QB",TribunalOffice.DUNDEE.getOfficeName() },
        { "DD1 4QB",TribunalOffice.DUNDEE.getOfficeName() },
        { "DD1 4QB",TribunalOffice.DUNDEE.getOfficeName() },
        { "EH3 7HF",TribunalOffice.EDINBURGH.getOfficeName() },
        { "EH3 7HF",TribunalOffice.EDINBURGH.getOfficeName() },
        { "EH3 7HF",TribunalOffice.EDINBURGH.getOfficeName() }
    };

    private final String postcode;
    private final String expectedOffice;

    public PostcodeToOfficeServiceTest(String postcode, String expectedOffice) {
        this.postcode = postcode;
        this.expectedOffice = expectedOffice;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(TEST_CASES);
    }

    @Test
    public void testGetsCorrectTribunalOfficeFromPostcode() throws InvalidPostcodeException {
        TribunalOffice tribunalOffice = postcodeToOfficeService.getTribunalOfficeFromPostcode(postcode);
        assertEquals(tribunalOffice.getOfficeName(), expectedOffice);
    }
}
