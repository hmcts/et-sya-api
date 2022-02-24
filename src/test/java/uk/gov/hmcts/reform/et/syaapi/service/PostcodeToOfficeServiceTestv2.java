package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.reform.et.syaapi.config.PostcodeToOfficeMappings;
import uk.gov.hmcts.reform.et.syaapi.model.helper.TribunalOffice;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostcodeToOfficeServiceTestv2 {

    private static final String INVALID_POSTCODE = "ABC123";
    private static final String EDINBURGH_POSTCODE_FIRST_PART = "EH";
    private static final String EDINBURGH_POSTCODE = EDINBURGH_POSTCODE_FIRST_PART + "3 7HF";
    private static final String UNKNOWN_POSTCODE = "BT9 6DJ";
    private static final String PETERBOROUGH_POSTCODE  = "PE11DP"; // Should return Watford
    private static final String SPALDING_POSTCODE = "PE111AE"; // Should return Midlands East

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

        Map<String, String> mockData = Map.of(EDINBURGH_POSTCODE_FIRST_PART, TribunalOffice.EDINBURGH.getOfficeName());
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

        Map<String, String> mockData = Map.of("PE11", TribunalOffice.MIDLANDS_EAST.getOfficeName(),"PE",TribunalOffice.WATFORD.getOfficeName());
        given(mockPostcodeToOfficeMappings.getPostcodes()).willReturn(mockData);
        Optional<TribunalOffice> result = postcodeToOfficeService.getTribunalOfficeFromPostcode(SPALDING_POSTCODE);
        assertThat(result).contains(TribunalOffice.MIDLANDS_EAST);
    }

    @Test
    void shouldDistinguishBetweenOutcodeAndAreaCode2() throws InvalidPostcodeException {

        Map<String, String> mockData = Map.of("PE11", TribunalOffice.MIDLANDS_EAST.getOfficeName(),"PE",TribunalOffice.WATFORD.getOfficeName());
        given(mockPostcodeToOfficeMappings.getPostcodes()).willReturn(mockData);
        Optional<TribunalOffice> result = postcodeToOfficeService.getTribunalOfficeFromPostcode(PETERBOROUGH_POSTCODE);
        assertThat(result).contains(TribunalOffice.WATFORD);
    }
}
