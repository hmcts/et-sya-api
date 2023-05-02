package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.MR;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.MRS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.MISS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.MS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.OTHER;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.OTHER_SPECIFY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.PHONE_NUMBER_PREFIX;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.Q1_DOB_DAY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.Q1_DOB_MONTH;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.Q1_DOB_YEAR;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.Q1_FIRST_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.Q1_SEX_MALE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.Q1_SEX_FEMALE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.Q1_SEX_PREFER_NOT_TO_SAY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.Q1_SURNAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_FEMALE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_FEMALE_LOWERCASE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_MALE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_PREFER_NOT_TO_SAY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_PREFER_NOT_TO_SAY_LOWERCASE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.TITLE_MAP;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.TITLES;

@SuppressWarnings({"PMD.UseConcurrentHashMap"})
class PdfMapperPersonalDetailsUtilTest {

    private CaseData caseData;
    private final static String OTHER_TITLE = "Other Title";

    @BeforeEach
    void beforeEach() {
        caseData = new CaseData();
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
    @MethodSource("retrievePutPersonalDetailsArguments")
    void putAllFieldsWhenClaimantIndTypeValuesEntered(ClaimantIndType claimantIndType,
                                                      String dobDay, String dobMonth, String dobYear) {
        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        caseData.setClaimantIndType(claimantIndType);
        PdfMapperPersonalDetailsUtil.putPersonalDetails(caseData, printFields);
        if(!ObjectUtils.isEmpty(claimantIndType)) {
            if (StringUtils.isNotBlank(caseData.getClaimantIndType().getClaimantPreferredTitle())) {
                assertThat(printFields.get(TITLES.get(caseData.getClaimantIndType().getClaimantPreferredTitle()))).contains(
                    TITLE_MAP.get(
                        caseData.getClaimantIndType().getClaimantPreferredTitle()));
            }
            if (OTHER.equals(caseData.getClaimantIndType().getClaimantPreferredTitle())) {
                assertThat(printFields.get(TITLES.get(OTHER_SPECIFY))).contains(OTHER_TITLE);
            }
            assertThat(printFields.get(Q1_FIRST_NAME)).contains(caseData.getClaimantIndType().getClaimantFirstNames());
            assertThat(printFields.get(Q1_SURNAME)).contains(caseData.getClaimantIndType().getClaimantLastName());
            if (StringUtils.isNotBlank(claimantIndType.getClaimantDateOfBirth())) {
                assertThat(printFields.get(Q1_DOB_DAY)).contains(dobDay);
                assertThat(printFields.get(Q1_DOB_MONTH)).contains(dobMonth);
                assertThat(printFields.get(Q1_DOB_YEAR)).contains(dobYear);
            }
            if (SEX_MALE.equals(caseData.getClaimantIndType().getClaimantSex())) {
                assertThat(printFields.get(Q1_SEX_MALE)).contains(YES);
            }
            if (SEX_FEMALE.equals(caseData.getClaimantIndType().getClaimantSex())) {
                assertThat(printFields.get(Q1_SEX_FEMALE)).contains(SEX_FEMALE_LOWERCASE);
            }
            if (SEX_PREFER_NOT_TO_SAY.equals(caseData.getClaimantIndType().getClaimantSex())) {
                assertThat(printFields.get(Q1_SEX_PREFER_NOT_TO_SAY)).contains(SEX_PREFER_NOT_TO_SAY_LOWERCASE);
            }
        } else {
            assertThat(printFields).isEmpty();
        }
    }

    private static Stream<Arguments> retrievePutPersonalDetailsArguments() {
        return TestData.generatePutPersonalDetailsArguments();
    }
/*
    @Test
    void mapClaimantIndType() {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantPreferredTitle("Other_Specify");
        claimantIndType.setClaimantTitleOther("ABC");
        claimantIndType.setClaimantFirstNames("ClaimantFirstNames");
        claimantIndType.setClaimantLastName("ClaimantLastName");
        claimantIndType.setClaimantDateOfBirth("1976-07-12");
        claimantIndType.setClaimantSex("Female");

        CaseData caseData = new CaseData();
        caseData.setClaimantIndType(claimantIndType);

        assertThat(new PdfMapperPersonalDetailsUtil().mapPersonalDetails(caseData)).containsExactlyInAnyOrderEntriesOf(
            Map.of(
                "1.1 other specify",
                Optional.empty(),
                "1.2 first names",
                Optional.of("ClaimantFirstNames"),
               "1.3 surname",
               Optional.of("ClaimantLastName"),
               "1.4 DOB day",
               Optional.of("12"),
               "1.4 DOB month",
               Optional.of("07"),
               "1.4 DOB year",
               Optional.of("1976"),
               "1.5 sex female",
               Optional.of("female")
            )
        );
    }

    @Test
    void mapClaimantIndTypeWithMaleSex() {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantSex("Male");

        CaseData caseData = new CaseData();
        caseData.setClaimantIndType(claimantIndType);

        Map<String, Optional<String>> mappedDetails = new PdfMapperPersonalDetailsUtil().mapPersonalDetails(caseData);
        assertThat(mappedDetails).containsEntry(
            "1.5 sex",
            Optional.of("Yes")
        );
        assertThat(mappedDetails.values().stream().filter(Optional::isPresent)).hasSize(1);
    }

    @Test
    void mapClaimantIndTypeWithPreferNotToSaySex() {
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantSex("Prefer not to say");

        CaseData caseData = new CaseData();
        caseData.setClaimantIndType(claimantIndType);

        Map<String, Optional<String>> mappedDetails = new PdfMapperPersonalDetailsUtil().mapPersonalDetails(caseData);
        assertThat(mappedDetails).containsEntry(
            "1.5 sex prefer not to say",
            Optional.of("prefer not to say")
        );
        assertThat(mappedDetails.values().stream().filter(Optional::isPresent)).hasSize(1);
    }

    @Test
    void mapClaimantType() {
        Address claimantAddressUK = new Address();
        claimantAddressUK.setAddressLine1("AddressLine1");
        claimantAddressUK.setAddressLine2("AddressLine2");
        claimantAddressUK.setAddressLine3("AddressLine3");
        claimantAddressUK.setPostTown("PostTown");
        claimantAddressUK.setCounty("County");
        claimantAddressUK.setCountry("Country");
        claimantAddressUK.setPostCode("SW1A 1AA");

        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantAddressUK(claimantAddressUK);
        claimantType.setClaimantPhoneNumber("ClaimantPhoneNumber");
        claimantType.setClaimantMobileNumber("ClaimantMobileNumber");
        claimantType.setClaimantEmailAddress("ClaimantEmailAddress");
        claimantType.setClaimantContactPreference("Email");

        CaseData caseData = new CaseData();
        caseData.setClaimantType(claimantType);

        assertThat(new PdfMapperPersonalDetailsUtil().mapPersonalDetails(caseData)).containsExactlyInAnyOrderEntriesOf(
            Map.of(
                Q1_6_CLAIMANT_ADDRESS,
                    Optional.of("Addressline1,\nAddressline2,\nAddressline3,\nPosttown,\nCounty,\nCountry"),
                Q1_6_CLAIMANT_POSTCODE,
                   Optional.of("SW1A 1AA"),
                "1.6 phone number",
                   Optional.of("ClaimantPhoneNumber"),
                "1.7 mobile number",
                   Optional.of("ClaimantMobileNumber"),
                "1.8 How should we contact you - Email",
                Optional.of("Email"),
                "1.9 email",
                Optional.of("ClaimantEmailAddress")
            )
        );
    }

    @Test
    void mapClaimantTypeWithPostPreference() {
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantContactPreference("Post");

        CaseData caseData = new CaseData();
        caseData.setClaimantType(claimantType);

        Map<String, Optional<String>> mappedDetails = new PdfMapperPersonalDetailsUtil().mapPersonalDetails(caseData);
        assertThat(mappedDetails).containsEntry(
            "1.8 How should we contact you - Post",
            Optional.of("Post")
        );
        assertThat(mappedDetails.values().stream().filter(Optional::isPresent)).hasSize(1);
    }

 */
}
