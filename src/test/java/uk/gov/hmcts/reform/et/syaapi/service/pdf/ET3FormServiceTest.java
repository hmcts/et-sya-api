package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ecm.common.service.pdf.PdfService;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.DocumentCategoryConstants.ET3_PDF_DOC_CATEGORY;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.ET3_FORM_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.ET3FormService.getRespondentNameBySelectedRespondent;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ET3FormServiceTest {

    private ET3FormService et3FormService;
    private CaseTestData caseTestData;
    private static final String TEST_RESPONDENT_NAME = "Test Respondent Name";
    private static final String TEST_RESPONDENT_ORGANISATION = "Test Respondent Organisation";
    private static final String TEST_RESPONDENT_GIVEN_NAME = "Test Respondent Given Name";
    private static final String TEST_RESPONDENT_FAMILY_NAME = "Test Respondent Family Name";
    private static final String TEST_RESPONDENT_FIRST_NAME = "Test Respondent First Name";
    private static final String TEST_RESPONDENT_LAST_NAME = "Test Respondent Last Name";

    @Mock
    private PdfService pdfService;
    @Mock
    private CaseDocumentService caseDocumentService;
    @Mock
    private IdamClient idamClient;

    @BeforeEach
    @SneakyThrows
    void beforeEach() {
        et3FormService = new ET3FormService(pdfService, caseDocumentService, idamClient);
        caseTestData = new CaseTestData();
    }


    @Test
    @SneakyThrows
    void theGenerateET3WelshAndEnglishForms() {
        CaseData caseData =  caseTestData.getCaseData();
        when(caseDocumentService.createDocumentTypeItem(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(caseData.getEcmCaseType()),
            eq(ET3_FORM_DOCUMENT_TYPE),
            eq(ET3_PDF_DOC_CATEGORY),
            any())).thenReturn(caseTestData.getUploadDocumentResponse().get(0));

        et3FormService.generateET3WelshAndEnglishForms(TEST_SERVICE_AUTH_TOKEN,
                                                       caseData,
                                                       caseData.getRespondentCollection().get(0));
        assertThat(caseData.getRespondentCollection().get(0).getValue().getEt3Form().getDocumentFilename()).isEqualTo(
            caseTestData.getUploadDocumentResponse().get(0).getValue().getUploadedDocument().getDocumentFilename());
    }

    @ParameterizedTest
    @MethodSource("provideGetRespondentNameBySelectedRespondentTestData")
    void theGetRespondentNameBySelectedRespondent(RespondentSumTypeItem selectedRespondent, UserInfo userInfo) {
        if (ObjectUtils.isEmpty(selectedRespondent)) {
            assertThat(getRespondentNameBySelectedRespondent(selectedRespondent, userInfo))
                .isEqualTo(StringUtils.EMPTY);
            return;
        }
        if (ObjectUtils.isEmpty(selectedRespondent.getValue())) {
            assertThat(getRespondentNameBySelectedRespondent(selectedRespondent, userInfo))
                .isEqualTo(StringUtils.EMPTY);
            return;
        }
        if (StringUtils.isNotBlank(selectedRespondent.getValue().getResponseRespondentName())) {
            assertThat(getRespondentNameBySelectedRespondent(selectedRespondent, userInfo))
                .isEqualTo(TEST_RESPONDENT_NAME);
            return;
        }
        if (StringUtils.isNotBlank(selectedRespondent.getValue().getRespondentOrganisation())) {
            assertThat(getRespondentNameBySelectedRespondent(selectedRespondent, userInfo))
                .isEqualTo(TEST_RESPONDENT_ORGANISATION);
            return;
        }
        if (ObjectUtils.isNotEmpty(userInfo) && StringUtils.isNotBlank(userInfo.getGivenName())
            && StringUtils.isBlank(userInfo.getFamilyName())) {
            assertThat(getRespondentNameBySelectedRespondent(selectedRespondent, userInfo))
                .isEqualTo(TEST_RESPONDENT_GIVEN_NAME);
            return;
        }
        checkRestRespondentNames1(selectedRespondent, userInfo);
    }

    private static void checkRestRespondentNames1(RespondentSumTypeItem selectedRespondent, UserInfo userInfo) {
        if (ObjectUtils.isNotEmpty(userInfo) && StringUtils.isBlank(userInfo.getGivenName())
            && StringUtils.isNotBlank(userInfo.getFamilyName())) {
            assertThat(getRespondentNameBySelectedRespondent(selectedRespondent, userInfo))
                .isEqualTo(TEST_RESPONDENT_FAMILY_NAME);
            return;
        }
        if (ObjectUtils.isNotEmpty(userInfo) && StringUtils.isNotBlank(userInfo.getGivenName())
            && StringUtils.isNotBlank(userInfo.getFamilyName())) {
            assertThat(getRespondentNameBySelectedRespondent(selectedRespondent, userInfo))
                .isEqualTo(TEST_RESPONDENT_GIVEN_NAME + StringUtils.SPACE + TEST_RESPONDENT_FAMILY_NAME);
            return;
        }
        if (StringUtils.isNotBlank(selectedRespondent.getValue().getRespondentFirstName())
            && StringUtils.isBlank(selectedRespondent.getValue().getRespondentLastName())) {
            assertThat(getRespondentNameBySelectedRespondent(selectedRespondent, userInfo))
                .isEqualTo(TEST_RESPONDENT_FIRST_NAME);
            return;
        }
        checkRestRespondentNames2(selectedRespondent, userInfo);
    }

    private static void checkRestRespondentNames2(RespondentSumTypeItem selectedRespondent, UserInfo userInfo) {
        if (StringUtils.isBlank(selectedRespondent.getValue().getRespondentFirstName())
            && StringUtils.isNotBlank(selectedRespondent.getValue().getRespondentLastName())) {
            assertThat(getRespondentNameBySelectedRespondent(selectedRespondent, userInfo))
                .isEqualTo(TEST_RESPONDENT_LAST_NAME);
            return;
        }
        if (StringUtils.isNotBlank(selectedRespondent.getValue().getRespondentFirstName())
            && StringUtils.isNotBlank(selectedRespondent.getValue().getRespondentLastName())) {
            assertThat(getRespondentNameBySelectedRespondent(selectedRespondent, userInfo))
                .isEqualTo(TEST_RESPONDENT_FIRST_NAME + StringUtils.SPACE + TEST_RESPONDENT_LAST_NAME);
        }
    }


    private static Stream<Arguments> provideGetRespondentNameBySelectedRespondentTestData() {
        RespondentSumTypeItem selectedRespondentWithEmptyValue = new CaseTestData().getEt3Request().getRespondent();
        selectedRespondentWithEmptyValue.setValue(null);
        RespondentSumTypeItem selectedRespondentWithRespondentName = new CaseTestData().getEt3Request().getRespondent();
        selectedRespondentWithRespondentName.getValue().setResponseRespondentName(TEST_RESPONDENT_NAME);
        RespondentSumTypeItem selectedRespondentWithRespondentOrganisation =
            new CaseTestData().getEt3Request().getRespondent();
        selectedRespondentWithRespondentOrganisation.getValue().setResponseRespondentName(StringUtils.EMPTY);
        selectedRespondentWithRespondentOrganisation.getValue().setRespondentOrganisation(TEST_RESPONDENT_ORGANISATION);
        RespondentSumTypeItem selectedRespondentWithoutRespondentAndOrganisationNames =
            new CaseTestData().getEt3Request().getRespondent();
        selectedRespondentWithoutRespondentAndOrganisationNames.getValue().setResponseRespondentName(StringUtils.EMPTY);
        selectedRespondentWithoutRespondentAndOrganisationNames.getValue().setRespondentOrganisation(StringUtils.EMPTY);
        RespondentSumTypeItem selectedRespondentWithoutRespondentAndOrganisationNamesWithFirstName =
            new CaseTestData().getEt3Request().getRespondent();
        selectedRespondentWithoutRespondentAndOrganisationNamesWithFirstName
            .getValue().setResponseRespondentName(StringUtils.EMPTY);
        selectedRespondentWithoutRespondentAndOrganisationNamesWithFirstName
            .getValue().setRespondentOrganisation(StringUtils.EMPTY);
        selectedRespondentWithoutRespondentAndOrganisationNamesWithFirstName
            .getValue().setRespondentLastName(StringUtils.EMPTY);
        selectedRespondentWithoutRespondentAndOrganisationNamesWithFirstName
            .getValue().setRespondentFirstName(TEST_RESPONDENT_FIRST_NAME);
        RespondentSumTypeItem selectedRespondentWithoutRespondentAndOrganisationNamesWithLastName =
            new CaseTestData().getEt3Request().getRespondent();
        selectedRespondentWithoutRespondentAndOrganisationNamesWithLastName
            .getValue().setResponseRespondentName(StringUtils.EMPTY);
        selectedRespondentWithoutRespondentAndOrganisationNamesWithLastName
            .getValue().setRespondentOrganisation(StringUtils.EMPTY);
        selectedRespondentWithoutRespondentAndOrganisationNamesWithLastName
            .getValue().setRespondentFirstName(StringUtils.EMPTY);
        selectedRespondentWithoutRespondentAndOrganisationNamesWithLastName
            .getValue().setRespondentLastName(TEST_RESPONDENT_LAST_NAME);
        RespondentSumTypeItem selectedRespondentWithoutRespondentAndOrganisationNamesWithFirstAndLastNames =
            new CaseTestData().getEt3Request().getRespondent();
        selectedRespondentWithoutRespondentAndOrganisationNamesWithFirstAndLastNames
            .getValue().setResponseRespondentName(StringUtils.EMPTY);
        selectedRespondentWithoutRespondentAndOrganisationNamesWithFirstAndLastNames
            .getValue().setRespondentOrganisation(StringUtils.EMPTY);
        selectedRespondentWithoutRespondentAndOrganisationNamesWithFirstAndLastNames
            .getValue().setRespondentFirstName(TEST_RESPONDENT_FIRST_NAME);
        selectedRespondentWithoutRespondentAndOrganisationNamesWithFirstAndLastNames
            .getValue().setRespondentLastName(TEST_RESPONDENT_LAST_NAME);
        UserInfo userInfoWithGivenName = UserInfo.builder().givenName(TEST_RESPONDENT_GIVEN_NAME).build();
        UserInfo userInfoWithFamilyName = UserInfo.builder().familyName(TEST_RESPONDENT_FAMILY_NAME).build();
        UserInfo userInfoWithGivenAndFamilyNames = UserInfo.builder()
            .givenName(TEST_RESPONDENT_GIVEN_NAME).familyName(TEST_RESPONDENT_FAMILY_NAME).build();
        return Stream.of(Arguments.of(null, null),
                         Arguments.of(selectedRespondentWithEmptyValue, null),
                         Arguments.of(selectedRespondentWithRespondentName, null),
                         Arguments.of(selectedRespondentWithRespondentOrganisation, null),
                         Arguments.of(selectedRespondentWithoutRespondentAndOrganisationNames,userInfoWithGivenName),
                         Arguments.of(selectedRespondentWithoutRespondentAndOrganisationNames, userInfoWithFamilyName),
                         Arguments.of(
                             selectedRespondentWithoutRespondentAndOrganisationNames, userInfoWithGivenAndFamilyNames),
                         Arguments.of(
                             selectedRespondentWithoutRespondentAndOrganisationNamesWithFirstName, null),
                         Arguments.of(
                             selectedRespondentWithoutRespondentAndOrganisationNamesWithLastName, null),
                         Arguments.of(
                             selectedRespondentWithoutRespondentAndOrganisationNamesWithFirstAndLastNames, null)
        );
    }
}
