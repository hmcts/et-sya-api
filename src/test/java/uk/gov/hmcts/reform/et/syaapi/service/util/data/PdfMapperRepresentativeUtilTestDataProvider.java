package uk.gov.hmcts.reform.et.syaapi.service.util.data;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.reform.et.syaapi.service.util.TestConstants;

import java.util.stream.Stream;

public final class PdfMapperRepresentativeUtilTestDataProvider {

    private PdfMapperRepresentativeUtilTestDataProvider() {
        // Utility classes should not have a public or default constructor.
    }

    public static Stream<Arguments> generateRepresentativeClaimantTypes() {
        Address representativeAddress = TestDataProvider.generateAddressByAddressFields(
            TestConstants.ADDRESS_LINE_1,
            TestConstants.ADDRESS_LINE_2,
            TestConstants.ADDRESS_LINE_3,
            TestConstants.POST_TOWN,
            TestConstants.COUNTY,
            TestConstants.COUNTRY,
            TestConstants.POSTCODE);
        RepresentedTypeC representativeClaimantTypeAllFilled =
            generateRepresentativeClaimantType(representativeAddress,
                                               TestConstants.REPRESENTATIVE_NAME,
                                               TestConstants.REPRESENTATIVE_REFERENCE,
                                               TestConstants.REPRESENTATIVE_ORGANISATION,
                                               TestConstants.REPRESENTATIVE_PHONE_NUMBER,
                                               TestConstants.REPRESENTATIVE_MOBILE_NUMBER,
                                               TestConstants.REPRESENTATIVE_EMAIL,
                                               TestConstants.EMAIL);
        RepresentedTypeC representativeClaimantTypeAddressNull =
            generateRepresentativeClaimantType(null,
                                               TestConstants.REPRESENTATIVE_NAME,
                                               TestConstants.REPRESENTATIVE_REFERENCE,
                                               TestConstants.REPRESENTATIVE_ORGANISATION,
                                               TestConstants.REPRESENTATIVE_PHONE_NUMBER,
                                               TestConstants.REPRESENTATIVE_MOBILE_NUMBER,
                                               TestConstants.REPRESENTATIVE_EMAIL,
                                               TestConstants.EMAIL);
        RepresentedTypeC representativeClaimantPreferenceNull =
            generateRepresentativeClaimantType(representativeAddress,
                                               TestConstants.REPRESENTATIVE_NAME,
                                               TestConstants.REPRESENTATIVE_REFERENCE,
                                               TestConstants.REPRESENTATIVE_ORGANISATION,
                                               TestConstants.REPRESENTATIVE_PHONE_NUMBER,
                                               TestConstants.REPRESENTATIVE_MOBILE_NUMBER,
                                               TestConstants.REPRESENTATIVE_EMAIL,
                                               TestConstants.NULL_STRING);
        RepresentedTypeC representativeClaimantPreferenceEmpty =
            generateRepresentativeClaimantType(representativeAddress,
                                               TestConstants.REPRESENTATIVE_NAME,
                                               TestConstants.REPRESENTATIVE_REFERENCE,
                                               TestConstants.REPRESENTATIVE_ORGANISATION,
                                               TestConstants.REPRESENTATIVE_PHONE_NUMBER,
                                               TestConstants.REPRESENTATIVE_MOBILE_NUMBER,
                                               TestConstants.REPRESENTATIVE_EMAIL,
                                               TestConstants.EMPTY_STRING);
        RepresentedTypeC representativeClaimantPreferenceBlank =
            generateRepresentativeClaimantType(representativeAddress,
                                               TestConstants.REPRESENTATIVE_NAME,
                                               TestConstants.REPRESENTATIVE_REFERENCE,
                                               TestConstants.REPRESENTATIVE_ORGANISATION,
                                               TestConstants.REPRESENTATIVE_PHONE_NUMBER,
                                               TestConstants.REPRESENTATIVE_MOBILE_NUMBER,
                                               TestConstants.REPRESENTATIVE_EMAIL,
                                               TestConstants.BLANK_STRING);
        RepresentedTypeC representativeClaimantTypePreferenceFax =
            generateRepresentativeClaimantType(representativeAddress,
                                               TestConstants.REPRESENTATIVE_NAME,
                                               TestConstants.REPRESENTATIVE_REFERENCE,
                                               TestConstants.REPRESENTATIVE_ORGANISATION,
                                               TestConstants.REPRESENTATIVE_PHONE_NUMBER,
                                               TestConstants.REPRESENTATIVE_MOBILE_NUMBER,
                                               TestConstants.REPRESENTATIVE_EMAIL,
                                               TestConstants.FAX);
        RepresentedTypeC representativeClaimantTypePreferencePost =
            generateRepresentativeClaimantType(representativeAddress,
                                               TestConstants.REPRESENTATIVE_NAME,
                                               TestConstants.REPRESENTATIVE_REFERENCE,
                                               TestConstants.REPRESENTATIVE_ORGANISATION,
                                               TestConstants.REPRESENTATIVE_PHONE_NUMBER,
                                               TestConstants.REPRESENTATIVE_MOBILE_NUMBER,
                                               TestConstants.REPRESENTATIVE_EMAIL,
                                               TestConstants.POST);

        return Stream.of(
            Arguments.of(representativeClaimantTypeAllFilled),
            Arguments.of(representativeClaimantTypeAddressNull),
            Arguments.of(representativeClaimantPreferenceNull),
            Arguments.of(representativeClaimantPreferenceEmpty),
            Arguments.of(representativeClaimantPreferenceBlank),
            Arguments.of(representativeClaimantTypePreferenceFax),
            Arguments.of(representativeClaimantTypePreferencePost)
        );
    }

    public static RepresentedTypeC generateRepresentativeClaimantType(Address representativeAddress,
                                                                      String representativeName,
                                                                      String representativeReference,
                                                                      String representativeOrganisation,
                                                                      String representativePhoneNumber,
                                                                      String representativeMobileNumber,
                                                                      String representativeEmailAddress,
                                                                      String representativePreference) {
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setRepresentativeAddress(representativeAddress);
        representedTypeC.setNameOfRepresentative(representativeName);
        representedTypeC.setRepresentativeReference(representativeReference);
        representedTypeC.setNameOfOrganisation(representativeOrganisation);
        representedTypeC.setRepresentativePhoneNumber(representativePhoneNumber);
        representedTypeC.setRepresentativeMobileNumber(representativeMobileNumber);
        representedTypeC.setRepresentativeEmailAddress(representativeEmailAddress);
        representedTypeC.setRepresentativePreference(representativePreference);

        return representedTypeC;
    }

}
