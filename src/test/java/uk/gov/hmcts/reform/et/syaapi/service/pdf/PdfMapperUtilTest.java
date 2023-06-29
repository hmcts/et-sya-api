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
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperUtil.formatDate;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperUtil.formatUkPostcode;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperUtil.generateClaimantCompensation;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperUtil.generateClaimantTribunalRecommendation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({"PMD.TooManyMethods"})
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

    @Test
    void theFormatUkPostcodeNull() {
        Address address = new TestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setPostCode(null);
        assertThat(formatUkPostcode(address)).isEmpty();
    }


    @Test
    void theFormatDate() {
        String dateToBeFormatted = "2022-12-01";
        String expectedDateString = "01-12-2022";

        assertThat(formatDate(dateToBeFormatted)).isEqualTo(expectedDateString);
    }


    @Test
    void theFormatDateNull() {
        String expectedDateString = "";
        assertThat(formatDate(null)).isEqualTo(expectedDateString);
    }

    @ParameterizedTest
    @MethodSource("compensationArguments")
    void theGenerateClaimantCompensation(CaseData caseData, String expectedValue) {
        assertThat(generateClaimantCompensation(caseData)).isEqualTo(expectedValue);
    }

    @Test
    void theGenerateClaimantTribunalRecommendationCaseDataNull() {
        assertThat(generateClaimantTribunalRecommendation(null)).isEmpty();
    }

    @Test
    void theGenerateClaimantClaimantRequestsNull() {
        CaseData caseData = new TestData().getCaseData();
        caseData.setClaimantRequests(null);
        assertThat(generateClaimantTribunalRecommendation(caseData)).isEmpty();
    }

    @Test
    void theGenerateClaimantClaimantTribunalRecommendationNull() {
        CaseData caseData = new TestData().getCaseData();
        caseData.getClaimantRequests().setClaimantTribunalRecommendation(null);
        assertThat(generateClaimantTribunalRecommendation(caseData)).isEmpty();
    }

    private static Stream<Arguments> postcodeArguments() {
        return TestData.postcodeAddressArguments();
    }

    private static Stream<Arguments> compensationArguments() {
        return TestData.compensationArguments();
    }
}