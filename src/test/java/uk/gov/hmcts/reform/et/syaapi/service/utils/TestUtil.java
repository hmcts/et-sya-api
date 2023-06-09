package uk.gov.hmcts.reform.et.syaapi.service.utils;

import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.List;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_DATA_JSON_FILE;

public final class TestUtil {

    private TestUtil() {

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

    public static RespondentSumTypeItem generateRespondentSumTypeItem(String respondentSumTypeItemId,
                                                              String name,
                                                              Address respondentAddress,
                                                              String respondentAcasQuestion,
                                                              String acasNumber,
                                                              String respondentAcasNo) {
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId(respondentSumTypeItemId);
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(name);
        respondentSumType.setRespondentAddress(respondentAddress);
        respondentSumType.setRespondentAcasQuestion(respondentAcasQuestion);
        respondentSumType.setRespondentAcas(acasNumber);
        respondentSumType.setRespondentAcasNo(respondentAcasNo);
        respondentSumTypeItem.setValue(respondentSumType);
        return respondentSumTypeItem;
    }

    public static ClaimantHearingPreference generateClaimantHearingPreference(boolean isEmptyReasonableAdjustment,
                                                                              String reasonableAdjustment,
                                                                              String reasonableAdjustmentDetails,
                                                                              String[] hearingPreferences,
                                                                              String hearingAssistance) {
        ClaimantHearingPreference claimantHearingPreference = new ClaimantHearingPreference();
        if (isEmptyReasonableAdjustment) {
            return claimantHearingPreference;
        }
        claimantHearingPreference.setReasonableAdjustments(reasonableAdjustment);
        claimantHearingPreference.setReasonableAdjustmentsDetail(reasonableAdjustmentDetails);
        claimantHearingPreference.setHearingPreferences(List.of(hearingPreferences));
        claimantHearingPreference.setHearingAssistance(hearingAssistance);
        return claimantHearingPreference;
    }
}
