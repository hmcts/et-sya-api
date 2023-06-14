package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.NewEmploymentType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;

import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.AVERAGE_WEEKLY_HOURS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_BENEFITS_DETAIL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_EMPLOYED_FROM;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_EMPLOYED_TO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_OCCUPATION;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_PAY_AFTER_TAX;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_PAY_BEFORE_TAX;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_PENSION_WEEKLY_CONTRIBUTION;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NEW_PAY_BEFORE_TAX;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NO_LONGER_WORKING;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.SWIFT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TAYLOR;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TRUE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.VIDEO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WEEKS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

public final class PdfMapperTestDataProvider {

    private PdfMapperTestDataProvider() {
        // Utility classes should not have a public or default constructor.
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

    public static Stream<Arguments> generateClaimantRequests() {

        ClaimantRequestType claimantRequestClaimDescriptionNull = new ClaimantRequestType();
        claimantRequestClaimDescriptionNull.setClaimDescription(null);
        ClaimantRequestType claimantRequestClaimDescriptionEmpty = new ClaimantRequestType();
        claimantRequestClaimDescriptionEmpty.setClaimDescription(TestConstants.EMPTY_STRING);
        ClaimantRequestType claimantRequestClaimDescriptionBlank = new ClaimantRequestType();
        claimantRequestClaimDescriptionBlank.setClaimDescription(TestConstants.BLANK_STRING);
        ClaimantRequestType claimantRequestClaimDescriptionFilled = new ClaimantRequestType();
        claimantRequestClaimDescriptionFilled.setClaimDescription(TestConstants.CLAIM_DESCRIPTION);
        ClaimantRequestType claimantRequestEmpty = new ClaimantRequestType();

        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(claimantRequestEmpty, null),
            Arguments.of(claimantRequestClaimDescriptionNull, null),
            Arguments.of(claimantRequestClaimDescriptionEmpty, null),
            Arguments.of(claimantRequestClaimDescriptionBlank, null),
            Arguments.of(claimantRequestClaimDescriptionFilled, TestConstants.CLAIM_DESCRIPTION)
        );
    }

    public static Stream<Arguments> generateCaseDataSamplesWithHearingPreferences() {
        // Empty Reasonable Adjustments
        CaseData caseDataEmptyReasonableAdjustment = new CaseData();
        caseDataEmptyReasonableAdjustment.setClaimantHearingPreference(
            generateClaimantHearingPreference(TRUE, TestConstants.NULL_STRING, TestConstants.NULL_STRING,
                                              TestConstants.EMPTY_STRING_ARRAY, TestConstants.HEARING_ASSISTANCE));
        caseDataEmptyReasonableAdjustment.setEthosCaseReference(TestConstants.STRING_NUMERIC_ONE);
        // Yes Reasonable Adjustments
        CaseData caseDataYesReasonableAdjustments = new CaseData();
        caseDataYesReasonableAdjustments.setClaimantHearingPreference(
            generateClaimantHearingPreference(TestConstants.FALSE, YES, TestConstants.REASONABLE_ADJUSTMENT_DETAILS,
                                              TestConstants.EMPTY_STRING_ARRAY, TestConstants.HEARING_ASSISTANCE));
        caseDataYesReasonableAdjustments.setEthosCaseReference(TestConstants.STRING_NUMERIC_TWO);
        // No Reasonable Adjustments
        CaseData caseDataNoReasonableAdjustments = new CaseData();
        caseDataNoReasonableAdjustments.setClaimantHearingPreference(
            generateClaimantHearingPreference(TestConstants.FALSE, TestConstants.NO, TestConstants.NULL_STRING,
                                              TestConstants.EMPTY_STRING_ARRAY, TestConstants.HEARING_ASSISTANCE));
        caseDataNoReasonableAdjustments.setEthosCaseReference(TestConstants.STRING_NUMERIC_THREE);
        // No Hearing Preference Selected
        CaseData caseDataNoHearingPreferenceSelected = new CaseData();
        caseDataNoHearingPreferenceSelected.setClaimantHearingPreference(
            generateClaimantHearingPreference(TestConstants.FALSE, YES, TestConstants.REASONABLE_ADJUSTMENT_DETAILS,
                                              TestConstants.EMPTY_STRING_ARRAY, TestConstants.HEARING_ASSISTANCE));
        caseDataNoHearingPreferenceSelected.setEthosCaseReference(TestConstants.STRING_NUMERIC_FOUR);
        // No Hearing Preference Selected
        CaseData caseDataHearingPreferenceVideoPhoneSelected = new CaseData();
        caseDataHearingPreferenceVideoPhoneSelected.setClaimantHearingPreference(
            generateClaimantHearingPreference(TestConstants.FALSE, YES, TestConstants.REASONABLE_ADJUSTMENT_DETAILS,
                                              new String[]{VIDEO, TestConstants.PHONE},
                                              TestConstants.HEARING_ASSISTANCE));
        caseDataHearingPreferenceVideoPhoneSelected.setEthosCaseReference(TestConstants.STRING_NUMERIC_FIVE);
        // Invalid Hearing Preference Selected
        CaseData caseDataInvalidHearingPreferenceSelected = new CaseData();
        caseDataInvalidHearingPreferenceSelected.setClaimantHearingPreference(
            generateClaimantHearingPreference(TestConstants.FALSE, YES, TestConstants.REASONABLE_ADJUSTMENT_DETAILS,
                                              new String[]{"Dummy", "String"}, TestConstants.HEARING_ASSISTANCE));
        caseDataHearingPreferenceVideoPhoneSelected.setEthosCaseReference(TestConstants.STRING_NUMERIC_SIX);
        // Empty Hearing Preferences
        CaseData caseDataClaimantHearingPreferencesEmpty = new CaseData();
        return Stream.of(Arguments.of(caseDataClaimantHearingPreferencesEmpty),
                         Arguments.of(caseDataEmptyReasonableAdjustment),
                         Arguments.of(caseDataYesReasonableAdjustments),
                         Arguments.of(caseDataNoReasonableAdjustments),
                         Arguments.of(caseDataInvalidHearingPreferenceSelected),
                         Arguments.of(caseDataHearingPreferenceVideoPhoneSelected),
                         Arguments.of(caseDataNoHearingPreferenceSelected));
    }

    public static Stream<Arguments> generateClaimantIndTypeArguments() {
        ClaimantIndType claimantIndTypeOtherTitleMaleNotNullDateOfBirth =
            generateClaimantIndType(TestConstants.OTHER, TestConstants.OTHER_TITLE, TestConstants.MICHAEL,
                                    TestConstants.MERCURY,"1979-05-08", TestConstants.MALE);
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

    public static Stream<Arguments> generateRepresentativeClaimantTypes() {
        Address representativeAddress = TestDataProvider.generateAddressByAddressFields(TestConstants.ADDRESS_LINE_1,
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


    public static Stream<Arguments> generateClaimantOtherTypes() {
        // Past employer, not working anymore, Pay Cycle Weekly, Notice Period Yes, Notice Period Unit Week,
        // Pension Contribution Yes, Other benefits car
        ClaimantOtherType claimantOtherType = new ClaimantOtherType();
        claimantOtherType.setPastEmployer(YES);
        claimantOtherType.setClaimantEmployedFrom(CLAIMANT_EMPLOYED_FROM);
        claimantOtherType.setStillWorking(NO_LONGER_WORKING);
        claimantOtherType.setClaimantEmployedTo(CLAIMANT_EMPLOYED_TO);
        claimantOtherType.setClaimantOccupation(CLAIMANT_OCCUPATION);
        claimantOtherType.setClaimantAverageWeeklyHours(AVERAGE_WEEKLY_HOURS);
        claimantOtherType.setClaimantPayBeforeTax(CLAIMANT_PAY_BEFORE_TAX);
        claimantOtherType.setClaimantPayAfterTax(CLAIMANT_PAY_AFTER_TAX);
        claimantOtherType.setClaimantPayCycle(WEEKS);
        claimantOtherType.setClaimantNoticePeriod(YES);
        claimantOtherType.setClaimantNoticePeriodUnit(WEEKS);
        claimantOtherType.setClaimantNoticePeriodDuration("4");
        claimantOtherType.setClaimantPensionWeeklyContribution(CLAIMANT_PENSION_WEEKLY_CONTRIBUTION);
        claimantOtherType.setClaimantBenefitsDetail(CLAIMANT_BENEFITS_DETAIL);
        // New Employment Type is filled and NewJob is Yes, New Employment Type payment is weekly
        NewEmploymentType newEmploymentType = new NewEmploymentType();
        newEmploymentType.setNewJob(YES);
        newEmploymentType.setNewlyEmployedFrom(CLAIMANT_EMPLOYED_FROM);
        newEmploymentType.setNewPayBeforeTax(NEW_PAY_BEFORE_TAX);
        newEmploymentType.setNewJobPayInterval(WEEKS);
        CaseData caseData1 = new CaseData();
        caseData1.setClaimantOtherType(claimantOtherType);
        caseData1.setNewEmploymentType(newEmploymentType);

        return Stream.of(
            Arguments.of(caseData1)
        );
    }
}
