package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.enums.RespondentSolicitorType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_ID_LONG;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_USER_ROLE_DEFENDANT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_USER_ROLE_INVALID;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CCD_DATA_STORE_BASE_URL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CLAIMANT_SOLICITOR_IDAM_ID;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.USER_ID;

class ManageCaseRoleServiceUtilTest {

    private static final String AAC_URL_TEST_VALUE = "https://test.url.com";
    private static final String EXPECTED_AAC_URI_WITH_ONLY_CASE_DETAILS =
        "https://test.url.com/case-users?case_ids=1646225213651533&case_ids=1646225213651512";
    private static final String EXPECTED_AAC_URI_WITH_CASE_AND_USER_IDS =
        "https://test.url.com/case-users?case_ids=1646225213651533&case_ids=1646225213651512&"
            + "user_ids=123456789012345678901234567890&user_ids=123456789012345678901234567890";
    private static final String EXCEPTION_INVALID_RESPONDENT_INDEX_EMPTY =
        "java.lang.Exception: Respondent index,  is not valid for the case with id, 1646225213651590";
    private static final String EXCEPTION_INVALID_RESPONDENT_INDEX_NOT_NUMERIC =
        "java.lang.Exception: Respondent index, abc is not valid for the case with id, 1646225213651590";
    private static final String EXCEPTION_INVALID_RESPONDENT_INDEX_NEGATIVE =
        "java.lang.Exception: Respondent index, -1 is not valid for the case with id, 1646225213651590";
    private static final String EXCEPTION_INVALID_RESPONDENT_INDEX_NOT_WITHIN_BOUNDS =
        "java.lang.Exception: Respondent index, 10 is not valid for the case with id, 1646225213651590";
    private static final String EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA =
        "java.lang.Exception: Case details with Case Id, %s doesn't have case data values";
    private static final String EXCEPTION_CASE_USER_ROLE_NOT_FOUND =
        "java.lang.Exception: Case user role not found for caseId: %s";
    public static final String EXCEPTION_INVALID_CASE_USER_ROLE = "java.lang.Exception: Invalid case user role: %s";
    private static final String EXCEPTION_CASE_DETAILS_NOT_FOUND =
        "java.lang.Exception: Case details not found with the given caseId, ";
    private static final String EXCEPTION_EMPTY_RESPONDENT_COLLECTION =
        "java.lang.Exception: Respondent collection not found for the case with id, 1646225213651590";
    private static final String EXCEPTION_NOTICE_OF_CHANGE_ANSWER_NOT_FOUND =
        "java.lang.Exception: Notice of change answer not found for the respondent with name, Test respondent name"
            + " for the case with id, 1646225213651590";
    private static final String EXCEPTION_RESPONDENT_SOLICITOR_TYPE_NOT_FOUND =
        "java.lang.Exception: Respondent solicitor type not found for case with id, 1646225213651590 and "
            + "respondent organisation policy index, 10";

    private static final Long TEST_CASE_ID = 1646225213651590L;
    private static final String STRING_TEN = "10";
    private static final String INVALID_INTEGER = "abc";
    private static final String TEST_RESPONDENT_NAME = "Test respondent name";
    private static final String CASE_USER_ROLE_CLAIMANT_SOLICITOR = "[CLAIMANTSOLICITOR]";
    private static final String CASE_USER_ROLE_INVALID = "[INVALID]";

    @ParameterizedTest
    @MethodSource("provideTheCreateAacSearchCaseUsersUriByCaseAndUserIdsTestData")
    void theCreateAacSearchCaseUsersUriByCaseAndUserIds(String aacUrl,
                                                        List<CaseDetails> caseDetailsList,
                                                        List<UserInfo> userInfoList) {
        if (StringUtils.isBlank(aacUrl) || CollectionUtils.isEmpty(caseDetailsList)) {
            assertThat(ManageCaseRoleServiceUtil.createAacSearchCaseUsersUriByCaseAndUserIds(
                aacUrl, caseDetailsList, userInfoList)).isEqualTo(StringUtils.EMPTY);
            return;
        }
        if (CollectionUtils.isEmpty(userInfoList)) {
            assertThat(ManageCaseRoleServiceUtil.createAacSearchCaseUsersUriByCaseAndUserIds(
                aacUrl, caseDetailsList, userInfoList)).isEqualTo(EXPECTED_AAC_URI_WITH_ONLY_CASE_DETAILS);
            return;
        }
        assertThat(ManageCaseRoleServiceUtil.createAacSearchCaseUsersUriByCaseAndUserIds(
            aacUrl, caseDetailsList, userInfoList)).isEqualTo(EXPECTED_AAC_URI_WITH_CASE_AND_USER_IDS);
    }

    private static Stream<Arguments> provideTheCreateAacSearchCaseUsersUriByCaseAndUserIdsTestData() {
        List<CaseDetails> caseDetailsList = new CaseTestData().getRequestCaseDataListScotland();
        UserInfo userInfo = new CaseTestData().getUserInfo();
        List<UserInfo> userInfoList = List.of(userInfo, userInfo);
        return Stream.of(Arguments.of(null, null, null),
                         Arguments.of(null, caseDetailsList, null),
                         Arguments.of(StringUtils.EMPTY, caseDetailsList, null),
                         Arguments.of(AAC_URL_TEST_VALUE, null, null),
                         Arguments.of(AAC_URL_TEST_VALUE, new ArrayList<>(), null),
                         Arguments.of(AAC_URL_TEST_VALUE, caseDetailsList, null),
                         Arguments.of(AAC_URL_TEST_VALUE, caseDetailsList, new ArrayList<>()),
                         Arguments.of(AAC_URL_TEST_VALUE, caseDetailsList, userInfoList));
    }

    @ParameterizedTest
    @MethodSource("provideTheFindCaseUserRoleTestData")
    void theFindCaseUserRole(CaseDetails caseDetails, CaseAssignmentUserRole caseAssignmentUserRole) {
        String actualCaseUserRole = ManageCaseRoleServiceUtil.findCaseUserRole(caseDetails, caseAssignmentUserRole);
        if (ObjectUtils.isNotEmpty(caseDetails)
            && ObjectUtils.isNotEmpty(caseDetails.getId())
            && ObjectUtils.isNotEmpty(caseAssignmentUserRole)
            && StringUtils.isNotEmpty(caseAssignmentUserRole.getCaseDataId())
            && caseDetails.getId().toString().equals(caseAssignmentUserRole.getCaseDataId())
            && (CASE_USER_ROLE_CREATOR.equals(caseAssignmentUserRole.getCaseRole())
            ||  ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT.equals(caseAssignmentUserRole.getCaseRole()))) {
            if (TEST_CASE_USER_ROLE_CREATOR.equals(caseAssignmentUserRole.getCaseRole())) {
                assertThat(actualCaseUserRole).isEqualTo(TEST_CASE_USER_ROLE_CREATOR);
            } else {
                assertThat(actualCaseUserRole).isEqualTo(TEST_CASE_USER_ROLE_DEFENDANT);
            }
        } else {
            assertThat(actualCaseUserRole).isEqualTo(StringUtils.EMPTY);
        }
    }

    private static Stream<Arguments> provideTheFindCaseUserRoleTestData() {
        CaseDetails caseDetails = new CaseTestData().getCaseDetails();
        CaseAssignmentUserRole caseAssignmentUserRoleCreator =
            CaseAssignmentUserRole.builder()
                .caseRole(TEST_CASE_USER_ROLE_CREATOR)
                .caseDataId(caseDetails.getId().toString())
                .userId(USER_ID).build();
        CaseAssignmentUserRole caseAssignmentUserRoleDefendant =
            CaseAssignmentUserRole.builder()
                .caseRole(TEST_CASE_USER_ROLE_DEFENDANT)
                .caseDataId(caseDetails.getId().toString())
                .userId(USER_ID).build();
        CaseDetails caseDetailsWithEmptyCaseId = new CaseTestData().getCaseDetails();
        caseDetailsWithEmptyCaseId.setId(null);
        CaseAssignmentUserRole caseAssignmentUserRoleWithEmptyCaseDataId =
            CaseAssignmentUserRole.builder()
                .caseRole(TEST_CASE_USER_ROLE_CREATOR).caseDataId(StringUtils.EMPTY).userId(USER_ID).build();
        CaseAssignmentUserRole caseAssignmentUserRoleWithInvalidCaseDataId =
            CaseAssignmentUserRole.builder()
                .caseRole(TEST_CASE_USER_ROLE_CREATOR).caseDataId(CASE_ID).userId(USER_ID).build();
        CaseAssignmentUserRole caseAssignmentUserRoleWithInvalidCaseUserRole =
            CaseAssignmentUserRole.builder()
                .caseRole(TEST_CASE_USER_ROLE_INVALID).caseDataId(CASE_ID).userId(USER_ID).build();
        return Stream.of(Arguments.of(null, caseAssignmentUserRoleCreator),
                         Arguments.of(caseDetailsWithEmptyCaseId, caseAssignmentUserRoleCreator),
                         Arguments.of(caseDetailsWithEmptyCaseId, caseAssignmentUserRoleDefendant),
                         Arguments.of(caseDetails, null),
                         Arguments.of(caseDetails, caseAssignmentUserRoleWithEmptyCaseDataId),
                         Arguments.of(caseDetails, caseAssignmentUserRoleWithInvalidCaseDataId),
                         Arguments.of(caseDetails, caseAssignmentUserRoleWithInvalidCaseUserRole));
    }

    @Test
    void theIsCaseRoleAssignmentExceptionForSameUser() {
        assertThat(ManageCaseRoleServiceUtil.isCaseRoleAssignmentExceptionForSameUser(
            new Exception("Test Exception"))).isFalse();
        assertThat(ManageCaseRoleServiceUtil.isCaseRoleAssignmentExceptionForSameUser(
            new Exception("You have already been assigned to this case caseId, "))).isTrue();
    }

    @Test
    void theCreateCaseUserRoleRequest() {
        CaseAssignmentUserRolesRequest expectedCaseAssignmentUserRolesRequest = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRoles(List.of(CaseAssignmentUserRole.builder()
                                                 .caseDataId(TEST_CASE_ID_STRING)
                                                 .userId(TEST_CLAIMANT_SOLICITOR_IDAM_ID)
                                                 .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR).build())).build();
        CaseAssignmentUserRolesRequest actualCaseAssignmentUserRolesRequest = ManageCaseRoleServiceUtil
            .createCaseUserRoleRequest(
                TEST_CLAIMANT_SOLICITOR_IDAM_ID,
                CaseDetails.builder().id(TEST_CASE_ID_LONG).build(),
                CASE_USER_ROLE_CLAIMANT_SOLICITOR);
        assertThat(expectedCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().getFirst().getCaseDataId())
            .isEqualTo(actualCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().getFirst().getCaseDataId());
        assertThat(expectedCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().getFirst().getCaseRole())
            .isEqualTo(actualCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().getFirst().getCaseRole());
        assertThat(expectedCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().getFirst().getUserId())
            .isEqualTo(actualCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().getFirst().getUserId());
    }

    @Test
    void theBuildCaseAccessUrl() {
        String expectedCaseAccessUrl = "http://localhost:8080/ccd/data-store/case-users?case_ids=1234567890123456";
        assertThat(ManageCaseRoleServiceUtil.buildCaseAccessUrl(TEST_CCD_DATA_STORE_BASE_URL, TEST_CASE_ID_STRING))
            .isEqualTo(expectedCaseAccessUrl);
    }

    @Test
    void theCheckModifyCaseUserRolesRequest() {
        // Should throw exception when modifyCaseUserRolesRequest is null or empty
        assertThrows(ManageCaseRoleException.class, () -> ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(
            null));
        // Should throw exception when modifyCaseUserRolesRequest is invalid

        // Should throw exception when modifyCaseUserRolesRequest doesn't have modifyCaseUserRole
        assertThrows(ManageCaseRoleException.class, () -> ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(
            ModifyCaseUserRolesRequest.builder().build()));
        // Should throw exception when modifyCaseUserRolesRequest have null modifyCaseUserRole
        ModifyCaseUserRolesRequest modifyCaseUserRolesRequestWithNullModifyCaseUserRole =
            ModifyCaseUserRolesRequest.builder().build();
        modifyCaseUserRolesRequestWithNullModifyCaseUserRole.setModifyCaseUserRoles(new ArrayList<>());
        modifyCaseUserRolesRequestWithNullModifyCaseUserRole.getModifyCaseUserRoles().add(null);
        assertThrows(ManageCaseRoleException.class, () -> ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(
            modifyCaseUserRolesRequestWithNullModifyCaseUserRole));
        // Should throw exception when modifyCaseUserRolesRequest has empty modifyCaseUserRole
        ModifyCaseUserRolesRequest modifyCaseUserRolesRequest =
            ModifyCaseUserRolesRequest.builder().modifyCaseUserRoles(
                List.of(ModifyCaseUserRole.builder().build())).build();
        assertThrows(ManageCaseRoleException.class,
                     () -> ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(modifyCaseUserRolesRequest));
        // Should not throw exception when modifyCaseUserRolesRequest has modifyCaseUserRole with not empty userId,
        // and valid caseRole
        ModifyCaseUserRole modifyCaseUserRoleNotEmptyUserId = ModifyCaseUserRole.builder()
            .userId(USER_ID)
            .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR)
            .build();
        assertDoesNotThrow(() -> ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(
            ModifyCaseUserRolesRequest.builder().modifyCaseUserRoles(
                List.of(modifyCaseUserRoleNotEmptyUserId)).build()));
        // Should not throw exception when modifyCaseUserRolesRequest has modifyCaseUserRole with not empty caseTypeId,
        // and valid caseRole
        ModifyCaseUserRole modifyCaseUserRoleNotEmptyCaseTypeId = ModifyCaseUserRole.builder()
            .caseTypeId("Dummy case type id")
            .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR)
            .build();
        assertDoesNotThrow(() -> ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(
            ModifyCaseUserRolesRequest.builder().modifyCaseUserRoles(
                List.of(modifyCaseUserRoleNotEmptyCaseTypeId)).build()));
        // Should not throw exception when modifyCaseUserRolesRequest has modifyCaseUserRole with not empty caseDataId,
        // and valid caseRole
        ModifyCaseUserRole modifyCaseUserRoleNotEmptyCaseDataId = ModifyCaseUserRole.builder()
            .caseDataId(TEST_CASE_ID_STRING)
            .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR)
            .build();
        assertDoesNotThrow(() -> ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(
            ModifyCaseUserRolesRequest.builder().modifyCaseUserRoles(
                List.of(modifyCaseUserRoleNotEmptyCaseDataId)).build()));
        // Should not throw exception when modifyCaseUserRolesRequest has modifyCaseUserRole with valid caseRole,
        // CASE_USER_ROLE_CLAIMANT_SOLICITOR
        ModifyCaseUserRole modifyCaseUserRoleNotEmptyCaseRoleClaimantSolicitor = ModifyCaseUserRole.builder()
            .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR)
            .build();
        assertDoesNotThrow(() -> ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(
            ModifyCaseUserRolesRequest.builder().modifyCaseUserRoles(
                List.of(modifyCaseUserRoleNotEmptyCaseRoleClaimantSolicitor)).build()));
        // Should not throw exception when modifyCaseUserRolesRequest has modifyCaseUserRole with valid caseRole,
        // CASE_USER_ROLE_CREATOR
        ModifyCaseUserRole modifyCaseUserRoleNotEmptyCaseRoleCreator = ModifyCaseUserRole.builder()
            .caseRole(CASE_USER_ROLE_CREATOR)
            .build();
        assertDoesNotThrow(() -> ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(
            ModifyCaseUserRolesRequest.builder().modifyCaseUserRoles(
                List.of(modifyCaseUserRoleNotEmptyCaseRoleCreator)).build()));
        // Should not throw exception when modifyCaseUserRolesRequest has modifyCaseUserRole with valid caseRole,
        // CASE_USER_ROLE_DEFENDANT
        ModifyCaseUserRole modifyCaseUserRoleNotEmptyCaseRoleDefendant = ModifyCaseUserRole.builder()
            .caseRole(CASE_USER_ROLE_DEFENDANT)
            .build();
        assertDoesNotThrow(() -> ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(
            ModifyCaseUserRolesRequest.builder().modifyCaseUserRoles(
                List.of(modifyCaseUserRoleNotEmptyCaseRoleDefendant)).build()));
        // Should not throw exception when modifyCaseUserRolesRequest has modifyCaseUserRole with not empty
        // respondentName, and valid caseRole
        ModifyCaseUserRole modifyCaseUserRoleNotEmptyRespondentName = ModifyCaseUserRole.builder()
            .respondentName("Dummy respondent name")
            .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR)
            .build();
        assertDoesNotThrow(() -> ManageCaseRoleServiceUtil.checkModifyCaseUserRolesRequest(
            ModifyCaseUserRolesRequest.builder().modifyCaseUserRoles(
                List.of(modifyCaseUserRoleNotEmptyRespondentName)).build()));
        // Should throw exception when modifyCaseUserRolesRequest has modifyCaseUserRole with invalid caseRole,
        // CASE_USER_ROLE_DEFENDANT
        ModifyCaseUserRole modifyCaseUserRoleNotEmptyCaseRoleInvalid = ModifyCaseUserRole.builder()
            .caseRole(TEST_CASE_USER_ROLE_INVALID)
            .build();
        ModifyCaseUserRolesRequest modifyCaseUserRolesRequestNotEmptyCaseRoleInvalid =
            ModifyCaseUserRolesRequest.builder()
                .modifyCaseUserRoles(List.of(modifyCaseUserRoleNotEmptyCaseRoleInvalid)).build();
        assertThrows(ManageCaseRoleException.class, () -> ManageCaseRoleServiceUtil
            .checkModifyCaseUserRolesRequest(modifyCaseUserRolesRequestNotEmptyCaseRoleInvalid));
    }

    @Test
    void theGetRespondentSolicitorTypeFromIndex() {
        // Should throw ManageCaseRoleException when caseDetails is null or empty
        ManageCaseRoleException manageCaseRoleException =
            assertThrows(ManageCaseRoleException.class, () ->
                ManageCaseRoleServiceUtil.getRespondentSolicitorType(null,
                                                                     NumberUtils.INTEGER_ONE.toString()));
        assertThat(manageCaseRoleException.getMessage()).isEqualTo(EXCEPTION_CASE_DETAILS_NOT_FOUND);

        // Should throw ManageCaseRoleException when caseDetails not have case data
        final CaseDetails caseDetailsWithoutCaseData = CaseDetails.builder().id(TEST_CASE_ID).build();
        manageCaseRoleException =
            assertThrows(ManageCaseRoleException.class, () ->
                ManageCaseRoleServiceUtil.getRespondentSolicitorType(caseDetailsWithoutCaseData,
                                                                     NumberUtils.INTEGER_ONE.toString()));
        assertThat(manageCaseRoleException.getMessage()).isEqualTo(
            String.format(EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA, TEST_CASE_ID));

        // Should throw ManageCaseRoleException when respondentIndex is empty
        final CaseDetails caseDetails = new CaseTestData().getCaseDetails();
        manageCaseRoleException = assertThrows(
            ManageCaseRoleException.class, () -> ManageCaseRoleServiceUtil
                .getRespondentSolicitorType(caseDetails, StringUtils.EMPTY));
        assertThat(manageCaseRoleException.getMessage()).isEqualTo(EXCEPTION_INVALID_RESPONDENT_INDEX_EMPTY);

        // Should throw ManageCaseRoleException when respondentIndex is not numeric
        manageCaseRoleException = assertThrows(
            ManageCaseRoleException.class, () -> ManageCaseRoleServiceUtil
                .getRespondentSolicitorType(caseDetails, INVALID_INTEGER));
        assertThat(manageCaseRoleException.getMessage()).isEqualTo(EXCEPTION_INVALID_RESPONDENT_INDEX_NOT_NUMERIC);

        // Should throw ManageCaseRoleException when respondentIndex is negative
        manageCaseRoleException = assertThrows(
            ManageCaseRoleException.class, () -> ManageCaseRoleServiceUtil
                .getRespondentSolicitorType(caseDetails, NumberUtils.INTEGER_MINUS_ONE.toString()));
        assertThat(manageCaseRoleException.getMessage()).isEqualTo(EXCEPTION_INVALID_RESPONDENT_INDEX_NEGATIVE);


        // Should throw ManageCaseRoleException when not able to map case details' case data map to case data object
        MockedStatic<EmployeeObjectMapper> employeeObjectMapper = mockStatic(EmployeeObjectMapper.class);
        employeeObjectMapper.when(
            () -> EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData())).thenReturn(null);
        manageCaseRoleException = assertThrows(ManageCaseRoleException.class, () -> ManageCaseRoleServiceUtil
            .getRespondentSolicitorType(caseDetails, NumberUtils.INTEGER_ZERO.toString()));
        assertThat(manageCaseRoleException.getMessage()).isEqualTo(
            String.format(EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA, TEST_CASE_ID));


        // Should throw ManageCaseRoleException when caseDetails' caseData doesn't have respondent collection
        final CaseData caseDataWithoutRespondentCollection = new CaseData();
        employeeObjectMapper.when(
            () -> EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData()))
            .thenReturn(caseDataWithoutRespondentCollection);
        manageCaseRoleException = assertThrows(ManageCaseRoleException.class, () -> ManageCaseRoleServiceUtil
            .getRespondentSolicitorType(caseDetails, NumberUtils.INTEGER_ZERO.toString()));
        assertThat(manageCaseRoleException.getMessage()).isEqualTo(EXCEPTION_EMPTY_RESPONDENT_COLLECTION);

        // Should throw ManageCaseRoleException when respondent not found with the given index
        employeeObjectMapper.close();
        manageCaseRoleException = assertThrows(ManageCaseRoleException.class, () -> ManageCaseRoleServiceUtil
            .getRespondentSolicitorType(caseDetails, STRING_TEN));
        assertThat(manageCaseRoleException.getMessage()).isEqualTo(
            EXCEPTION_INVALID_RESPONDENT_INDEX_NOT_WITHIN_BOUNDS);

        // Should throw ManageCaseRoleException when notice of change answer not found
        final CaseData validCaseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        MockedStatic<NoticeOfChangeUtil> noticeOfChangeUtil = mockStatic(NoticeOfChangeUtil.class);
        noticeOfChangeUtil.when(() -> NoticeOfChangeUtil.findNoticeOfChangeAnswerIndex(validCaseData,
                                                                                       TEST_RESPONDENT_NAME))
            .thenReturn(NumberUtils.INTEGER_MINUS_ONE);
        manageCaseRoleException = assertThrows(ManageCaseRoleException.class, () -> ManageCaseRoleServiceUtil
            .getRespondentSolicitorType(caseDetails, NumberUtils.INTEGER_ZERO.toString()));
        assertThat(manageCaseRoleException.getMessage()).isEqualTo(
            EXCEPTION_NOTICE_OF_CHANGE_ANSWER_NOT_FOUND);

        // Should throw ManageCaseRoleException when respondent solicitor type not found
        noticeOfChangeUtil.when(() -> NoticeOfChangeUtil.findNoticeOfChangeAnswerIndex(validCaseData,
                                                                                       TEST_RESPONDENT_NAME))
            .thenReturn(NumberUtils.createInteger(STRING_TEN));
        manageCaseRoleException = assertThrows(ManageCaseRoleException.class, () -> ManageCaseRoleServiceUtil
            .getRespondentSolicitorType(caseDetails, NumberUtils.INTEGER_ZERO.toString()));
        assertThat(manageCaseRoleException.getMessage()).isEqualTo(
            EXCEPTION_RESPONDENT_SOLICITOR_TYPE_NOT_FOUND);

        // Should return valid respondent solicitor type
        noticeOfChangeUtil.when(() -> NoticeOfChangeUtil.findNoticeOfChangeAnswerIndex(validCaseData,
                                                                                       TEST_RESPONDENT_NAME))
            .thenReturn(NumberUtils.INTEGER_ZERO);
        noticeOfChangeUtil.when(() -> NoticeOfChangeUtil.findRespondentSolicitorTypeByIndex(
            NumberUtils.INTEGER_ZERO)).thenReturn(RespondentSolicitorType.SOLICITORA);
        assertThat(ManageCaseRoleServiceUtil.getRespondentSolicitorType(
            caseDetails, NumberUtils.INTEGER_ZERO.toString())).isEqualTo(RespondentSolicitorType.SOLICITORA);
    }

    @Test
    void testResetOrganizationPolicy_AllScenarios() {
        String caseId = "CASE-001";

        // -- Test 1: Null caseData --
        ManageCaseRoleException ex1 = assertThrows(ManageCaseRoleException.class, () ->
            ManageCaseRoleServiceUtil.resetOrganizationPolicy(null,
                                                              CASE_USER_ROLE_CLAIMANT_SOLICITOR,
                                                              caseId));
        assertThat(ex1.getMessage()).isEqualTo(String.format(EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA, caseId));

        // -- Test 2: Blank caseUserRole --
        CaseData blankRoleCase = new CaseData();
        ManageCaseRoleException ex2 = assertThrows(ManageCaseRoleException.class, () ->
            ManageCaseRoleServiceUtil.resetOrganizationPolicy(blankRoleCase, StringUtils.EMPTY, caseId));
        assertThat(ex2.getMessage()).isEqualTo(String.format(EXCEPTION_CASE_USER_ROLE_NOT_FOUND, caseId));

        // -- Test 3: Valid claimant role --
        CaseData claimantCase = new CaseData();
        ManageCaseRoleServiceUtil.resetOrganizationPolicy(claimantCase, CASE_USER_ROLE_CLAIMANT_SOLICITOR, caseId);
        OrganisationPolicy claimantPolicy = claimantCase.getClaimantRepresentativeOrganisationPolicy();
        assertThat(claimantPolicy).isNotNull();
        assertThat(claimantPolicy.getOrgPolicyCaseAssignedRole()).isEqualTo(CASE_USER_ROLE_CLAIMANT_SOLICITOR);

        // -- Test 4: Valid respondent solicitor roles --
        List<String> roles = List.of(
            RespondentSolicitorType.SOLICITORA.getLabel(),
            RespondentSolicitorType.SOLICITORB.getLabel(),
            RespondentSolicitorType.SOLICITORC.getLabel(),
            RespondentSolicitorType.SOLICITORD.getLabel(),
            RespondentSolicitorType.SOLICITORE.getLabel(),
            RespondentSolicitorType.SOLICITORF.getLabel(),
            RespondentSolicitorType.SOLICITORG.getLabel(),
            RespondentSolicitorType.SOLICITORH.getLabel(),
            RespondentSolicitorType.SOLICITORI.getLabel(),
            RespondentSolicitorType.SOLICITORJ.getLabel()
        );

        for (int i = 0; i < roles.size(); i++) {
            String role = roles.get(i);
            CaseData caseData = new CaseData();
            ManageCaseRoleServiceUtil.resetOrganizationPolicy(caseData, role, caseId);

            OrganisationPolicy actualPolicy = switch (i) {
                case 0 -> caseData.getRespondentOrganisationPolicy0();
                case 1 -> caseData.getRespondentOrganisationPolicy1();
                case 2 -> caseData.getRespondentOrganisationPolicy2();
                case 3 -> caseData.getRespondentOrganisationPolicy3();
                case 4 -> caseData.getRespondentOrganisationPolicy4();
                case 5 -> caseData.getRespondentOrganisationPolicy5();
                case 6 -> caseData.getRespondentOrganisationPolicy6();
                case 7 -> caseData.getRespondentOrganisationPolicy7();
                case 8 -> caseData.getRespondentOrganisationPolicy8();
                case 9 -> caseData.getRespondentOrganisationPolicy9();
                default -> throw new ManageCaseRoleException(new Exception(
                    EXCEPTION_INVALID_RESPONDENT_INDEX_EMPTY));
            };

            assertThat(actualPolicy).isNotNull();
            assertThat(role).isEqualTo(actualPolicy.getOrgPolicyCaseAssignedRole());
        }
        // -- Test 5: Invalid role --
        CaseData claimantCase2 = new CaseData();
        ManageCaseRoleException ex5 = assertThrows(ManageCaseRoleException.class, () ->
            ManageCaseRoleServiceUtil.resetOrganizationPolicy(claimantCase2, CASE_USER_ROLE_INVALID, caseId));
        assertThat(ex5.getMessage()).isEqualTo(String.format(EXCEPTION_INVALID_CASE_USER_ROLE, CASE_USER_ROLE_INVALID));
    }
}
