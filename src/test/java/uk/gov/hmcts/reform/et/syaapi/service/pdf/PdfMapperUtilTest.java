package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperUtil.formatUkPostcode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PdfMapperUtilTest {

    private static final String ADDRESS_LINE1 = "CO-OPERATIVE RETAIL SERVICES LTD, 11, MERRION WAY";
    private static final String ADDRESS_LINE2 = "SAMPLE ADDRESS LINE 2";
    private static final String ADDRESS_LINE3 = "SAMPLE ADDRESS LINE 3";
    private static final String POSTCODE = "LS2 8BT";
    private static final String ENGLAND = "ENGLAND";
    private static final String LEEDS = "LEEDS";


    @Test
    void theFormatAddressForTextFieldWhenAddressLine1PostTownPostCodeCountryExists() {
        // Given
        Address address = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();

        String expectedAddressString = "Co-operative Retail Services Ltd, 11, Merrion Way,\n"
            + "Leeds,\n"
            + "England";
        // When
        String actualAddressString = PdfMapperUtil.formatAddressForTextField(address);
        // Then
        assertThat(actualAddressString).isEqualTo(expectedAddressString);
    }

    @Test
    void theFormatAddressForTextFieldWhenAllFieldsExist() {
        // Given
        Address address = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setAddressLine1(ADDRESS_LINE1);
        address.setAddressLine2(ADDRESS_LINE2);
        address.setAddressLine3(ADDRESS_LINE3);
        address.setPostCode(POSTCODE);
        address.setCountry(ENGLAND);
        address.setPostTown(LEEDS);
        String expectedAddressString = "Co-operative Retail Services Ltd, 11, Merrion Way,\n"
            + "Sample Address Line 2,\n"
            + "Sample Address Line 3,\n"
            + "Leeds,\n"
            + "England";
        // When
        String actualAddressString = PdfMapperUtil.formatAddressForTextField(address);
        // Then
        assertThat(actualAddressString).isEqualTo(expectedAddressString);
    }

    @Test
    void theFormatAddressForTextFieldWhenAllFieldsAreEmpty() {
        // Given
        Address address = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setAddressLine1(null);
        address.setAddressLine2(null);
        address.setAddressLine3(null);
        address.setPostCode(null);
        address.setCountry(null);
        address.setPostTown(null);
        // When
        String actualAddressString = PdfMapperUtil.formatAddressForTextField(address);
        // Then
        assertDoesNotThrow(() -> PdfMapperUtil.formatAddressForTextField(address));
        assertThat(actualAddressString).isNull();
    }

    @Test
    void theFormatAddressForTextFieldWhenAddressLine1IsEmpty() {
        // Given
        Address address = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setAddressLine1("");
        address.setAddressLine2(ADDRESS_LINE2);
        address.setAddressLine3(ADDRESS_LINE3);
        address.setPostCode(POSTCODE);
        address.setCountry(ENGLAND);
        address.setPostTown(LEEDS);
        // When
        String actualAddressString = PdfMapperUtil.formatAddressForTextField(address);
        // Then
        assertDoesNotThrow(() -> PdfMapperUtil.formatAddressForTextField(address));
        assertThat(actualAddressString).isNull();
    }

    @Test
    void theFormatAddressForTextFieldWhenPostTownIsEmpty() {
        // Given
        Address address = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setAddressLine1(ADDRESS_LINE1);
        address.setAddressLine2(ADDRESS_LINE2);
        address.setAddressLine3(ADDRESS_LINE3);
        address.setPostCode(POSTCODE);
        address.setCountry(ENGLAND);
        address.setPostTown("");
        // When
        String actualAddressString = PdfMapperUtil.formatAddressForTextField(address);
        // Then
        assertDoesNotThrow(() -> PdfMapperUtil.formatAddressForTextField(address));
        assertThat(actualAddressString).isNull();
    }

    @Test
    void theFormatAddressForTextFieldWhenCountryIsEmpty() {
        // Given
        Address address = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setAddressLine1(ADDRESS_LINE1);
        address.setAddressLine2(ADDRESS_LINE2);
        address.setAddressLine3(ADDRESS_LINE3);
        address.setPostCode(POSTCODE);
        address.setCountry("");
        address.setPostTown(LEEDS);
        // When
        String actualAddressString = PdfMapperUtil.formatAddressForTextField(address);
        // Then
        assertDoesNotThrow(() -> PdfMapperUtil.formatAddressForTextField(address));
        assertThat(actualAddressString).isNull();
    }

    @Test
    void theFormatAddressForTextFieldReturnValueNotIncludesPostcode() {
        // Given
        Address address = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setAddressLine1(ADDRESS_LINE1);
        address.setAddressLine2(ADDRESS_LINE2);
        address.setAddressLine3(ADDRESS_LINE3);
        address.setPostCode(POSTCODE);
        address.setCountry(ENGLAND);
        address.setPostTown(LEEDS);
        // When
        String actualAddressString = PdfMapperUtil.formatAddressForTextField(address);
        // Then
        assertThat(actualAddressString).isNotEmpty();
        assertThat(actualAddressString).doesNotContain(address.getPostCode());
    }

    @ParameterizedTest
    @MethodSource("postcodeArguments")
    void theFormatUkPostcode(String expectedPostcode, Address srcPostcode) {
        assertThat(formatUkPostcode(srcPostcode)).isEqualTo(expectedPostcode);
    }

    @Test
    void theFormatUkPostcodeNotInUK() {
        Address address = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setCountry("United Arab Emirates");
        address.setPostCode("1WEXO9NYZ");
        assertThat(formatUkPostcode(address)).isEqualTo("1WEXO9NYZ");
    }

    private static Stream<Arguments> postcodeArguments() {
        return TestData.postcodeAddressArguments();

    @Test
    void convertAddressToString() {
        // Given
        Address address = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        String expectedAddressString = "Co-operative Retail Services Ltd, 11, Merrion Way\n"
            + "Leeds\n"
            + "England";
        // When
        String actualAddressString = PdfMapperUtil.convertAddressToString(address);
        // Then
        assertEquals(actualAddressString, expectedAddressString);
    }

}
