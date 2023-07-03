package uk.gov.hmcts.reform.et.syaapi.service.util.data;

import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;

import static uk.gov.hmcts.reform.et.syaapi.service.util.TestConstants.TEST_CASE_DATA_JSON_FILE;

public final class TestDataProvider {

    private TestDataProvider() {
        // Utility classes should not have a public or default constructor.
    }

    public static Address generateTestAddressByPostcodeCountry(String postCode, String country) {
        CaseData caseData = ResourceLoader.fromString(TEST_CASE_DATA_JSON_FILE, CaseData.class);
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
        CaseData caseData = ResourceLoader.fromString(TEST_CASE_DATA_JSON_FILE, CaseData.class);
        caseData.getClaimantRequests().setClaimantCompensationText(claimantCompensationText);
        caseData.getClaimantRequests().setClaimantCompensationAmount(claimantCompensationAmount);
        caseData.getClaimantRequests().setClaimantTribunalRecommendation(claimantTribunalRecommendation);
        return caseData;
    }

    public static CaseData generateTestCaseDataByClaimantHearingPreferenceContactLanguage(
        boolean isClaimantHearingPreferenceEmpty,
        String contactLanguage) {
        CaseData caseData = ResourceLoader.fromString(TEST_CASE_DATA_JSON_FILE, CaseData.class);
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
        CaseData caseData = ResourceLoader.fromString(TEST_CASE_DATA_JSON_FILE, CaseData.class);
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

    public static CaseData generateCaseDataForRespondent(String ethosCaseReference,
                                                         String claimantWorkAddressQuestion,
                                                         Address claimantWorkAddress) {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference(ethosCaseReference);
        caseData.setClaimantWorkAddressQuestion(claimantWorkAddressQuestion);
        ClaimantWorkAddressType claimantWorkAddressType = new ClaimantWorkAddressType();
        claimantWorkAddressType.setClaimantWorkAddress(claimantWorkAddress);
        caseData.setClaimantWorkAddress(claimantWorkAddressType);

        return caseData;
    }

}
