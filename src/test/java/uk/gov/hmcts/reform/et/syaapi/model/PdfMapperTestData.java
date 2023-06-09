package uk.gov.hmcts.reform.et.syaapi.model;

import lombok.Data;
import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.SWIFT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TAYLOR;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_ACAS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_COMPANY_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TRUE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.VIDEO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_ADDRESS_LINE_1;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_ADDRESS_LINE_2;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_ADDRESS_LINE_3;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_COUNTRY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_COUNTY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_POSTCODE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WORK_POST_TOWN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

@Data
public final class PdfMapperTestData {

    private PdfMapperTestData() {
        // Utility classes should not have a public or default constructor.
    }

    public static Stream<Arguments> generateRepresentativeClaimantTypes() {
        Address representativeAddress = TestUtil.generateAddressByAddressFields(
            TestConstants.ADDRESS_LINE_1,
            TestConstants.ADDRESS_LINE_2,
            TestConstants.ADDRESS_LINE_3,
            TestConstants.POST_TOWN,
            TestConstants.COUNTY,
            TestConstants.COUNTRY,
            TestConstants.POSTCODE);
        RepresentedTypeC representativeClaimantTypeAllFilled = TestUtil.generateRepresentativeClaimantType(
            representativeAddress, TestConstants.REPRESENTATIVE_NAME, TestConstants.REPRESENTATIVE_REFERENCE,
            TestConstants.REPRESENTATIVE_ORGANISATION, TestConstants.REPRESENTATIVE_PHONE_NUMBER,
            TestConstants.REPRESENTATIVE_MOBILE_NUMBER, TestConstants.REPRESENTATIVE_EMAIL, TestConstants.EMAIL
        );
        RepresentedTypeC representativeClaimantTypeAddressNull = TestUtil.generateRepresentativeClaimantType(
            null, TestConstants.REPRESENTATIVE_NAME, TestConstants.REPRESENTATIVE_REFERENCE,
            TestConstants.REPRESENTATIVE_ORGANISATION, TestConstants.REPRESENTATIVE_PHONE_NUMBER,
            TestConstants.REPRESENTATIVE_MOBILE_NUMBER,TestConstants.REPRESENTATIVE_EMAIL, TestConstants.EMAIL
        );
        RepresentedTypeC representativeClaimantPreferenceNull = TestUtil.generateRepresentativeClaimantType(
            representativeAddress, TestConstants.REPRESENTATIVE_NAME, TestConstants.REPRESENTATIVE_REFERENCE,
            TestConstants.REPRESENTATIVE_ORGANISATION, TestConstants.REPRESENTATIVE_PHONE_NUMBER,
            TestConstants.REPRESENTATIVE_MOBILE_NUMBER, TestConstants.REPRESENTATIVE_EMAIL, TestConstants.NULL_STRING
        );
        RepresentedTypeC representativeClaimantPreferenceEmpty = TestUtil.generateRepresentativeClaimantType(
            representativeAddress, TestConstants.REPRESENTATIVE_NAME, TestConstants.REPRESENTATIVE_REFERENCE,
            TestConstants.REPRESENTATIVE_ORGANISATION, TestConstants.REPRESENTATIVE_PHONE_NUMBER,
            TestConstants.REPRESENTATIVE_MOBILE_NUMBER, TestConstants.REPRESENTATIVE_EMAIL, TestConstants.EMPTY_STRING
        );
        RepresentedTypeC representativeClaimantPreferenceBlank = TestUtil.generateRepresentativeClaimantType(
            representativeAddress, TestConstants.REPRESENTATIVE_NAME, TestConstants.REPRESENTATIVE_REFERENCE,
            TestConstants.REPRESENTATIVE_ORGANISATION, TestConstants.REPRESENTATIVE_PHONE_NUMBER,
            TestConstants.REPRESENTATIVE_MOBILE_NUMBER, TestConstants.REPRESENTATIVE_EMAIL, TestConstants.BLANK_STRING
        );
        RepresentedTypeC representativeClaimantTypePreferenceFax = TestUtil.generateRepresentativeClaimantType(
            representativeAddress, TestConstants.REPRESENTATIVE_NAME, TestConstants.REPRESENTATIVE_REFERENCE,
            TestConstants.REPRESENTATIVE_ORGANISATION, TestConstants.REPRESENTATIVE_PHONE_NUMBER,
            TestConstants.REPRESENTATIVE_MOBILE_NUMBER, TestConstants.REPRESENTATIVE_EMAIL, TestConstants.FAX
        );
        RepresentedTypeC representativeClaimantTypePreferencePost = TestUtil.generateRepresentativeClaimantType(
            representativeAddress, TestConstants.REPRESENTATIVE_NAME, TestConstants.REPRESENTATIVE_REFERENCE,
            TestConstants.REPRESENTATIVE_ORGANISATION, TestConstants.REPRESENTATIVE_PHONE_NUMBER,
            TestConstants.REPRESENTATIVE_MOBILE_NUMBER, TestConstants.REPRESENTATIVE_EMAIL, TestConstants.POST
        );

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

    public static Stream<Arguments> generateCaseDataSamplesWithRespondentSumTypeItems() {
        Address respondentAddress = TestUtil.generateAddressByAddressFields(TestConstants.ADDRESS_LINE_1,
                                                                            TestConstants.ADDRESS_LINE_2,
                                                                            TestConstants.ADDRESS_LINE_3,
                                                                            TestConstants.POST_TOWN,
                                                                            TestConstants.COUNTY,
                                                                            TestConstants.COUNTRY,
                                                                            TestConstants.POSTCODE);
        ////// CASE DATA 1
        CaseData caseData1 = TestUtil.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_ONE, YES,
                                                                    TestConstants.NULL_ADDRESS);
        RespondentSumTypeItem respondentSumTypeItem = TestUtil.generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_ONE, TEST_COMPANY_NAME, respondentAddress, YES, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_UNFAIR_DISMISSAL);
        List<RespondentSumTypeItem> respondentCollection = new ArrayList<>();
        respondentCollection.add(respondentSumTypeItem);
        caseData1.setRespondentCollection(respondentCollection);
        ////// CASE DATA 2
        RespondentSumTypeItem respondentSumTypeItem2 = TestUtil.generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_TWO, TEST_COMPANY_NAME, respondentAddress, YES, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_ANOTHER_PERSON);
        RespondentSumTypeItem respondentSumTypeItem3 = TestUtil.generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_THREE, TEST_COMPANY_NAME, respondentAddress, TestConstants.NO, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_NO_POWER);
        RespondentSumTypeItem respondentSumTypeItem4 = TestUtil.generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_FOUR, TEST_COMPANY_NAME, respondentAddress, TestConstants.NO, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_UNFAIR_DISMISSAL);
        RespondentSumTypeItem respondentSumTypeItem5 = TestUtil.generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_FIVE, TEST_COMPANY_NAME, respondentAddress, TestConstants.NO, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_ANOTHER_PERSON);
        List<RespondentSumTypeItem> respondentCollection2 = new ArrayList<>();
        respondentCollection2.add(respondentSumTypeItem);
        respondentCollection2.add(respondentSumTypeItem2);
        respondentCollection2.add(respondentSumTypeItem3);
        respondentCollection2.add(respondentSumTypeItem4);
        respondentCollection2.add(respondentSumTypeItem5);
        Address workAddress = TestUtil.generateAddressByAddressFields(WORK_ADDRESS_LINE_1, WORK_ADDRESS_LINE_2,
                                                                      WORK_ADDRESS_LINE_3, WORK_POST_TOWN, WORK_COUNTY,
                                                                      WORK_COUNTRY, WORK_POSTCODE);
        CaseData caseData2 = TestUtil.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_TWO,
                                                                    TestConstants.NO,
                                                                    workAddress);
        caseData2.setRespondentCollection(respondentCollection2);
        ////// CASE DATA 3
        CaseData caseData3 = TestUtil.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_THREE,
                                                                    YES,
                                                                    TestConstants.NULL_ADDRESS);
        RespondentSumTypeItem respondentSumTypeItem6 = TestUtil.generateRespondentSumTypeItem(
            TestConstants.STRING_NUMERIC_TWO, TEST_COMPANY_NAME, respondentAddress, YES, TEST_ACAS,
            PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_EMPLOYER_ALREADY_IN_TOUCH);

        List<RespondentSumTypeItem> respondentCollection3 = new ArrayList<>();
        respondentCollection3.add(respondentSumTypeItem);
        respondentCollection3.add(respondentSumTypeItem6);
        caseData3.setRespondentCollection(respondentCollection3);

        ////// CASE DATA 4
        CaseData caseData4 = TestUtil.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_FOUR, YES,
                                                                    TestConstants.NULL_ADDRESS);

        return Stream.of(Arguments.of(caseData1),
                         Arguments.of(caseData2),
                         Arguments.of(caseData3),
                         Arguments.of(caseData4));
    }

    public static Stream<Arguments> generateCaseDataSamplesWithHearingPreferences() {
        // Empty Reasonable Adjustments
        CaseData caseDataEmptyReasonableAdjustment = new CaseData();
        caseDataEmptyReasonableAdjustment.setClaimantHearingPreference(
            TestUtil.generateClaimantHearingPreference(TRUE, TestConstants.NULL_STRING, TestConstants.NULL_STRING,
                                                       TestConstants.EMPTY_STRING_ARRAY,
                                                       TestConstants.HEARING_ASSISTANCE));
        caseDataEmptyReasonableAdjustment.setEthosCaseReference(TestConstants.STRING_NUMERIC_ONE);
        // Yes Reasonable Adjustments
        CaseData caseDataYesReasonableAdjustments = new CaseData();
        caseDataYesReasonableAdjustments.setClaimantHearingPreference(
            TestUtil.generateClaimantHearingPreference(TestConstants.FALSE, YES,
                                                       TestConstants.REASONABLE_ADJUSTMENT_DETAILS,
                                                       TestConstants.EMPTY_STRING_ARRAY,
                                                       TestConstants.HEARING_ASSISTANCE));
        caseDataYesReasonableAdjustments.setEthosCaseReference(TestConstants.STRING_NUMERIC_TWO);
        // No Reasonable Adjustments
        CaseData caseDataNoReasonableAdjustments = new CaseData();
        caseDataNoReasonableAdjustments.setClaimantHearingPreference(
            TestUtil.generateClaimantHearingPreference(TestConstants.FALSE, TestConstants.NO, TestConstants.NULL_STRING,
                                                       TestConstants.EMPTY_STRING_ARRAY,
                                                       TestConstants.HEARING_ASSISTANCE));
        caseDataNoReasonableAdjustments.setEthosCaseReference(TestConstants.STRING_NUMERIC_THREE);
        // No Hearing Preference Selected
        CaseData caseDataNoHearingPreferenceSelected = new CaseData();
        caseDataNoHearingPreferenceSelected.setClaimantHearingPreference(
            TestUtil.generateClaimantHearingPreference(TestConstants.FALSE, YES,
                                                       TestConstants.REASONABLE_ADJUSTMENT_DETAILS,
                                                       TestConstants.EMPTY_STRING_ARRAY,
                                                       TestConstants.HEARING_ASSISTANCE));
        caseDataNoHearingPreferenceSelected.setEthosCaseReference(TestConstants.STRING_NUMERIC_FOUR);
        // No Hearing Preference Selected
        CaseData caseDataHearingPreferenceVideoPhoneSelected = new CaseData();
        caseDataHearingPreferenceVideoPhoneSelected.setClaimantHearingPreference(
            TestUtil.generateClaimantHearingPreference(TestConstants.FALSE, YES,
                                                       TestConstants.REASONABLE_ADJUSTMENT_DETAILS,
                                                       new String[]{VIDEO, TestConstants.PHONE},
                                                       TestConstants.HEARING_ASSISTANCE));
        caseDataHearingPreferenceVideoPhoneSelected.setEthosCaseReference(TestConstants.STRING_NUMERIC_FIVE);
        // Invalid Hearing Preference Selected
        CaseData caseDataInvalidHearingPreferenceSelected = new CaseData();
        caseDataInvalidHearingPreferenceSelected.setClaimantHearingPreference(
            TestUtil.generateClaimantHearingPreference(TestConstants.FALSE, YES,
                                                       TestConstants.REASONABLE_ADJUSTMENT_DETAILS,
                                                       new String[]{"Dummy", "String"},
                                                       TestConstants.HEARING_ASSISTANCE
            ));
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
        ClaimantIndType claimantIndTypeOtherTitleMaleNotNullDateOfBirth = TestUtil.generateClaimantIndType(
            TestConstants.OTHER, TestConstants.OTHER_TITLE, TestConstants.MICHAEL, TestConstants.MERCURY,
            "1979-05-08", TestConstants.MALE
        );

        ClaimantIndType claimantIndTypeTitleMrMaleNotNullDateOfBirth = TestUtil.generateClaimantIndType(
            TestConstants.MR, TestConstants.NULL_STRING, TestConstants.MICHAEL, TestConstants.MERCURY,
            "1980-06-09", TestConstants.MALE
        );


        ClaimantIndType claimantIndTypeTitleMsFemaleNotNullDateOfBirth = TestUtil.generateClaimantIndType(
            TestConstants.MS, TestConstants.NULL_STRING, TestConstants.ELIZABETH, TAYLOR, "1981-07-10",
            TestConstants.FEMALE
        );

        ClaimantIndType claimantIndTypeTitleMrsFemaleNotNullDateOfBirth = TestUtil.generateClaimantIndType(
            TestConstants.MRS, TestConstants.NULL_STRING, TAYLOR, SWIFT, "1982-08-11", TestConstants.FEMALE
        );

        ClaimantIndType claimantIndTypeTitleMissFemaleNotNullDateOfBirth = TestUtil.generateClaimantIndType(
            TestConstants.MISS, TestConstants.NULL_STRING, TAYLOR, SWIFT, "1983-09-12", TestConstants.FEMALE
        );

        ClaimantIndType claimantIndTypeOtherTitleMaleNullDateOfBirth = TestUtil.generateClaimantIndType(
            TestConstants.OTHER, TestConstants.OTHER_TITLE, TestConstants.MICHAEL,
            TestConstants.MERCURY, TestConstants.NULL_STRING, TestConstants.MALE
        );

        ClaimantIndType claimantIndTypeOtherTitleMaleEmptyDateOfBirth = TestUtil.generateClaimantIndType(
            TestConstants.OTHER, TestConstants.OTHER_TITLE, TestConstants.MICHAEL,
            TestConstants.MERCURY, TestConstants.EMPTY_STRING, TestConstants.MALE
        );
        ClaimantIndType claimantIndTypeOtherTitleMaleBlankDateOfBirth = TestUtil.generateClaimantIndType(
            TestConstants.OTHER, TestConstants.OTHER_TITLE, TestConstants.MICHAEL,
            TestConstants.MERCURY, TestConstants.BLANK_STRING, TestConstants.MALE
        );


        ClaimantIndType claimantIndTypeOtherTitlePreferNotToSay = TestUtil.generateClaimantIndType(
            TestConstants.OTHER, TestConstants.OTHER_TITLE, TestConstants.MICHAEL,
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
        Address claimantAddressUK = TestUtil.generateAddressByAddressFields(TestConstants.ADDRESS_LINE_1,
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

}
