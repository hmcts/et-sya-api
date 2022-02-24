package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.reform.et.syaapi.config.PostcodeToOfficeLookup;
import uk.gov.hmcts.reform.et.syaapi.model.helper.TribunalOffice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostcodeToOfficeServiceTestv2 {

    @Mock
    private PostcodeToOfficeLookup config;

    @InjectMocks
    private PostcodeToOfficeService cut;

    private static final String INVALID_POSTCODE = "ABC123";
    private static final String VALID_POSTCODE ="EH3 7HF";

    @Test
    public void shouldNotBeNull() {
        assertNotNull(config);
        assertNotNull(cut);
    }

    @Test
    public void shouldThrowExceptionWhenPostCodeIsInvalid() {
        assertThrows(
            InvalidPostcodeException.class,
            () -> cut.getTribunalOfficeFromPostcode(INVALID_POSTCODE));
    }

//    @Test
//    public void shouldReturnCorrectOfficeWhenPostcodeIsValid() throws InvalidPostcodeException {
//
//        String outCode = "EH";
//        when(config.getPostcodes().containsKey(outCode)).thenReturn(true);
//        when(cut.getTribunalOffice(config.getPostcodes().get(outCode))).thenReturn(true);
//        System.out.println("post code to test is "+ VALID_POSTCODE);
//       TribunalOffice result = cut.getTribunalOfficeFromPostcode(VALID_POSTCODE);
//        System.out.println("office is " + result.getOfficeName());
////        String expected = TribunalOffice.EDINBURGH.getOfficeName();
////        System.out.println("expected is" + expected);
////        assertEquals(expected, result.getOfficeName());
//    }
}
