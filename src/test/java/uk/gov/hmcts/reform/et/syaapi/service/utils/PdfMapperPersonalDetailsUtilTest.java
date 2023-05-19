package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperService.EMAIL;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperService.POST;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_CONTACT_EMAIL_FIELD_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_CONTACT_POST_FIELD_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_EMAIL_FIELD_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_MOBILE_NUMBER_FIELD_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CLAIMANT_PHONE_NUMBER_FIELD_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.OTHER_TITLE;

@SuppressWarnings({"PMD.UseConcurrentHashMap"})
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
        assertThat(printFields.get(PdfMapperConstants.TITLES.get(PdfMapperConstants.MR))).isNull();
        assertThat(printFields.get(PdfMapperConstants.TITLES.get(PdfMapperConstants.MRS))).isNull();
        assertThat(printFields.get(PdfMapperConstants.TITLES.get(PdfMapperConstants.MISS))).isNull();
        assertThat(printFields.get(PdfMapperConstants.TITLES.get(PdfMapperConstants.MS))).isNull();
        assertThat(printFields.get(PdfMapperConstants.TITLES.get(PdfMapperConstants.OTHER))).isNull();
        assertThat(printFields.get(PdfMapperConstants.TITLES.get(PdfMapperConstants.OTHER_SPECIFY))).isNull();

    }

    @ParameterizedTest
    @MethodSource("retrieveClaimantIndTypeArguments")
    void putClaimantIndTypeValues(ClaimantIndType claimantIndType,
                                                      String dobDay, String dobMonth, String dobYear) {
        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        caseData.setClaimantIndType(claimantIndType);
        PdfMapperPersonalDetailsUtil.putPersonalDetails(caseData, printFields);
        if (StringUtils.isNotBlank(caseData.getClaimantIndType().getClaimantPreferredTitle())) {
            assertThat(printFields.get(PdfMapperConstants.TITLES.get(
                caseData.getClaimantIndType().getClaimantPreferredTitle()))).contains(PdfMapperConstants.TITLE_MAP.get(
                    caseData.getClaimantIndType().getClaimantPreferredTitle()));
        }
        if (PdfMapperConstants.OTHER.equals(caseData.getClaimantIndType().getClaimantPreferredTitle())) {
            assertThat(printFields.get(PdfMapperConstants.TITLES.get(
                PdfMapperConstants.OTHER_SPECIFY))).contains(OTHER_TITLE);
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
        if (PdfMapperConstants.SEX_MALE.equals(caseData.getClaimantIndType().getClaimantSex())) {
            assertThat(printFields.get(PdfMapperConstants.Q1_SEX_MALE)).contains(YES);
        }
        if (PdfMapperConstants.SEX_FEMALE.equals(caseData.getClaimantIndType().getClaimantSex())) {
            assertThat(printFields.get(PdfMapperConstants.Q1_SEX_FEMALE)).contains(
                PdfMapperConstants.SEX_FEMALE_LOWERCASE);
        }
        if (PdfMapperConstants.SEX_PREFER_NOT_TO_SAY.equals(caseData.getClaimantIndType().getClaimantSex())) {
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
    @MethodSource("retrieveClaimantTypeArguments")
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
        } else if (POST.equals(contactPreference)) {
            assertThat(printFields.get(CLAIMANT_CONTACT_POST_FIELD_NAME)).contains(contactPreference);
        }
    }

    private static Stream<Arguments> retrieveClaimantIndTypeArguments() {
        return TestData.generateClaimantIndTypeArguments();
    }

    private static Stream<Arguments> retrieveClaimantTypeArguments() {
        return TestData.generateClaimantTypeArguments();
    }
}
