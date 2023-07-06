package uk.gov.hmcts.reform.et.syaapi.service.utils.data;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;

import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TRUE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.VIDEO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

public final class PdfMapperHearingPreferencesUtilTestDataProvider {

    private PdfMapperHearingPreferencesUtilTestDataProvider() {
        // Utility classes should not have a public or default constructor.
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
