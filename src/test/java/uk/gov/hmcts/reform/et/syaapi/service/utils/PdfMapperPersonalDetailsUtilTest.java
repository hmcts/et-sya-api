package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.MISS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.MR;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.MRS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.MS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.OTHER;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.OTHER_SPECIFY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_FEMALE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_MALE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_PREFER_NOT_TO_SAY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.TITLES;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.TITLE_MAP;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.YES;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_CONTACT_EMAIL_FIELD_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_EMAIL_FIELD_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_MOBILE_NUMBER_FIELD_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_PHONE_NUMBER_FIELD_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.EMAIL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.OTHER_TITLE;

class PdfMapperPersonalDetailsUtilTest {

    private CaseData caseData;

    @BeforeEach
    void beforeEach() {
        caseData = new CaseData();
        caseData.setEthosCaseReference("1234567890");
    }

    @Test
    void logExceptionWhenCaseDataIsNull() {
        try (MockedStatic<GenericServiceUtil> mockedGenericServiceUtil = Mockito.mockStatic(GenericServiceUtil.class)) {
            ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
            PdfMapperPersonalDetailsUtil.putPersonalDetails(null, printFields);

            mockedGenericServiceUtil.verify(
                () -> GenericServiceUtil.logException(anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString()),
                times(1)
            );
        }
    }

    @Test
    void putNothingWhenClaimantIndTypeIsNull() {
        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        PdfMapperPersonalDetailsUtil.putPersonalDetails(new CaseData(), printFields);
        assertThat(printFields).isEmpty();
    }

    @Test
    void putNothingToTitleWhenClaimantIndTypeIsEmpty() {
        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();

        PdfMapperPersonalDetailsUtil.putPersonalDetails(caseData, printFields);
        assertThat(printFields.get(TITLES.get(MR))).isNull();
        assertThat(printFields.get(TITLES.get(MRS))).isNull();
        assertThat(printFields.get(TITLES.get(MISS))).isNull();
        assertThat(printFields.get(TITLES.get(MS))).isNull();
        assertThat(printFields.get(TITLES.get(OTHER))).isNull();
        assertThat(printFields.get(TITLES.get(OTHER_SPECIFY))).isNull();

    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.et.syaapi.model.PdfMapperTestData#generateClaimantIndTypeArguments")
    void putClaimantIndTypeValues(ClaimantIndType claimantIndType,
                                                      String dobDay, String dobMonth, String dobYear) {
        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        caseData.setClaimantIndType(claimantIndType);
        PdfMapperPersonalDetailsUtil.putPersonalDetails(caseData, printFields);
        if (StringUtils.isNotBlank(caseData.getClaimantIndType().getClaimantPreferredTitle())) {
            assertThat(printFields.get(TITLES.get(
                caseData.getClaimantIndType().getClaimantPreferredTitle()))).contains(TITLE_MAP.get(
                    caseData.getClaimantIndType().getClaimantPreferredTitle()));
        }
        if (OTHER.equals(caseData.getClaimantIndType().getClaimantPreferredTitle())) {
            assertThat(printFields.get(TITLES.get(
                OTHER_SPECIFY))).contains(OTHER_TITLE);
        }
        assertThat(printFields.get(PdfMapperConstants.Q1_FIRST_NAME))
            .contains(caseData.getClaimantIndType().getClaimantFirstNames());
        assertThat(printFields.get(PdfMapperConstants.Q1_SURNAME))
            .contains(caseData.getClaimantIndType().getClaimantLastName());
        if (StringUtils.isNotBlank(claimantIndType.getClaimantDateOfBirth())) {
            assertThat(printFields.get(PdfMapperConstants.Q1_DOB_DAY)).contains(dobDay);
            assertThat(printFields.get(PdfMapperConstants.Q1_DOB_MONTH)).contains(dobMonth);
            assertThat(printFields.get(PdfMapperConstants.Q1_DOB_YEAR)).contains(dobYear);
        }
        if (SEX_MALE.equals(caseData.getClaimantIndType().getClaimantSex())) {
            assertThat(printFields.get(PdfMapperConstants.Q1_SEX_MALE)).contains(YES);
        }
        if (SEX_FEMALE.equals(caseData.getClaimantIndType().getClaimantSex())) {
            assertThat(printFields.get(PdfMapperConstants.Q1_SEX_FEMALE)).contains(
                PdfMapperConstants.SEX_FEMALE_LOWERCASE);
        }
        if (SEX_PREFER_NOT_TO_SAY.equals(caseData.getClaimantIndType().getClaimantSex())) {
            assertThat(printFields.get(PdfMapperConstants.Q1_SEX_PREFER_NOT_TO_SAY))
                .contains(PdfMapperConstants.SEX_PREFER_NOT_TO_SAY_LOWERCASE);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"dummy", "sex", "test dummy sex", "not male, female or prefer not to say"})
    void logExceptionWhenSexIsNotEntered(String claimantSex) {
        try (MockedStatic<GenericServiceUtil> mockedGenericServiceUtil = Mockito.mockStatic(GenericServiceUtil.class)) {
            ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
            ClaimantIndType claimantIndTypeSexNotEntered = new ClaimantIndType();
            claimantIndTypeSexNotEntered.setClaimantSex(claimantSex);
            caseData.setClaimantIndType(claimantIndTypeSexNotEntered);
            PdfMapperPersonalDetailsUtil.putPersonalDetails(caseData, printFields);

            mockedGenericServiceUtil.verify(
                () -> GenericServiceUtil.logException(anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString(),
                                                      anyString()),
                times(1)
            );
        }
    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.et.syaapi.model.PdfMapperTestData#generateClaimantTypeArguments")
    void putClaimantTypeValues(ClaimantType claimantType) {
        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        caseData.setClaimantType(claimantType);
        PdfMapperPersonalDetailsUtil.putPersonalDetails(caseData, printFields);
        if (StringUtils.isNotBlank(caseData.getClaimantType().getClaimantPhoneNumber())) {
            assertThat(printFields.get(CLAIMANT_PHONE_NUMBER_FIELD_NAME))
                .contains(caseData.getClaimantType().getClaimantPhoneNumber());
        }
        if (StringUtils.isNotBlank(caseData.getClaimantType().getClaimantMobileNumber())) {
            assertThat(printFields.get(CLAIMANT_MOBILE_NUMBER_FIELD_NAME))
                .contains(caseData.getClaimantType().getClaimantPhoneNumber());
        }
        if (StringUtils.isNotBlank(caseData.getClaimantType().getClaimantEmailAddress())) {
            assertThat(printFields.get(CLAIMANT_EMAIL_FIELD_NAME))
                .contains(caseData.getClaimantType().getClaimantEmailAddress());
        }
        String contactPreference = caseData.getClaimantType().getClaimantContactPreference();
        if (EMAIL.equals(contactPreference)) {
            assertThat(printFields.get(CLAIMANT_CONTACT_EMAIL_FIELD_NAME)).contains(contactPreference);
        }
    }
}
