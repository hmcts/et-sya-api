package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.reform.et.syaapi.config.PostcodeToOfficeMappings;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostcodeToOfficeServiceTest {

    private static final String INVALID_POSTCODE = "ABC123";
    private static final String EDINBURGH_POSTCODE_FIRST_PART = "EH";
    private static final String EDINBURGH_POSTCODE = EDINBURGH_POSTCODE_FIRST_PART + "3 7HF";
    private static final String UNKNOWN_POSTCODE = "BT9 6DJ";
    private static final String PETERBOROUGH_POSTCODE  = "PE11DP"; // Should return Watford
    private static final String SPALDING_POSTCODE = "PE111AE"; // Should return Midlands East
    private static final String EDINBURGH = "Edinburgh";
    private static final String MIDLANDS_EAST = "Midlands East";
    private static final String WATFORD = "Watford";


    @Mock
    private PostcodeToOfficeMappings mockPostcodeToOfficeMappings;

    @InjectMocks
    private PostcodeToOfficeService postcodeToOfficeService;

    @Test
    void shouldNotBeNull() {
        assertThat(mockPostcodeToOfficeMappings).isNotNull();
        assertThat(postcodeToOfficeService).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenPostCodeIsInvalid() {
        assertThrows(
            InvalidPostcodeException.class,
            () -> postcodeToOfficeService.getTribunalOfficeFromPostcode(INVALID_POSTCODE)
        );
    }

    @Test
    void shouldReturnCorrectOfficeWhenPostcodeIsValid() throws InvalidPostcodeException {

        Map<String, String> mockData = Map.of(EDINBURGH_POSTCODE_FIRST_PART, EDINBURGH);
        given(mockPostcodeToOfficeMappings.getPostcodes()).willReturn(mockData);

        Optional<TribunalOffice> result = postcodeToOfficeService.getTribunalOfficeFromPostcode(EDINBURGH_POSTCODE);
        assertThat(result).contains(TribunalOffice.EDINBURGH);
    }

    @Test
    void shouldReturnUnknownOfficeWhenPostcodeIsValidButNotKnown() throws InvalidPostcodeException {

        Map<String, String> mockData = Collections.emptyMap();
        given(mockPostcodeToOfficeMappings.getPostcodes()).willReturn(mockData);

        Optional<TribunalOffice> result = postcodeToOfficeService.getTribunalOfficeFromPostcode(UNKNOWN_POSTCODE);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldDistinguishBetweenOutcodeAndAreaCode() throws InvalidPostcodeException {

        Map<String, String> mockData = Map.of("PE11", MIDLANDS_EAST,
                                              "PE",WATFORD);
        given(mockPostcodeToOfficeMappings.getPostcodes()).willReturn(mockData);
        Optional<TribunalOffice> result = postcodeToOfficeService.getTribunalOfficeFromPostcode(SPALDING_POSTCODE);
        assertThat(result).contains(TribunalOffice.MIDLANDS_EAST);
    }

    @Test
    void shouldDistinguishBetweenOutcodeAndAreaCode2() throws InvalidPostcodeException {

        Map<String, String> mockData = Map.of("PE11", MIDLANDS_EAST,
                                              "PE",WATFORD);
        given(mockPostcodeToOfficeMappings.getPostcodes()).willReturn(mockData);
        Optional<TribunalOffice> result = postcodeToOfficeService.getTribunalOfficeFromPostcode(PETERBOROUGH_POSTCODE);
        assertThat(result).contains(TribunalOffice.WATFORD);
    }
}
