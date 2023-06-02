package uk.gov.hmcts.reform.et.syaapi.service.utils;

import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;

public final class TestUtil {

    private TestUtil() {

        // Utility classes should not have a public or default constructor.
    }

    public static Address generateTestAddressByPostcodeCountry(String postCode, String country) {
        CaseData caseData = ResourceLoader.fromString("requests/caseData.json", CaseData.class);
        if (postCode != null) {
            caseData.getClaimantType().getClaimantAddressUK().setPostCode(postCode);
        }
        if (country != null) {
            caseData.getClaimantType().getClaimantAddressUK().setCountry(country);
        }
        return caseData.getClaimantType().getClaimantAddressUK();
    }

    public static CaseData generateTestCaseDataByClaimantCompensation(String claimantCompensationText,
                                                                  String claimantCompensationAmount,
                                                                  String claimantTribunalRecommendation) {
        CaseData caseData = ResourceLoader.fromString("requests/caseData.json", CaseData.class);
        caseData.getClaimantRequests().setClaimantCompensationText(claimantCompensationText);
        caseData.getClaimantRequests().setClaimantCompensationAmount(claimantCompensationAmount);
        caseData.getClaimantRequests().setClaimantTribunalRecommendation(claimantTribunalRecommendation);
        return caseData;
    }

    public static CaseData generateTestCaseDataByClaimantHearingPreferenceContactLanguage(
        boolean isClaimantHearingPreferenceEmpty,
        String contactLanguage) {
        CaseData caseData = ResourceLoader.fromString("requests/caseData.json", CaseData.class);
        if (isClaimantHearingPreferenceEmpty) {
            caseData.setClaimantHearingPreference(null);
        } else {
            caseData.getClaimantHearingPreference().setContactLanguage(contactLanguage);
        }
        return caseData;
    }

    public static CaseData generateTestCaseDataByFirstNames(
        String claimantIndTypeEmptyOrNull,
        String firstNames) {
        CaseData caseData = ResourceLoader.fromString("requests/caseData.json", CaseData.class);
        if ("null".equals(claimantIndTypeEmptyOrNull)) {
            caseData.setClaimantIndType(null);
        } else if ("empty".equals(claimantIndTypeEmptyOrNull)) {
            caseData.setClaimantIndType(new ClaimantIndType());
        } else {
            caseData.getClaimantIndType().setClaimantFirstNames(firstNames);
        }
        return caseData;
    }

    public static Address generateAddressByAddressFields(String addressLine1,
                                                         String addressLine2,
                                                         String addressLine3,
                                                         String postTown,
                                                         String county,
                                                         String country,
                                                         String postCode) {
        Address address = new Address();
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);
        address.setAddressLine3(addressLine3);
        address.setPostTown(postTown);
        address.setCounty(county);
        address.setCountry(country);
        address.setPostCode(postCode);
        return address;
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
}
