package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.model.PdfMapperTestData;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.NO_LOWERCASE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.PHONE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.VIDEO;

class PdfMapperHearingPreferencesUtilTest {

    @ParameterizedTest
    @NullSource
    @MethodSource("retrieveCaseDataSamplesWithHearingPreferences")
    void putHearingPreferences(CaseData caseData) {
        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        PdfMapperHearingPreferencesUtil.putHearingPreferences(caseData, printFields);
        if (ObjectUtils.isEmpty(caseData)) {
            assertThat(printFields.get(PdfMapperConstants.I_CAN_TAKE_PART_IN_NO_HEARINGS)).isNull();
            assertThat(printFields.get(PdfMapperConstants.I_CAN_TAKE_PART_IN_NO_HEARINGS_EXPLAIN)).isNull();
            assertThat(printFields.get(PdfMapperConstants.I_CAN_TAKE_PART_IN_PHONE_HEARINGS)).isNull();
            assertThat(printFields.get(PdfMapperConstants.I_CAN_TAKE_PART_IN_VIDEO_HEARINGS)).isNull();
        } else {
            if (!ObjectUtils.isEmpty(caseData.getClaimantHearingPreference())
                && !StringUtils.isEmpty(caseData.getClaimantHearingPreference().getReasonableAdjustments())) {
                checkReasonableAdjustments(caseData, printFields);
            }
            checkHearingPreferences(caseData, printFields);
        }
    }

    private static void checkReasonableAdjustments(CaseData caseData,
                                                   ConcurrentMap<String, Optional<String>> printFields) {
        if (PdfMapperServiceUtil.isYes(caseData.getClaimantHearingPreference().getReasonableAdjustments())) {
            assertThat(printFields.get(PdfMapperConstants.Q12_DISABILITY_YES)).contains(YES);
        } else {
            assertThat(printFields.get(PdfMapperConstants.Q12_DISABILITY_NO)).contains(NO_LOWERCASE);
        }

        if (!StringUtils.isEmpty(caseData.getClaimantHearingPreference().getReasonableAdjustmentsDetail())) {
            assertThat(printFields.get(PdfMapperConstants.Q12_DISABILITY_DETAILS)).contains(
                caseData.getClaimantHearingPreference().getReasonableAdjustmentsDetail());
        }
    }

    private static void checkHearingPreferences(CaseData caseData,
                                                ConcurrentMap<String, Optional<String>> printFields) {
        if (ObjectUtils.isEmpty(caseData.getClaimantHearingPreference())
            || ObjectUtils.isEmpty(caseData.getClaimantHearingPreference().getHearingPreferences())
            || !caseData.getClaimantHearingPreference().getHearingPreferences().contains(VIDEO)
            && !caseData.getClaimantHearingPreference().getHearingPreferences().contains(PHONE)) {
            checkHearingPreferencesNotEntered(caseData, printFields);
        } else {
            if (!ObjectUtils.isEmpty(caseData.getClaimantHearingPreference())
                && !ObjectUtils.isEmpty(caseData.getClaimantHearingPreference().getHearingPreferences())) {
                checkHearingPreferencesEntered(caseData, printFields);
            }
        }
    }

    private static void checkHearingPreferencesNotEntered(CaseData caseData,
                                                          ConcurrentMap<String, Optional<String>> printFields) {
        assertThat(printFields.get(PdfMapperConstants.I_CAN_TAKE_PART_IN_NO_HEARINGS)).contains(YES);
        if (!ObjectUtils.isEmpty(caseData.getClaimantHearingPreference())
            && StringUtils.isNotBlank(caseData.getClaimantHearingPreference().getHearingAssistance())) {
            assertThat(printFields.get(PdfMapperConstants.I_CAN_TAKE_PART_IN_NO_HEARINGS_EXPLAIN))
                .contains(caseData.getClaimantHearingPreference().getHearingAssistance());
        }
    }

    private static void checkHearingPreferencesEntered(CaseData caseData,
                                                       ConcurrentMap<String, Optional<String>> printFields) {
        if (caseData.getClaimantHearingPreference().getHearingPreferences().contains(VIDEO)) {
            assertThat(printFields.get(PdfMapperConstants.I_CAN_TAKE_PART_IN_VIDEO_HEARINGS)).contains(YES);
        }
        if (caseData.getClaimantHearingPreference().getHearingPreferences().contains(PHONE)) {
            assertThat(printFields.get(PdfMapperConstants.I_CAN_TAKE_PART_IN_PHONE_HEARINGS)).contains(YES);
        }
    }


    private static Stream<Arguments> retrieveCaseDataSamplesWithHearingPreferences() {
        return PdfMapperTestData.generateCaseDataSamplesWithHearingPreferences();
    }
}
