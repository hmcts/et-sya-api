package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignedUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;
import uk.gov.hmcts.reform.et.syaapi.search.ElasticSearchQueryBuilder;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ManageCaseRoleServiceUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CCD_API_POST_METHOD_NAME;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
class ManageCaseRoleServiceTest {

    @Mock
    RestTemplate restTemplate;
    @Mock
    AdminUserService adminUserService;
    @Mock
    CoreCaseDataApi ccdApi;
    @Mock
    AuthTokenGenerator authTokenGenerator;
    @Mock
    IdamClient idamClient;
    @Mock
    ET3Service et3Service;
    @Mock
    CaseService caseService;

    private ManageCaseRoleService manageCaseRoleService;
    private UserInfo userInfo;
    private CaseAssignmentUserRole caseAssignmentUserRole1;
    private CaseAssignmentUserRole caseAssignmentUserRole2;
    private CaseAssignmentUserRole caseAssignmentUserRole3;
    private CaseTestData caseTestData;

    private CaseAssignedUserRolesResponse expectedCaseAssignedUserRolesResponseCreator;
    private CaseAssignedUserRolesResponse expectedCaseAssignedUserRolesResponseDefendant;

    private static final String INVALID_MODIFICATION_TYPE_EXPECTED_EXCEPTION_MESSAGE =
        "java.lang.Exception: Invalid modification type";
    private static final String INVALID_CASE_ROLE_REQUEST_EXCEPTION_MESSAGE =
        "java.lang.Exception: Request to modify roles is empty";
    private static final String INVALID_ROLE_MODIFICATION_ITEM_EXCEPTION_MESSAGE =
        "java.lang.Exception: One of the case user role modification item is invalid. "
            + "Invalid Data is For CaseId: ";
    private static final String MODIFICATION_TYPE_ASSIGNMENT = "Assignment";
    private static final String MODIFICATION_TYPE_REVOKE = "Revoke";
    private static final String CASE_ID = "1646225213651590";
    private static final String USER_ID = "1234564789";
    private static final String CASE_ROLE_DEFENDANT = "[DEFENDANT]";
    private static final String CASE_ROLE_CREATOR = "[CREATOR]";
    private static final String CASE_SUBMISSION_REFERENCE = "1234567890123456";
    private static final String RESPONDENT_NAME = "Respondent Name";
    private static final String CLAIMANT_FIRST_NAMES = "Claimant First Names";
    private static final String CLAIMANT_LAST_NAME = "Claimant Last Name";
    private static final String DUMMY_CASE_SUBMISSION_REFERENCE = "1234567890123456";
    private static final String DUMMY_USER_ID = "123456789012345678901234567890";
    private static final String DUMMY_AUTHORISATION_TOKEN = "dummy_authorisation_token";
    private static final String TEST_SERVICE_AUTH_TOKEN = "Bearer TestServiceAuth";
    private static final String USER_CASE_ROLE_DEFENDANT = "[DEFENDANT]";
    private static final String ENGLAND_CASE_TYPE = "ET_EnglandWales";
    private static final String SCOTLAND_CASE_TYPE = "ET_Scotland";
    private static final String AAC_URL_PARAMETER_NAME = "aacUrl";
    private static final String AAC_URL_PARAMETER_TEST_VALUE = "https://test.url.com";
    private static final String CCD_API_URL_PARAMETER_NAME = "ccdApiUrl";
    private static final String CCD_API_URL_PARAMETER_TEST_VALUE = "https://test.url.com";
    private static final String EXPECTED_EMPTY_CASE_DETAILS_EXCEPTION_MESSAGE =
        "java.lang.Exception: Unable to get user cases because not able to create aacApiUrl with the given "
            + "caseDetails and authorization data";
    private static final String TEST_CASE_STATE_ACCEPTED = "Accepted";

    @BeforeEach
    void setup() {
        caseTestData = new CaseTestData();
        manageCaseRoleService = new ManageCaseRoleService(
            adminUserService, restTemplate, authTokenGenerator, ccdApi, idamClient, et3Service, caseService);
        userInfo = new CaseTestData().getUserInfo();
        caseAssignmentUserRole1 = CaseAssignmentUserRole.builder()
            .userId(DUMMY_USER_ID)
            .caseRole(CASE_ROLE_CREATOR).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .caseDataId("1646225213651533")
            .build();
        caseAssignmentUserRole2 = CaseAssignmentUserRole.builder()
            .userId(DUMMY_USER_ID)
            .caseRole(CASE_ROLE_CREATOR).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .caseDataId("1646225213651512")
            .build();
        caseAssignmentUserRole3 = CaseAssignmentUserRole.builder()
            .userId(DUMMY_USER_ID)
            .caseRole(CASE_ROLE_CREATOR).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .caseDataId("1646225213651598")
            .build();

        expectedCaseAssignedUserRolesResponseCreator =
            CaseAssignedUserRolesResponse.builder()
                .caseAssignedUserRoles(List.of(caseAssignmentUserRole1,
                                               caseAssignmentUserRole2,
                                               caseAssignmentUserRole3))
            .build();

        CaseAssignmentUserRole caseAssignmentUserRole1D = CaseAssignmentUserRole.builder()
            .userId(DUMMY_USER_ID)
            .caseRole(CASE_USER_ROLE_DEFENDANT).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .caseDataId("1646225213651533")
            .build();
        CaseAssignmentUserRole caseAssignmentUserRole2D = CaseAssignmentUserRole.builder()
            .userId(DUMMY_USER_ID)
            .caseRole(CASE_USER_ROLE_DEFENDANT).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .caseDataId("1646225213651512")
            .build();
        CaseAssignmentUserRole caseAssignmentUserRole3D = CaseAssignmentUserRole.builder()
            .userId(DUMMY_USER_ID)
            .caseRole(CASE_USER_ROLE_DEFENDANT).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .caseDataId("1646225213651598")
            .build();
        expectedCaseAssignedUserRolesResponseDefendant = CaseAssignedUserRolesResponse.builder()
            .caseAssignedUserRoles(List.of(caseAssignmentUserRole1D,
                                           caseAssignmentUserRole2D,
                                           caseAssignmentUserRole3D))
            .build();
    }

    @ParameterizedTest
    @MethodSource("provideModifyUserCaseRolesTestData")
    @SneakyThrows
    void theModifyUserCaseRoles(ModifyCaseUserRolesRequest modifyCaseUserRolesRequest, String modificationType) {
        if (StringUtils.isEmpty(modificationType)
            || !MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)
            && !MODIFICATION_TYPE_REVOKE.equals(modificationType)) {
            ManageCaseRoleException exception = assertThrows(ManageCaseRoleException.class, () ->
                manageCaseRoleService.modifyUserCaseRoles(DUMMY_AUTHORISATION_TOKEN,
                                                          modifyCaseUserRolesRequest,
                                                          modificationType));
            assertThat(exception.getMessage()).isEqualTo(INVALID_MODIFICATION_TYPE_EXPECTED_EXCEPTION_MESSAGE);
            return;
        }
        if (isModifyCaseUserRolesRequestInvalid(modifyCaseUserRolesRequest)) {
            ManageCaseRoleException exception = assertThrows(ManageCaseRoleException.class, () ->
                manageCaseRoleService.modifyUserCaseRoles(TestConstants.DUMMY_AUTHORISATION_TOKEN,
                                                          modifyCaseUserRolesRequest,
                                                          modificationType));
            assertThat(exception.getMessage()).isEqualTo(INVALID_CASE_ROLE_REQUEST_EXCEPTION_MESSAGE);
            return;
        }
        if (isAnyOfTheModifyCaseUserRoleInvalid(modifyCaseUserRolesRequest)) {
            ManageCaseRoleException exception = assertThrows(ManageCaseRoleException.class, () ->
                manageCaseRoleService.modifyUserCaseRoles(TestConstants.DUMMY_AUTHORISATION_TOKEN,
                                                          modifyCaseUserRolesRequest,
                                                          modificationType));
            assertThat(exception.getMessage()).contains(INVALID_ROLE_MODIFICATION_ITEM_EXCEPTION_MESSAGE);
            return;
        }
        CaseDetails expectedCaseDetails = new CaseTestData().getCaseDetailsWithCaseData();

        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        if (CASE_USER_ROLE_DEFENDANT.equals(modifyCaseUserRolesRequest
                                                .getModifyCaseUserRoles()
                                                .get(0).getCaseRole())) {
            setExpectedDetails(modifyCaseUserRolesRequest, modificationType, expectedCaseDetails);
        }
        HttpMethod httpMethod = MODIFICATION_TYPE_REVOKE.equals(modificationType) ? HttpMethod.DELETE : HttpMethod.POST;
        when(restTemplate.exchange(ArgumentMatchers.anyString(),
                                   ArgumentMatchers.eq(httpMethod),
                                   ArgumentMatchers.any(),
                                   ArgumentMatchers.eq(CaseAssignmentUserRolesResponse.class)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        assertDoesNotThrow(() -> manageCaseRoleService.modifyUserCaseRoles(
            TestConstants.DUMMY_AUTHORISATION_TOKEN,
            modifyCaseUserRolesRequest,
            modificationType));
    }

    private void setExpectedDetails(ModifyCaseUserRolesRequest modifyCaseUserRolesRequest,
                                           String modificationType,
                                           CaseDetails expectedCaseDetails) {
        if (ManageCaseRoleConstants.MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)) {
            CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(expectedCaseDetails.getData());
            caseData.getRespondentCollection().get(0).getValue().setRespondentName(
                TestConstants.TEST_RESPONDENT_NAME);
            expectedCaseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        }
        if (ManageCaseRoleConstants.MODIFICATION_TYPE_REVOKE.equals(modificationType)) {
            CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(expectedCaseDetails.getData());
            caseData.getRespondentCollection().get(0).getValue().setIdamId(USER_ID);
            expectedCaseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        }
        when(et3Service.findCaseBySubmissionReference(modifyCaseUserRolesRequest
                                                          .getModifyCaseUserRoles()
                                                          .get(0).getCaseDataId())).thenReturn(expectedCaseDetails);
        when(et3Service.updateSubmittedCaseWithCaseDetails(
            DUMMY_AUTHORISATION_TOKEN,
            expectedCaseDetails
        )).thenReturn(expectedCaseDetails);
    }

    private static boolean isModifyCaseUserRolesRequestInvalid(ModifyCaseUserRolesRequest modifyCaseUserRolesRequest) {
        return ObjectUtils.isEmpty(modifyCaseUserRolesRequest)
            || CollectionUtils.isEmpty(modifyCaseUserRolesRequest.getModifyCaseUserRoles());
    }

    private static boolean isAnyOfTheModifyCaseUserRoleInvalid(ModifyCaseUserRolesRequest modifyCaseUserRolesRequest) {
        for (ModifyCaseUserRole modifyCaseUserRole : modifyCaseUserRolesRequest.getModifyCaseUserRoles()) {
            if (ObjectUtils.isEmpty(modifyCaseUserRole)
                || StringUtils.isBlank(modifyCaseUserRole.getUserId())
                || StringUtils.isBlank(modifyCaseUserRole.getCaseTypeId())
                || StringUtils.isBlank(modifyCaseUserRole.getCaseRole())
                || StringUtils.isBlank(modifyCaseUserRole.getCaseDataId())
                || StringUtils.isBlank(modifyCaseUserRole.getRespondentName())) {
                return true;
            }
        }
        return false;
    }

    private static Stream<Arguments> provideModifyUserCaseRolesTestData() {
        ModifyCaseUserRole modifyCaseUserRoleValidRoleDefendant = ModifyCaseUserRole.builder()
            .userId(USER_ID)
            .caseDataId(CASE_ID)
            .caseRole(CASE_ROLE_DEFENDANT)
            .caseTypeId(ENGLAND_CASE_TYPE)
            .respondentName(RESPONDENT_NAME)
            .build();
        ModifyCaseUserRole modifyCaseUserRoleValidRoleCreator = ModifyCaseUserRole.builder()
            .userId(USER_ID)
            .caseDataId(CASE_ID)
            .caseRole(CASE_ROLE_CREATOR)
            .caseTypeId(ENGLAND_CASE_TYPE)
            .respondentName(RESPONDENT_NAME)
            .build();
        ModifyCaseUserRolesRequest modifyCaseUserRolesRequestValidDefendant = ModifyCaseUserRolesRequest
            .builder().modifyCaseUserRoles(List.of(modifyCaseUserRoleValidRoleDefendant)).build();
        ModifyCaseUserRolesRequest modifyCaseUserRolesRequestValidCreator = ModifyCaseUserRolesRequest
            .builder().modifyCaseUserRoles(List.of(modifyCaseUserRoleValidRoleCreator)).build();
        ModifyCaseUserRolesRequest modifyCaseUserRolesRequestEmpty = ModifyCaseUserRolesRequest.builder().build();

        return Stream.of(Arguments.of(modifyCaseUserRolesRequestValidDefendant, MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(modifyCaseUserRolesRequestValidDefendant, MODIFICATION_TYPE_REVOKE),
                         Arguments.of(modifyCaseUserRolesRequestValidCreator, MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(modifyCaseUserRolesRequestValidCreator, MODIFICATION_TYPE_REVOKE),
                         Arguments.of(modifyCaseUserRolesRequestValidDefendant, StringUtils.EMPTY),
                         Arguments.of(null, MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(modifyCaseUserRolesRequestEmpty, MODIFICATION_TYPE_ASSIGNMENT));
    }

    @Test
    void theFindCaseForRoleModificationForEnglandWales() throws IOException {
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest =
            FindCaseForRoleModificationRequest.builder()
                .caseSubmissionReference(CASE_SUBMISSION_REFERENCE)
                .respondentName(RESPONDENT_NAME)
                .claimantFirstNames(CLAIMANT_FIRST_NAMES)
                .claimantLastName(CLAIMANT_LAST_NAME)
                .build();
        String elasticSearchQuery = ElasticSearchQueryBuilder.buildByFindCaseForRoleModificationRequest(
            findCaseForRoleModificationRequest
        );
        when(adminUserService.getAdminUserToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new CaseTestData().getUserInfo());
        when(ccdApi.searchCases(TEST_SERVICE_AUTH_TOKEN,
                                TEST_SERVICE_AUTH_TOKEN,
                                ENGLAND_CASE_TYPE,
                                elasticSearchQuery))
            .thenReturn(SearchResult.builder().cases(List.of(CaseDetails.builder()
                                                                 .caseTypeId(ENGLAND_CASE_TYPE)
                                                                 .id(Long.parseLong(CASE_SUBMISSION_REFERENCE))
                                                                 .state(TEST_CASE_STATE_ACCEPTED)
                                                                 .build())).total(1).build());
        assertThat(manageCaseRoleService.findCaseForRoleModification(findCaseForRoleModificationRequest,
                                                                     TEST_SERVICE_AUTH_TOKEN)).isNotNull();
        assertThat(manageCaseRoleService.findCaseForRoleModification(findCaseForRoleModificationRequest,
                                                                     TEST_SERVICE_AUTH_TOKEN).getId().toString())
            .isEqualTo(CASE_SUBMISSION_REFERENCE);
        assertThat(manageCaseRoleService.findCaseForRoleModification(findCaseForRoleModificationRequest,
                                                                     TEST_SERVICE_AUTH_TOKEN).getCaseTypeId())
            .isEqualTo(ENGLAND_CASE_TYPE);

    }

    @Test
    void theFindCaseForRoleModificationForScotland() throws IOException {
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest =
            FindCaseForRoleModificationRequest.builder()
                .caseSubmissionReference(CASE_SUBMISSION_REFERENCE)
                .respondentName(RESPONDENT_NAME)
                .claimantFirstNames(CLAIMANT_FIRST_NAMES)
                .claimantLastName(CLAIMANT_LAST_NAME)
                .build();
        String elasticSearchQuery = ElasticSearchQueryBuilder.buildByFindCaseForRoleModificationRequest(
            findCaseForRoleModificationRequest
        );
        when(adminUserService.getAdminUserToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new CaseTestData().getUserInfo());
        when(ccdApi.searchCases(TEST_SERVICE_AUTH_TOKEN,
                                TEST_SERVICE_AUTH_TOKEN,
                                ENGLAND_CASE_TYPE,
                                elasticSearchQuery)).thenReturn(SearchResult.builder().build());
        when(ccdApi.searchCases(TEST_SERVICE_AUTH_TOKEN,
                                TEST_SERVICE_AUTH_TOKEN,
                                SCOTLAND_CASE_TYPE,
                                elasticSearchQuery))
            .thenReturn(SearchResult.builder().cases(List.of(CaseDetails.builder()
                                                                 .caseTypeId(SCOTLAND_CASE_TYPE)
                                                                 .id(Long.parseLong(CASE_SUBMISSION_REFERENCE))
                                                                 .state(TEST_CASE_STATE_ACCEPTED)
                                                                 .build())).total(1).build());
        assertThat(manageCaseRoleService.findCaseForRoleModification(findCaseForRoleModificationRequest,
                                                                     TEST_SERVICE_AUTH_TOKEN)).isNotNull();
        assertThat(manageCaseRoleService.findCaseForRoleModification(findCaseForRoleModificationRequest,
                                                                     TEST_SERVICE_AUTH_TOKEN).getId().toString())
            .isEqualTo(CASE_SUBMISSION_REFERENCE);
        assertThat(manageCaseRoleService.findCaseForRoleModification(findCaseForRoleModificationRequest,
                                                                     TEST_SERVICE_AUTH_TOKEN).getCaseTypeId())
            .isEqualTo(SCOTLAND_CASE_TYPE);
    }

    @Test
    void theFindCaseForRoleModificationNotFound() throws IOException {
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest =
            FindCaseForRoleModificationRequest.builder()
                .caseSubmissionReference(CASE_SUBMISSION_REFERENCE)
                .respondentName(RESPONDENT_NAME)
                .claimantFirstNames(CLAIMANT_FIRST_NAMES)
                .claimantLastName(CLAIMANT_LAST_NAME)
                .build();
        String elasticSearchQuery = ElasticSearchQueryBuilder.buildByFindCaseForRoleModificationRequest(
            findCaseForRoleModificationRequest
        );
        when(adminUserService.getAdminUserToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApi.searchCases(TEST_SERVICE_AUTH_TOKEN,
                                TEST_SERVICE_AUTH_TOKEN,
                                ENGLAND_CASE_TYPE,
                                elasticSearchQuery)).thenReturn(SearchResult.builder().build());
        when(ccdApi.searchCases(TEST_SERVICE_AUTH_TOKEN,
                                TEST_SERVICE_AUTH_TOKEN,
                                SCOTLAND_CASE_TYPE,
                                elasticSearchQuery)).thenReturn(SearchResult.builder().build());
        assertThat(manageCaseRoleService.findCaseForRoleModification(findCaseForRoleModificationRequest,
                                                                     TEST_SERVICE_AUTH_TOKEN)).isNull();
    }

    @Test
    void theGenerateCaseAssignmentUserRolesRequest() {
        UserInfo userInfo = new CaseTestData().getUserInfo();
        ModifyCaseUserRole modifyCaseUserRoleWithoutUserId = ModifyCaseUserRole.builder()
            .userId(null)
            .caseRole(USER_CASE_ROLE_DEFENDANT)
            .caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .respondentName(RESPONDENT_NAME)
            .caseTypeId(SCOTLAND_CASE_TYPE)
            .build();
        ModifyCaseUserRole modifyCaseUserRoleWithUserId = ModifyCaseUserRole.builder()
            .userId(DUMMY_USER_ID)
            .caseRole(USER_CASE_ROLE_DEFENDANT)
            .caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .respondentName(RESPONDENT_NAME)
            .caseTypeId(SCOTLAND_CASE_TYPE)
            .build();
        ModifyCaseUserRolesRequest modifyCaseUserRolesRequest = ModifyCaseUserRolesRequest.builder()
            .modifyCaseUserRoles(List.of(modifyCaseUserRoleWithoutUserId, modifyCaseUserRoleWithUserId)).build();
        when(idamClient.getUserInfo(DUMMY_AUTHORISATION_TOKEN)).thenReturn(userInfo);
        ModifyCaseUserRolesRequest  actualModifyCaseUserRolesRequest =
            manageCaseRoleService.generateModifyCaseUserRolesRequest(
                DUMMY_AUTHORISATION_TOKEN, modifyCaseUserRolesRequest);
        assertThat(actualModifyCaseUserRolesRequest.getModifyCaseUserRoles()).hasSize(2);
        assertThat(actualModifyCaseUserRolesRequest.getModifyCaseUserRoles().get(0).getUserId())
            .isEqualTo(DUMMY_USER_ID);
        assertThat(actualModifyCaseUserRolesRequest.getModifyCaseUserRoles().get(1).getUserId())
            .isEqualTo(userInfo.getUid());
    }

    @Test
    @SneakyThrows
    void theGetCaseUserRolesByCaseAndUserIdsAac() {
        ReflectionTestUtils.setField(manageCaseRoleService, AAC_URL_PARAMETER_NAME, AAC_URL_PARAMETER_TEST_VALUE);
        CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole.builder()
            .userId(DUMMY_USER_ID)
            .caseRole(USER_CASE_ROLE_DEFENDANT).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .build();
        CaseAssignedUserRolesResponse expectedCaseAssignedUserRolesResponse = CaseAssignedUserRolesResponse.builder()
            .caseAssignedUserRoles(List.of(caseAssignmentUserRole))
            .build();
        when(idamClient.getUserInfo(DUMMY_AUTHORISATION_TOKEN)).thenReturn(userInfo);
        when(restTemplate.exchange(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.any(HttpEntity.class),
            ArgumentMatchers.eq(CaseAssignedUserRolesResponse.class)))
            .thenReturn(new ResponseEntity<>(expectedCaseAssignedUserRolesResponse, HttpStatus.OK));
        CaseAssignedUserRolesResponse actualCaseAssignedUserRolesResponse =
            manageCaseRoleService.getCaseUserRolesByCaseAndUserIdsAac(
                DUMMY_AUTHORISATION_TOKEN, List.of(new CaseTestData().getCaseDetails()));
        assertThat(actualCaseAssignedUserRolesResponse.getCaseAssignedUserRoles()).isNotNull();
        assertThat(actualCaseAssignedUserRolesResponse.getCaseAssignedUserRoles()).hasSize(1);
        assertThat(actualCaseAssignedUserRolesResponse).isEqualTo(expectedCaseAssignedUserRolesResponse);
    }

    @Test
    @SneakyThrows
    void theGetCaseUserRolesByCaseAndUserIdsAacThrowsExceptionWhenCaseDetailsEmpty() {
        ReflectionTestUtils.setField(manageCaseRoleService, AAC_URL_PARAMETER_NAME, AAC_URL_PARAMETER_TEST_VALUE);
        when(idamClient.getUserInfo(DUMMY_AUTHORISATION_TOKEN)).thenReturn(userInfo);
        String message = assertThrows(
            ManageCaseRoleException.class,
            () -> manageCaseRoleService
                         .getCaseUserRolesByCaseAndUserIdsAac(DUMMY_AUTHORISATION_TOKEN, null)).getMessage();
        assertThat(message).isEqualTo(EXPECTED_EMPTY_CASE_DETAILS_EXCEPTION_MESSAGE);
    }

    @Test
    @SneakyThrows
    void theGetCaseUserRolesByCaseAndUserIdsCcd() {
        ReflectionTestUtils.setField(manageCaseRoleService,
                                     CCD_API_URL_PARAMETER_NAME,
                                     CCD_API_URL_PARAMETER_TEST_VALUE);
        CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole.builder()
            .userId(DUMMY_USER_ID)
            .caseRole(USER_CASE_ROLE_DEFENDANT).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .build();
        CaseAssignedUserRolesResponse expectedCaseAssignedUserRolesResponse = CaseAssignedUserRolesResponse.builder()
            .caseAssignedUserRoles(List.of(caseAssignmentUserRole))
            .build();
        when(idamClient.getUserInfo(DUMMY_AUTHORISATION_TOKEN)).thenReturn(userInfo);
        when(restTemplate.postForObject(ArgumentMatchers.eq(CCD_API_URL_PARAMETER_TEST_VALUE
                                                                + CASE_USER_ROLE_CCD_API_POST_METHOD_NAME),
                                        ArgumentMatchers.any(HttpEntity.class),
                                        ArgumentMatchers.eq(CaseAssignedUserRolesResponse.class)))
            .thenReturn(expectedCaseAssignedUserRolesResponse);
        CaseAssignedUserRolesResponse actualCaseAssignedUserRolesResponse =
            manageCaseRoleService.getCaseUserRolesByCaseAndUserIdsCcd(
                DUMMY_AUTHORISATION_TOKEN, List.of(new CaseTestData().getCaseDetails()));
        assertThat(actualCaseAssignedUserRolesResponse.getCaseAssignedUserRoles()).isNotNull();
        assertThat(actualCaseAssignedUserRolesResponse.getCaseAssignedUserRoles()).hasSize(1);
        assertThat(actualCaseAssignedUserRolesResponse).isEqualTo(expectedCaseAssignedUserRolesResponse);
    }

    @Test
    @SneakyThrows
    void theGetCaseUserRolesByCaseCcdAndUserIdsWhenCaseDetailsEmpty() {
        assertThat(
            manageCaseRoleService.getCaseUserRolesByCaseAndUserIdsCcd(DUMMY_AUTHORISATION_TOKEN, null))
            .isEqualTo(CaseAssignedUserRolesResponse.builder().build());
        CaseDetails caseDetails = new CaseTestData().getCaseDetails();
        caseDetails.setId(null);
        assertThat(
            manageCaseRoleService.getCaseUserRolesByCaseAndUserIdsCcd(DUMMY_AUTHORISATION_TOKEN, List.of(caseDetails)))
            .isEqualTo(CaseAssignedUserRolesResponse.builder().build());
    }

    @ParameterizedTest
    @MethodSource("provideGetCaseDetailsByCaseUserRoleTestData")
    void theGetCaseDetailsByCaseUserRole(List<CaseDetails> caseDetailsList,
                                         List<CaseAssignmentUserRole> caseAssignmentUserRoles,
                                         String caseUserRole) {
        List<CaseDetails> caseDetailsListByCaseUserRole =
            ManageCaseRoleServiceUtil.getCaseDetailsByCaseUserRole(caseDetailsList,
                                                                   caseAssignmentUserRoles,
                                                                   caseUserRole);
        if (ObjectUtils.isEmpty(caseDetailsList)
            || CollectionUtils.isEmpty(caseAssignmentUserRoles)
            || StringUtils.isBlank(caseUserRole)) {
            assertThat(caseDetailsListByCaseUserRole).isNotNull();
            assertThat(caseDetailsListByCaseUserRole).hasSize(0);
        } else {
            if (CASE_ROLE_CREATOR.equals(caseUserRole)) {
                assertThat(caseDetailsListByCaseUserRole).isNotNull();
                assertThat(caseDetailsListByCaseUserRole).hasSize(2);
                assertThat(caseDetailsListByCaseUserRole)
                    .isEqualTo(new CaseTestData().getSearchResultRequestCaseDataListScotland().getCases());
            }
            if (CASE_ROLE_DEFENDANT.equals(caseUserRole)) {
                assertThat(caseDetailsListByCaseUserRole).isNotNull();
                assertThat(caseDetailsListByCaseUserRole).hasSize(1);
                assertThat(caseDetailsListByCaseUserRole)
                    .isEqualTo(new CaseTestData().getSearchResultRequestCaseDataListEngland().getCases());
            }
        }
    }

    private static Stream<Arguments> provideGetCaseDetailsByCaseUserRoleTestData() {
        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest = CaseAssignmentUserRolesRequest
            .builder().caseAssignmentUserRoles(
                List.of(CaseAssignmentUserRole.builder()
                            .userId(DUMMY_USER_ID)
                            .caseRole(CASE_ROLE_CREATOR).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
                            .caseDataId("1646225213651533")
                            .build(),
                        CaseAssignmentUserRole.builder()
                            .userId(DUMMY_USER_ID)
                            .caseRole(CASE_ROLE_CREATOR).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
                            .caseDataId("1646225213651512")
                            .build(),
                        CaseAssignmentUserRole.builder()
                            .userId(DUMMY_USER_ID)
                            .caseRole(CASE_ROLE_DEFENDANT).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
                            .caseDataId("1646225213651598")
                            .build())).build();
        List<CaseDetails> emptyCaseDetailsList = new ArrayList<>();
        List<CaseDetails> caseDetailsListAll =
            new CaseTestData().getSearchResultRequestCaseDataListScotland().getCases();
        caseDetailsListAll.addAll(new CaseTestData().getSearchResultRequestCaseDataListEngland().getCases());
        CaseAssignmentUserRolesRequest emptyCaseAssignmentUserRoleRequest =
            CaseAssignmentUserRolesRequest.builder().build();

        return Stream.of(Arguments.of(null, null, CASE_ROLE_CREATOR),
                         Arguments.of(null,
                                      caseAssignmentUserRolesRequest.getCaseAssignmentUserRoles(),
                                      CASE_ROLE_CREATOR),
                         Arguments.of(caseDetailsListAll, null, CASE_ROLE_CREATOR),
                         Arguments.of(emptyCaseDetailsList,
                                      caseAssignmentUserRolesRequest.getCaseAssignmentUserRoles(),
                                      CASE_ROLE_CREATOR),
                         Arguments.of(caseDetailsListAll,
                                      emptyCaseAssignmentUserRoleRequest.getCaseAssignmentUserRoles(),
                                      CASE_ROLE_CREATOR),
                         Arguments.of(caseDetailsListAll,
                                      caseAssignmentUserRolesRequest.getCaseAssignmentUserRoles(),
                                      CASE_ROLE_CREATOR),
                         Arguments.of(caseDetailsListAll,
                                      caseAssignmentUserRolesRequest.getCaseAssignmentUserRoles(),
                                      StringUtils.EMPTY),
                         Arguments.of(caseDetailsListAll,
                                      caseAssignmentUserRolesRequest.getCaseAssignmentUserRoles(),
                                      CASE_ROLE_CREATOR),
                         Arguments.of(caseDetailsListAll,
                                      caseAssignmentUserRolesRequest.getCaseAssignmentUserRoles(),
                                      CASE_ROLE_DEFENDANT)
        );
    }

    @Test
    @SneakyThrows
    void shouldGetUserCase() {
        ReflectionTestUtils.setField(manageCaseRoleService,
                                     CCD_API_URL_PARAMETER_NAME,
                                     CCD_API_URL_PARAMETER_TEST_VALUE);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserInfo(ArgumentMatchers.anyString())).thenReturn(userInfo);
        when(ccdApi.getCase(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            caseTestData.getCaseRequest().getCaseId()
        )).thenReturn(caseTestData.getExpectedDetails());
        CaseRequest caseRequest = CaseRequest.builder()
            .caseId(caseTestData.getCaseRequest().getCaseId()).build();
        CaseAssignmentUserRole caseAssignedUserRole = CaseAssignmentUserRole.builder()
            .caseDataId(caseRequest.getCaseId())
            .caseRole(CASE_USER_ROLE_CREATOR)
            .userId(USER_ID).build();
        CaseAssignedUserRolesResponse caseAssignedUserRolesResponse = CaseAssignedUserRolesResponse.builder()
            .caseAssignedUserRoles(List.of(caseAssignedUserRole))
            .build();
        when(restTemplate.postForObject(ArgumentMatchers.eq(CCD_API_URL_PARAMETER_TEST_VALUE
                                                                + CASE_USER_ROLE_CCD_API_POST_METHOD_NAME),
                                        ArgumentMatchers.any(HttpEntity.class),
                                        ArgumentMatchers.eq(CaseAssignedUserRolesResponse.class)))
            .thenReturn(caseAssignedUserRolesResponse);
        CaseDetails caseDetails = manageCaseRoleService.getUserCaseByCaseUserRole(TEST_SERVICE_AUTH_TOKEN,
                                                                        caseRequest.getCaseId(),
                                                                        CASE_USER_ROLE_CREATOR);
        assertEquals(caseTestData.getExpectedDetails(), caseDetails);
    }

    @Test
    @SneakyThrows
    void shouldGetAllUserCases() {
        ReflectionTestUtils.setField(manageCaseRoleService,
                                     CCD_API_URL_PARAMETER_NAME,
                                     CCD_API_URL_PARAMETER_TEST_VALUE);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        List<CaseDetails> allCaseDetails = caseTestData.getSearchResultRequestCaseDataListScotland().getCases();
        allCaseDetails.addAll(caseTestData.getSearchResultRequestCaseDataListEngland().getCases());
        when(et3Service.getAllUserCasesForET3(TEST_SERVICE_AUTH_TOKEN)).thenReturn(allCaseDetails);
        when(caseService.getAllUserCases(TEST_SERVICE_AUTH_TOKEN)).thenReturn(allCaseDetails);
        when(idamClient.getUserInfo(ArgumentMatchers.anyString())).thenReturn(userInfo);
        List<CaseDetails> expectedCaseDetails = caseTestData.getExpectedCaseDataListCombined();

        when(restTemplate.postForObject(ArgumentMatchers.eq(CCD_API_URL_PARAMETER_TEST_VALUE
                                                                + CASE_USER_ROLE_CCD_API_POST_METHOD_NAME),
                                        ArgumentMatchers.any(HttpEntity.class),
                                        ArgumentMatchers.eq(CaseAssignedUserRolesResponse.class)))
            .thenReturn(expectedCaseAssignedUserRolesResponseCreator);
        List<CaseDetails> caseDetailsForCreator =
            manageCaseRoleService.getUserCasesByCaseUserRole(TEST_SERVICE_AUTH_TOKEN, CASE_USER_ROLE_CREATOR);
        assertThat(caseDetailsForCreator)
            .hasSize(expectedCaseDetails.size()).hasSameElementsAs(expectedCaseDetails);

        when(restTemplate.postForObject(ArgumentMatchers.eq(CCD_API_URL_PARAMETER_TEST_VALUE
                                                                + CASE_USER_ROLE_CCD_API_POST_METHOD_NAME),
                                        ArgumentMatchers.any(HttpEntity.class),
                                        ArgumentMatchers.eq(CaseAssignedUserRolesResponse.class)))
            .thenReturn(expectedCaseAssignedUserRolesResponseDefendant);
        List<CaseDetails> caseDetailsForDefendant =
            manageCaseRoleService.getUserCasesByCaseUserRole(TEST_SERVICE_AUTH_TOKEN, CASE_USER_ROLE_DEFENDANT);
        assertThat(caseDetailsForDefendant)
            .hasSize(expectedCaseDetails.size()).hasSameElementsAs(expectedCaseDetails);
    }

}
