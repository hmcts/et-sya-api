package uk.gov.hmcts.reform.et.syaapi.service.utils.data;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;

import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.SWIFT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TAYLOR;

public final class PdfMapperPersonalDetailsUtilTestDataProvider {

    private PdfMapperPersonalDetailsUtilTestDataProvider() {
        // Utility classes should not have a public or default constructor.
    }

    public static Stream<Arguments> generateClaimantIndTypeArguments() {
        ClaimantIndType claimantIndTypeOtherTitleMaleNotNullDateOfBirth =
            generateClaimantIndType(TestConstants.OTHER, TestConstants.OTHER_TITLE, TestConstants.MICHAEL,
                                    TestConstants.MERCURY, "1979-05-08", TestConstants.MALE);
        ClaimantIndType claimantIndTypeTitleMrMaleNotNullDateOfBirth =
            generateClaimantIndType(TestConstants.MR, TestConstants.NULL_STRING, TestConstants.MICHAEL,
                                    TestConstants.MERCURY,"1980-06-09", TestConstants.MALE);
        ClaimantIndType claimantIndTypeTitleMsFemaleNotNullDateOfBirth =
            generateClaimantIndType(TestConstants.MS, TestConstants.NULL_STRING, TestConstants.ELIZABETH, TAYLOR,
                                    "1981-07-10", TestConstants.FEMALE);
        ClaimantIndType claimantIndTypeTitleMrsFemaleNotNullDateOfBirth =
            generateClaimantIndType(TestConstants.MRS, TestConstants.NULL_STRING, TAYLOR, SWIFT,
                                    "1982-08-11", TestConstants.FEMALE);
        ClaimantIndType claimantIndTypeTitleMissFemaleNotNullDateOfBirth =
            generateClaimantIndType(TestConstants.MISS, TestConstants.NULL_STRING, TAYLOR, SWIFT,
                                    "1983-09-12", TestConstants.FEMALE);
        ClaimantIndType claimantIndTypeOtherTitleMaleNullDateOfBirth =
            generateClaimantIndType(TestConstants.OTHER, TestConstants.OTHER_TITLE, TestConstants.MICHAEL,
                                    TestConstants.MERCURY, TestConstants.NULL_STRING, TestConstants.MALE);
        ClaimantIndType claimantIndTypeOtherTitleMaleEmptyDateOfBirth =
            generateClaimantIndType(TestConstants.OTHER, TestConstants.OTHER_TITLE, TestConstants.MICHAEL,
                                    TestConstants.MERCURY, TestConstants.EMPTY_STRING, TestConstants.MALE);
        ClaimantIndType claimantIndTypeOtherTitleMaleBlankDateOfBirth =
            generateClaimantIndType(TestConstants.OTHER, TestConstants.OTHER_TITLE, TestConstants.MICHAEL,
                                    TestConstants.MERCURY, TestConstants.BLANK_STRING, TestConstants.MALE);
        ClaimantIndType claimantIndTypeOtherTitlePreferNotToSay =
            generateClaimantIndType(TestConstants.OTHER, TestConstants.OTHER_TITLE, TestConstants.MICHAEL,
                                    TestConstants.MERCURY, TestConstants.BLANK_STRING, TestConstants.PREFER_NOT_TO_SAY
            );
        return Stream.of(
            Arguments.of(claimantIndTypeOtherTitleMaleNotNullDateOfBirth, "08", "05", "1979"),
            Arguments.of(claimantIndTypeTitleMrMaleNotNullDateOfBirth, "09", "06", "1980"),
            Arguments.of(claimantIndTypeTitleMsFemaleNotNullDateOfBirth, "10", "07", "1981"),
            Arguments.of(claimantIndTypeTitleMrsFemaleNotNullDateOfBirth, "11", "08", "1982"),
            Arguments.of(claimantIndTypeTitleMissFemaleNotNullDateOfBirth, "12", "09", "1983"),
            Arguments.of(claimantIndTypeOtherTitleMaleNullDateOfBirth, "", "", ""),
            Arguments.of(claimantIndTypeOtherTitleMaleEmptyDateOfBirth, "", "", ""),
            Arguments.of(claimantIndTypeOtherTitleMaleBlankDateOfBirth, "", "", ""),
            Arguments.of(claimantIndTypeOtherTitlePreferNotToSay, "", "", "")
        );
    }

    public static ClaimantIndType generateClaimantIndType(String claimantPreferredTitle,
                                                          String otherTitle,
                                                          String firstName,
                                                          String lastName,
                                                          String dateOfBirth,
                                                          String sex) {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantPreferredTitle(claimantPreferredTitle);
        claimantIndType.setClaimantTitleOther(otherTitle);
        claimantIndType.setClaimantFirstNames(firstName);
        claimantIndType.setClaimantLastName(lastName);
        claimantIndType.setClaimantDateOfBirth(dateOfBirth);
        claimantIndType.setClaimantSex(sex);
        return claimantIndType;
    }

    public static Stream<Arguments> generateClaimantTypeArguments() {

        ClaimantType claimantTypePhoneNumber = new ClaimantType();
        claimantTypePhoneNumber.setClaimantPhoneNumber("07444444444");
        ClaimantType claimantTypeMobileNumber = new ClaimantType();
        claimantTypeMobileNumber.setClaimantPhoneNumber("07444444555");
        ClaimantType claimantTypeEmail = new ClaimantType();
        claimantTypeEmail.setClaimantEmailAddress("mehmet@tdmehmet.com");
        ClaimantType claimantTypeContactPreferenceEmail = new ClaimantType();
        claimantTypeContactPreferenceEmail.setClaimantContactPreference("Email");
        ClaimantType claimantTypeContactPreferencePost = new ClaimantType();
        claimantTypeContactPreferencePost.setClaimantContactPreference("Post");
        Address claimantAddressUK = TestDataProvider.generateAddressByAddressFields(TestConstants.ADDRESS_LINE_1,
                                                                                    TestConstants.ADDRESS_LINE_2,
                                                                                    TestConstants.ADDRESS_LINE_3,
                                                                                    TestConstants.POST_TOWN,
                                                                                    TestConstants.COUNTY,
                                                                                    TestConstants.COUNTRY,
                                                                                    TestConstants.POSTCODE);
        ClaimantType claimantTypeAddressUK = new ClaimantType();
        claimantTypeAddressUK.setClaimantAddressUK(claimantAddressUK);
        ClaimantType claimantTypeAll = new ClaimantType();
        claimantTypeAll.setClaimantPhoneNumber("07444444444");
        claimantTypeAll.setClaimantPhoneNumber("07444444555");
        claimantTypeAll.setClaimantEmailAddress("mehmet@tdmehmet.com");
        claimantTypeAll.setClaimantContactPreference("Email");
        claimantTypeAll.setClaimantContactPreference("Post");
        claimantTypeAll.setClaimantAddressUK(claimantAddressUK);
        ClaimantType claimantTypeBlank = new ClaimantType();
        return Stream.of(
            Arguments.of(claimantTypeBlank),
            Arguments.of(claimantTypePhoneNumber),
            Arguments.of(claimantTypeMobileNumber),
            Arguments.of(claimantTypeEmail),
            Arguments.of(claimantTypeContactPreferenceEmail),
            Arguments.of(claimantTypeContactPreferencePost),
            Arguments.of(claimantTypeAddressUK),
            Arguments.of(claimantTypeAll)
        );
    }

}
