package uk.gov.hmcts.reform.et.syaapi.service.pdf.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperServiceUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperServiceUtil.generateClaimantCompensation;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperServiceUtil.generateClaimantTribunalRecommendation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({"PMD.TooManyMethods"})
class PdfMapperServiceUtilTest {

    private static final String ADDRESS_LINE1 = "CO-OPERATIVE RETAIL SERVICES LTD, 11, MERRION WAY";
    private static final String ADDRESS_LINE2 = "SAMPLE ADDRESS LINE 2";
    private static final String ADDRESS_LINE3 = "SAMPLE ADDRESS LINE 3";
    private static final String POSTCODE = "LS2 8BT";
    private static final String ENGLAND = "ENGLAND";
    private static final String LEEDS = "LEEDS";


    @Test
    void theFormatAddressForTextFieldWhenAddressLine1PostTownPostCodeCountryExists() {
        // Given
        Address address = new CaseTestData().getCaseData().getClaimantType().getClaimantAddressUK();

        String expectedAddressString = """
            Co-operative Retail Services Ltd, 11, Merrion Way,
            Leeds,
            England""";
        // When
        String actualAddressString = PdfMapperServiceUtil.formatAddressForTextField(address);
        // Then
        assertThat(actualAddressString).isEqualTo(expectedAddressString);
    }

    @Test
    void theFormatAddressForTextFieldWhenAllFieldsExist() {
        // Given
        Address address = new CaseTestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setAddressLine1(ADDRESS_LINE1);
        address.setAddressLine2(ADDRESS_LINE2);
        address.setAddressLine3(ADDRESS_LINE3);
        address.setPostCode(POSTCODE);
        address.setCountry(ENGLAND);
        address.setPostTown(LEEDS);
        String expectedAddressString = """
            Co-operative Retail Services Ltd, 11, Merrion Way,
            Sample Address Line 2,
            Sample Address Line 3,
            Leeds,
            England""";
        // When
        String actualAddressString = PdfMapperServiceUtil.formatAddressForTextField(address);
        // Then
        assertThat(actualAddressString).isEqualTo(expectedAddressString);
    }

    @Test
    void theFormatAddressForTextFieldWhenAllFieldsAreEmpty() {
        // Given
        Address address = new CaseTestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setAddressLine1(null);
        address.setAddressLine2(null);
        address.setAddressLine3(null);
        address.setPostCode(null);
        address.setCountry(null);
        address.setPostTown(null);
        // When
        String actualAddressString = PdfMapperServiceUtil.formatAddressForTextField(address);
        // Then
        assertDoesNotThrow(() -> PdfMapperServiceUtil.formatAddressForTextField(address));
        assertThat(actualAddressString).isNull();
    }

    @Test
    void theFormatAddressForTextFieldWhenAddressLine1IsEmpty() {
        // Given
        Address address = new CaseTestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setAddressLine1("");
        address.setAddressLine2(ADDRESS_LINE2);
        address.setAddressLine3(ADDRESS_LINE3);
        address.setPostCode(POSTCODE);
        address.setCountry(ENGLAND);
        address.setPostTown(LEEDS);
        // When
        String actualAddressString = PdfMapperServiceUtil.formatAddressForTextField(address);
        // Then
        assertDoesNotThrow(() -> PdfMapperServiceUtil.formatAddressForTextField(address));
        assertThat(actualAddressString).isNull();
    }

    @Test
    void theFormatAddressForTextFieldWhenPostTownIsEmpty() {
        // Given
        Address address = new CaseTestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setAddressLine1(ADDRESS_LINE1);
        address.setAddressLine2(ADDRESS_LINE2);
        address.setAddressLine3(ADDRESS_LINE3);
        address.setPostCode(POSTCODE);
        address.setCountry(ENGLAND);
        address.setPostTown("");
        // When
        String actualAddressString = PdfMapperServiceUtil.formatAddressForTextField(address);
        // Then
        assertDoesNotThrow(() -> PdfMapperServiceUtil.formatAddressForTextField(address));
        assertThat(actualAddressString).isNull();
    }

    @Test
    void theFormatAddressForTextFieldWhenCountryIsEmpty() {
        // Given
        Address address = new CaseTestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setAddressLine1(ADDRESS_LINE1);
        address.setAddressLine2(ADDRESS_LINE2);
        address.setAddressLine3(ADDRESS_LINE3);
        address.setPostCode(POSTCODE);
        address.setCountry("");
        address.setPostTown(LEEDS);
        // When
        String actualAddressString = PdfMapperServiceUtil.formatAddressForTextField(address);
        // Then
        assertDoesNotThrow(() -> PdfMapperServiceUtil.formatAddressForTextField(address));
        assertThat(actualAddressString).isNull();
    }

    @Test
    void theFormatAddressForTextFieldReturnValueNotIncludesPostcode() {
        // Given
        Address address = new CaseTestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setAddressLine1(ADDRESS_LINE1);
        address.setAddressLine2(ADDRESS_LINE2);
        address.setAddressLine3(ADDRESS_LINE3);
        address.setPostCode(POSTCODE);
        address.setCountry(ENGLAND);
        address.setPostTown(LEEDS);
        // When
        String actualAddressString = PdfMapperServiceUtil.formatAddressForTextField(address);
        // Then
        assertThat(actualAddressString).isNotEmpty().doesNotContain(address.getPostCode());
    }

    @Test
    void formatUkPostcodeNull() {
        Address address = new CaseTestData().getCaseData().getClaimantType().getClaimantAddressUK();
        address.setPostCode(null);
        assertThat(PdfMapperServiceUtil.formatUkPostcode(address)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.et.syaapi.model.TestData#postcodeAddressArguments")
    void formatUkPostcode(String expectedPostCode, Address address) {
        assertThat(PdfMapperServiceUtil.formatUkPostcode(address)).isEqualTo(expectedPostCode);
    }

    @ParameterizedTest
    @CsvSource({"1979-08-05,05-08-1979", "null,null", "\"\", \"\"", ",''"})
    void formatDate(String inputDate, String expectedDate) {
        assertThat(PdfMapperServiceUtil.formatDate(inputDate)).isEqualTo(expectedDate);
    }

    @ParameterizedTest
    @CsvSource({"yes,true", "Yes,true", "YEs,true", "YES,true", "yES,true", "yeS,true", "yEs,true",
        "No,false","test,false"})
    void isYes(String inputDate, boolean expected) {
        assertThat(PdfMapperServiceUtil.isYes(inputDate)).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.et.syaapi.model.TestData#compensationArguments")
    void theGenerateClaimantCompensation(CaseData caseData, String expectedValue) {
        assertThat(generateClaimantCompensation(caseData)).isEqualTo(expectedValue);
    }

    @Test
    void theGenerateClaimantTribunalRecommendationCaseDataNull() {
        assertThat(generateClaimantTribunalRecommendation(null)).isEmpty();
    }

    @Test
    void theGenerateClaimantClaimantRequestsNull() {
        CaseData caseData = new CaseTestData().getCaseData();
        caseData.setClaimantRequests(null);
        assertThat(generateClaimantTribunalRecommendation(caseData)).isEmpty();
    }

    @Test
    void theGenerateClaimantClaimantTribunalRecommendationNull() {
        CaseData caseData = new CaseTestData().getCaseData();
        caseData.getClaimantRequests().setClaimantTribunalRecommendation(null);
        assertThat(generateClaimantTribunalRecommendation(caseData)).isEmpty();
    }
}
