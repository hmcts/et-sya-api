package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.enums.RespondentSolicitorType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CCD_API_POST_METHOD_NAME;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CLAIMANT_SOLICITOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.REMOVE_OWN_REPRESENTATIVE;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_CASE_SUBMITTED;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_ET3_FORM;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_ID_LONG;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
class ManageCaseRoleServiceTest {

    @Mock
    RestTemplate restTemplate;
    @Mock
    CoreCaseDataApi ccdApi;
    @Mock
    AdminUserService adminUserService;
    @Mock
    AuthTokenGenerator authTokenGenerator;
    @Mock
    IdamClient idamClient;
    @Mock
    ET3Service et3Service;
    @Mock
    CaseService caseService;
    @Mock
    CaseDetailsConverter caseDetailsConverter;

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
    private static final String TEST_RESPONDENT_ORGANISATION_NAME = "Test Respondent Organisation -1-";
    private static final String CLAIMANT_FIRST_NAMES = "Claimant First Names";
    private static final String CLAIMANT_LAST_NAME = "Claimant Last Name";
    private static final String DUMMY_CASE_SUBMISSION_REFERENCE = "1234567890123456";
    private static final String DUMMY_USER_ID = "123456789012345678901234567890";
    private static final String DUMMY_AUTHORISATION_TOKEN = "dummy_authorisation_token";
    private static final String TEST_SERVICE_AUTH_TOKEN = "Bearer TestServiceAuth";
    private static final String USER_CASE_ROLE_DEFENDANT = "[DEFENDANT]";
    private static final String SCOTLAND_CASE_TYPE = "ET_Scotland";
    private static final String AAC_URL_PARAMETER_NAME = "aacUrl";
    private static final String AAC_URL_PARAMETER_TEST_VALUE = "https://test.url.com";
    private static final String CCD_API_URL_PARAMETER_NAME = "ccdApiUrl";
    private static final String CCD_API_URL_PARAMETER_TEST_VALUE = "https://test.url.com";
    private static final String EXPECTED_EMPTY_CASE_DETAILS_EXCEPTION_MESSAGE =
        "java.lang.Exception: Unable to get user cases because not able to create aacApiUrl with the given "
            + "caseDetails and authorization data";

    @BeforeEach
    void setup() {
        caseTestData = new CaseTestData();
        manageCaseRoleService = new ManageCaseRoleService(adminUserService,
                                                          authTokenGenerator,
                                                          idamClient,
                                                          restTemplate,
                                                          ccdApi,
                                                          et3Service,
                                                          caseService,
                                                          caseDetailsConverter);
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
    @MethodSource("provideModifyUserCaseRolesForRespondentsTestData")
    @SneakyThrows
    void theModifyUserCaseRolesForRespondents(ModifyCaseUserRolesRequest modifyCaseUserRolesRequest,
                                              String modificationType) {
        if (StringUtils.isEmpty(modificationType)
            || !MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)
            && !MODIFICATION_TYPE_REVOKE.equals(modificationType)) {
            ManageCaseRoleException exception = assertThrows(ManageCaseRoleException.class, () ->
                manageCaseRoleService.modifyUserCaseRolesForRespondents(DUMMY_AUTHORISATION_TOKEN,
                                                                        modifyCaseUserRolesRequest,
                                                                        modificationType));
            assertThat(exception.getMessage()).isEqualTo(INVALID_MODIFICATION_TYPE_EXPECTED_EXCEPTION_MESSAGE);
            return;
        }
        if (isModifyCaseUserRolesRequestInvalid(modifyCaseUserRolesRequest)) {
            ManageCaseRoleException exception = assertThrows(ManageCaseRoleException.class, () ->
                manageCaseRoleService.modifyUserCaseRolesForRespondents(TestConstants.DUMMY_AUTHORISATION_TOKEN,
                                                                        modifyCaseUserRolesRequest,
                                                                        modificationType));
            assertThat(exception.getMessage()).isEqualTo(INVALID_CASE_ROLE_REQUEST_EXCEPTION_MESSAGE);
            return;
        }
        if (isAnyOfTheModifyCaseUserRoleInvalid(modifyCaseUserRolesRequest)) {
            ManageCaseRoleException exception = assertThrows(ManageCaseRoleException.class, () ->
                manageCaseRoleService.modifyUserCaseRolesForRespondents(TestConstants.DUMMY_AUTHORISATION_TOKEN,
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
                                                .getFirst().getCaseRole())) {
            setExpectedDetails(modifyCaseUserRolesRequest, modificationType, expectedCaseDetails);
        }
        HttpMethod httpMethod = MODIFICATION_TYPE_REVOKE.equals(modificationType) ? HttpMethod.DELETE : HttpMethod.POST;
        when(restTemplate.exchange(ArgumentMatchers.anyString(),
                                   eq(httpMethod),
                                   any(),
                                   eq(CaseAssignmentUserRolesResponse.class)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        assertDoesNotThrow(() -> manageCaseRoleService.modifyUserCaseRolesForRespondents(
            TestConstants.DUMMY_AUTHORISATION_TOKEN,
            modifyCaseUserRolesRequest,
            modificationType));
    }

    private void setExpectedDetails(ModifyCaseUserRolesRequest modifyCaseUserRolesRequest,
                                    String modificationType,
                                    CaseDetails expectedCaseDetails) {
        if (ManageCaseRoleConstants.MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)) {
            CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(expectedCaseDetails.getData());
            caseData.getRespondentCollection().getFirst().getValue().setRespondentName(
                TestConstants.TEST_RESPONDENT_NAME);
            expectedCaseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        }
        if (ManageCaseRoleConstants.MODIFICATION_TYPE_REVOKE.equals(modificationType)) {
            CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(expectedCaseDetails.getData());
            caseData.getRespondentCollection().getFirst().getValue().setIdamId(USER_ID);
            expectedCaseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        }
        when(et3Service.findCaseBySubmissionReference(modifyCaseUserRolesRequest
                                                          .getModifyCaseUserRoles()
                                                          .getFirst().getCaseDataId())).thenReturn(expectedCaseDetails);
        when(et3Service.updateSubmittedCaseWithCaseDetailsForCaseAssignment(
            DUMMY_AUTHORISATION_TOKEN,
            expectedCaseDetails,
            UPDATE_ET3_FORM
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

    private static Stream<Arguments> provideModifyUserCaseRolesForRespondentsTestData() {
        ModifyCaseUserRole modifyCaseUserRoleValidRoleDefendant = ModifyCaseUserRole.builder()
            .userId(USER_ID)
            .caseDataId(CASE_ID)
            .caseRole(CASE_ROLE_DEFENDANT)
            .caseTypeId(TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES)
            .respondentName(RESPONDENT_NAME)
            .build();
        ModifyCaseUserRole modifyCaseUserRoleValidRoleCreator = ModifyCaseUserRole.builder()
            .userId(USER_ID)
            .caseDataId(CASE_ID)
            .caseRole(CASE_ROLE_CREATOR)
            .caseTypeId(TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES)
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
                                TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES,
                                elasticSearchQuery))
            .thenReturn(SearchResult.builder().cases(List.of(CaseDetails.builder()
                                                                 .caseTypeId(
                                                                     TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES)
                                                                 .id(Long.parseLong(CASE_SUBMISSION_REFERENCE))
                                                                 .state(TestConstants.TEST_CASE_STATE_ACCEPTED)
                                                                 .build())).total(1).build());
        assertThat(manageCaseRoleService.findCaseForRoleModification(findCaseForRoleModificationRequest,
                                                                     TEST_SERVICE_AUTH_TOKEN)).isNotNull();
        assertThat(manageCaseRoleService.findCaseForRoleModification(findCaseForRoleModificationRequest,
                                                                     TEST_SERVICE_AUTH_TOKEN).getId().toString())
            .isEqualTo(CASE_SUBMISSION_REFERENCE);
        assertThat(manageCaseRoleService.findCaseForRoleModification(findCaseForRoleModificationRequest,
                                                                     TEST_SERVICE_AUTH_TOKEN).getCaseTypeId())
            .isEqualTo(TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES);

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
                                TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES,
                                elasticSearchQuery)).thenReturn(SearchResult.builder().build());
        when(ccdApi.searchCases(TEST_SERVICE_AUTH_TOKEN,
                                TEST_SERVICE_AUTH_TOKEN,
                                SCOTLAND_CASE_TYPE,
                                elasticSearchQuery))
            .thenReturn(SearchResult.builder().cases(List.of(CaseDetails.builder()
                                                                 .caseTypeId(SCOTLAND_CASE_TYPE)
                                                                 .id(Long.parseLong(CASE_SUBMISSION_REFERENCE))
                                                                 .state(TestConstants.TEST_CASE_STATE_ACCEPTED)
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
                                TestConstants.TEST_CASE_TYPE_ID_ENGLAND_WALES,
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
        assertThat(actualModifyCaseUserRolesRequest.getModifyCaseUserRoles().getFirst().getUserId())
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
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(CaseAssignedUserRolesResponse.class)))
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
        when(restTemplate.postForObject(
            eq(CCD_API_URL_PARAMETER_TEST_VALUE
                   + CASE_USER_ROLE_CCD_API_POST_METHOD_NAME),
            any(HttpEntity.class),
            eq(CaseAssignedUserRolesResponse.class)))
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
        when(restTemplate.postForObject(
            eq(CCD_API_URL_PARAMETER_TEST_VALUE
                   + CASE_USER_ROLE_CCD_API_POST_METHOD_NAME),
            any(HttpEntity.class),
            eq(CaseAssignedUserRolesResponse.class)))
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
        when(caseService.getAllUserCases(TEST_SERVICE_AUTH_TOKEN)).thenReturn(allCaseDetails);
        when(idamClient.getUserInfo(ArgumentMatchers.anyString())).thenReturn(userInfo);
        List<CaseDetails> expectedCaseDetails = caseTestData.getExpectedCaseDataListCombined();

        when(restTemplate.postForObject(
            eq(CCD_API_URL_PARAMETER_TEST_VALUE
                   + CASE_USER_ROLE_CCD_API_POST_METHOD_NAME),
            any(HttpEntity.class),
            eq(CaseAssignedUserRolesResponse.class)))
            .thenReturn(expectedCaseAssignedUserRolesResponseCreator);
        List<CaseDetails> caseDetailsForCreator =
            manageCaseRoleService.getUserCasesByCaseUserRole(TEST_SERVICE_AUTH_TOKEN, CASE_USER_ROLE_CREATOR);
        assertThat(caseDetailsForCreator)
            .hasSize(expectedCaseDetails.size()).hasSameElementsAs(expectedCaseDetails);

        when(restTemplate.postForObject(
            eq(CCD_API_URL_PARAMETER_TEST_VALUE
                   + CASE_USER_ROLE_CCD_API_POST_METHOD_NAME),
            any(HttpEntity.class),
            eq(CaseAssignedUserRolesResponse.class)))
            .thenReturn(expectedCaseAssignedUserRolesResponseDefendant);
        List<CaseDetails> caseDetailsForDefendant =
            manageCaseRoleService.getUserCasesByCaseUserRole(TEST_SERVICE_AUTH_TOKEN, CASE_USER_ROLE_DEFENDANT);
        assertThat(caseDetailsForDefendant)
            .hasSize(expectedCaseDetails.size()).hasSameElementsAs(expectedCaseDetails);
    }

    @Test
    void theRemoveClaimantRepresentativeFromCaseData() {
        CaseDetails caseDetails = new CaseTestData().getCaseDetailsWithCaseData();
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        caseData.setClaimantRepresentedQuestion(YES);
        caseData.getRepresentativeClaimantType().setRepresentativeId(USER_ID);
        caseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails).eventId(UPDATE_CASE_SUBMITTED.name()).token(DUMMY_AUTHORISATION_TOKEN).build();
        when(ccdApi.startEventForCitizen(
            DUMMY_AUTHORISATION_TOKEN,
            DUMMY_AUTHORISATION_TOKEN,
            userInfo.getUid(),
            EMPLOYMENT,
            caseDetails.getCaseTypeId(),
            caseDetails.getId().toString(),
            UPDATE_CASE_SUBMITTED.name()
        )).thenReturn(startEventResponse);
        when(caseDetailsConverter.caseDataContent(eq(startEventResponse), any(CaseData.class)))
            .thenReturn(null);
        when(caseService.submitUpdate(
            DUMMY_AUTHORISATION_TOKEN,
            caseDetails.getId().toString(),
            null,
            caseDetails.getCaseTypeId()
        )).thenReturn(caseDetails);
        when(idamClient.getUserInfo(DUMMY_AUTHORISATION_TOKEN)).thenReturn(new CaseTestData().getUserInfo());
        when(authTokenGenerator.generate()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        assertDoesNotThrow(() -> manageCaseRoleService.removeClaimantRepresentativeFromCaseData(
            DUMMY_AUTHORISATION_TOKEN, caseDetails));
    }

    @Test
    @SneakyThrows
    void theFetchCaseUserAssignmentsByCaseId() {
        ReflectionTestUtils.setField(manageCaseRoleService,
                                     CCD_API_URL_PARAMETER_NAME,
                                     CCD_API_URL_PARAMETER_TEST_VALUE);
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
            .caseUserAssignments(List.of(CaseUserAssignment.builder()
                                             .caseId(CASE_ID)
                                             .userId(USER_ID)
                                             .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR)
                                             .build()))
            .build();
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        when(restTemplate.exchange(anyString(),
                                   eq(HttpMethod.GET),
                                   any(HttpEntity.class),
                                   eq(CaseUserAssignmentData.class))).thenReturn(
                                       new ResponseEntity<>(caseUserAssignmentData, HttpStatus.OK));
        CaseUserAssignmentData actualCaseUserAssignmentData =
            manageCaseRoleService.fetchCaseUserAssignmentsByCaseId(CASE_ID);
        assertThat(actualCaseUserAssignmentData.getCaseUserAssignments().getFirst().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @SneakyThrows
    void theFindCaseUserAssignmentsByRoleAndCase() {
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        when(restTemplate.exchange(anyString(),
                                   eq(HttpMethod.GET),
                                   any(HttpEntity.class),
                                   eq(CaseUserAssignmentData.class))).thenReturn(
                                       new ResponseEntity<>(null, HttpStatus.OK));
        CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID_LONG).build();
        ManageCaseRoleException caseRoleException = assertThrows(ManageCaseRoleException.class, () ->
            manageCaseRoleService.findCaseUserAssignmentsByRoleAndCase(
                CASE_USER_ROLE_CLAIMANT_SOLICITOR, caseDetails));
        assertThat(caseRoleException.getMessage())
            .isEqualTo("java.lang.Exception: Case user roles not found for caseId: 1234567890123456");
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
            .caseUserAssignments(List.of(CaseUserAssignment.builder()
                                             .caseId(CASE_ID)
                                             .userId(USER_ID)
                                             .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR)
                                             .build()))
            .build();
        when(restTemplate.exchange(anyString(),
                                   eq(HttpMethod.GET),
                                   any(HttpEntity.class),
                                   eq(CaseUserAssignmentData.class))).thenReturn(
                                       new ResponseEntity<>(caseUserAssignmentData, HttpStatus.OK));
        assertThat(manageCaseRoleService.findCaseUserAssignmentsByRoleAndCase(CASE_USER_ROLE_CREATOR,
                                                                              CaseDetails.builder()
                                                                                  .id(TEST_CASE_ID_LONG).build()))
            .isNullOrEmpty();
        assertThat(manageCaseRoleService.findCaseUserAssignmentsByRoleAndCase(CASE_USER_ROLE_CLAIMANT_SOLICITOR,
                                                                              CaseDetails.builder()
                                                                                  .id(TEST_CASE_ID_LONG).build()))
            .isNotNull();
    }

    @Test
    @SneakyThrows
    void theRevokeCaseUserRole() {
        ReflectionTestUtils.setField(manageCaseRoleService,
                                     CCD_API_URL_PARAMETER_NAME,
                                     CCD_API_URL_PARAMETER_TEST_VALUE);
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
            .caseUserAssignments(List.of(CaseUserAssignment.builder()
                                             .caseId(CASE_ID)
                                             .userId(USER_ID)
                                             .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR)
                                             .build()))
            .build();
        String getCaseUserAssignmentsUrl = "https://test.url.com/case-users?case_ids=1234567890123456";
        when(restTemplate.exchange(eq(getCaseUserAssignmentsUrl),
                                   eq(HttpMethod.GET),
                                   any(HttpEntity.class),
                                   eq(CaseUserAssignmentData.class))).thenReturn(
                                       new ResponseEntity<>(caseUserAssignmentData, HttpStatus.OK));
        CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID_LONG).build();
        // When the case user role is being tried to be revoked with invalid case user role
        ManageCaseRoleException manageCaseRoleException =
            assertThrows(ManageCaseRoleException.class,() -> manageCaseRoleService
                .revokeCaseUserRole(caseDetails, CASE_USER_ROLE_CREATOR));
        String expectedMessage = "java.lang.Exception: Case user roles not found for caseId: 1234567890123456";
        assertThat(manageCaseRoleException.getMessage()).isEqualTo(expectedMessage);
        // When the case user role is successfully revoked
        assertDoesNotThrow(() -> manageCaseRoleService
            .revokeCaseUserRole(caseDetails, CASE_USER_ROLE_CLAIMANT_SOLICITOR));
    }

    @Test
    @SneakyThrows
    void theRevokeClaimantSolicitorRole() {
        // This test is for the revokeClaimantSolicitorRole method which is used to revoke the claimant solicitor
        // role from a case. It sets up the necessary mocks and verifies that the method can be called without throwing
        // any exceptions.
        ReflectionTestUtils.setField(manageCaseRoleService,
                                     CCD_API_URL_PARAMETER_NAME,
                                     CCD_API_URL_PARAMETER_TEST_VALUE);
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        CaseAssignedUserRolesResponse caseAssignedUserRolesResponse = CaseAssignedUserRolesResponse.builder()
            .caseAssignedUserRoles(List.of(CaseAssignmentUserRole.builder()
                                               .caseDataId(TEST_CASE_ID_STRING)
                                               .userId(DUMMY_USER_ID)
                                               .caseRole(CASE_USER_ROLE_CREATOR)
                                               .build()))
            .build();
        when(restTemplate.postForObject(eq(CCD_API_URL_PARAMETER_TEST_VALUE
                                               + ManageCaseRoleConstants.CASE_USER_ROLE_CCD_API_POST_METHOD_NAME),
                                        any(HttpEntity.class),
                                        eq(CaseAssignedUserRolesResponse.class)))
            .thenReturn(caseAssignedUserRolesResponse);
        CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID_LONG)
            .data(new CaseTestData().getCaseDetails().getData()).build();
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        when(idamClient.getUserInfo(DUMMY_AUTHORISATION_TOKEN)).thenReturn(new CaseTestData().getUserInfo());
        when(ccdApi.getCase(DUMMY_AUTHORISATION_TOKEN, DUMMY_AUTHORISATION_TOKEN, TEST_CASE_ID_STRING))
            .thenReturn(caseDetails);
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
            .caseUserAssignments(List.of(CaseUserAssignment.builder()
                                             .caseId(TEST_CASE_ID_STRING)
                                             .userId(DUMMY_USER_ID)
                                             .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR)
                                             .build()))
            .build();
        when(restTemplate.exchange(eq("https://test.url.com/case-users?case_ids=1234567890123456"),
                                   eq(HttpMethod.GET),
                                   any(HttpEntity.class),
                                   eq(CaseUserAssignmentData.class))).thenReturn(
                                       new ResponseEntity<>(caseUserAssignmentData, HttpStatus.OK));
        when(restTemplate.exchange(eq("https://test.url.com/case-users"),
                                   eq(HttpMethod.DELETE),
                                   any(HttpEntity.class),
                                   eq(CaseAssignmentUserRolesResponse.class))).thenReturn(
                                       new ResponseEntity<>(CaseAssignmentUserRolesResponse.builder().build(),
                                                            HttpStatus.OK));
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails).eventId(UPDATE_CASE_SUBMITTED.name()).token(DUMMY_AUTHORISATION_TOKEN).build();
        when(ccdApi.startEventForCitizen(
            DUMMY_AUTHORISATION_TOKEN,
            DUMMY_AUTHORISATION_TOKEN,
            userInfo.getUid(),
            EMPLOYMENT,
            caseDetails.getCaseTypeId(),
            caseDetails.getId().toString(),
            UPDATE_CASE_SUBMITTED.name()
        )).thenReturn(startEventResponse);
        when(caseDetailsConverter.caseDataContent(eq(startEventResponse), any(CaseData.class)))
            .thenReturn(null);
        when(caseService.submitUpdate(
            DUMMY_AUTHORISATION_TOKEN,
            caseDetails.getId().toString(),
            null,
            caseDetails.getCaseTypeId()
        )).thenReturn(caseDetails);
        when(idamClient.getUserInfo(DUMMY_AUTHORISATION_TOKEN)).thenReturn(new CaseTestData().getUserInfo());
        when(authTokenGenerator.generate()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        assertDoesNotThrow(() -> manageCaseRoleService.revokeClaimantSolicitorRole(
            DUMMY_AUTHORISATION_TOKEN, TEST_CASE_ID_STRING));
        // When invalid case role then should not return any case details and should throw exception
        CaseAssignedUserRolesResponse caseAssignedUserRolesResponseInvalid = CaseAssignedUserRolesResponse.builder()
            .caseAssignedUserRoles(List.of(CaseAssignmentUserRole.builder()
                                               .caseDataId(TEST_CASE_ID_STRING)
                                               .userId(DUMMY_USER_ID)
                                               .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR)
                                               .build()))
            .build();
        when(restTemplate.postForObject(eq(CCD_API_URL_PARAMETER_TEST_VALUE
                                               + ManageCaseRoleConstants.CASE_USER_ROLE_CCD_API_POST_METHOD_NAME),
                                        any(HttpEntity.class),
                                        eq(CaseAssignedUserRolesResponse.class)))
            .thenReturn(caseAssignedUserRolesResponseInvalid);
        assertThrows(ManageCaseRoleException.class, () -> manageCaseRoleService.revokeClaimantSolicitorRole(
            DUMMY_AUTHORISATION_TOKEN, TEST_CASE_ID_STRING));
    }

    @Test
    @SneakyThrows
    void theRemoveRespondentRepresentativeFromCaseData() {
        // This test is for the removeRespondentRepresentativeFromCaseData method which is used to remove the
        // respondent representative from a case's data. It sets up the necessary mocks and verifies that the method
        // can be called without throwing any exceptions.
        CaseDetails caseDetails = new CaseTestData().getCaseDetailsWithCaseData();
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        caseData.getRespondentCollection().getFirst().setId(USER_ID);
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().value(
            RepresentedTypeR.builder().respondentId(USER_ID).build()).build()));
        caseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        when(idamClient.getUserInfo(DUMMY_AUTHORISATION_TOKEN)).thenReturn(userInfo);
        when(authTokenGenerator.generate()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails).eventId(UPDATE_CASE_SUBMITTED.name()).token(DUMMY_AUTHORISATION_TOKEN).build();
        when(ccdApi.startEventForCitizen(
            DUMMY_AUTHORISATION_TOKEN,
            DUMMY_AUTHORISATION_TOKEN,
            userInfo.getUid(),
            EMPLOYMENT,
            caseDetails.getCaseTypeId(),
            caseDetails.getId().toString(),
            REMOVE_OWN_REPRESENTATIVE.name()
        )).thenReturn(startEventResponse);
        when(caseDetailsConverter.caseDataContent(eq(startEventResponse), any(CaseData.class)))
            .thenReturn(null);
        when(ccdApi.submitEventForCitizen(
            DUMMY_AUTHORISATION_TOKEN,
            DUMMY_AUTHORISATION_TOKEN,
            userInfo.getUid(),
            EMPLOYMENT,
            caseDetails.getCaseTypeId(),
            caseDetails.getId().toString(),
            true,
            null
        ))
            .thenReturn(caseDetails);
        CaseDetails updatedCaseDetails = manageCaseRoleService.removeRespondentRepresentativeFromCaseData(
            DUMMY_AUTHORISATION_TOKEN, caseDetails, "0", "[SOLICITORA]");
        assertThat(updatedCaseDetails).isNotNull();
    }

    @Test
    @SneakyThrows
    void theRevokeRespondentSolicitorRole() {
        // This test is for the revokeClaimantSolicitorRole method which is used to revoke the claimant solicitor
        // role from a case. It sets up the necessary mocks and verifies that the method can be called without throwing
        // any exceptions.
        ReflectionTestUtils.setField(manageCaseRoleService,
                                     CCD_API_URL_PARAMETER_NAME,
                                     CCD_API_URL_PARAMETER_TEST_VALUE);
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        CaseAssignedUserRolesResponse caseAssignedUserRolesResponse = CaseAssignedUserRolesResponse.builder()
            .caseAssignedUserRoles(List.of(CaseAssignmentUserRole.builder()
                                               .caseDataId(TEST_CASE_ID_STRING)
                                               .userId(DUMMY_USER_ID)
                                               .caseRole(CASE_ROLE_DEFENDANT)
                                               .build()))
            .build();
        when(restTemplate.postForObject(eq(CCD_API_URL_PARAMETER_TEST_VALUE
                                               + ManageCaseRoleConstants.CASE_USER_ROLE_CCD_API_POST_METHOD_NAME),
                                        any(HttpEntity.class),
                                        eq(CaseAssignedUserRolesResponse.class)))
            .thenReturn(caseAssignedUserRolesResponse);
        CaseDetails caseDetails = new CaseTestData().getCaseDetailsWithCaseData();
        caseDetails.setId(TEST_CASE_ID_LONG);
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        caseData.getRespondentCollection().getFirst().setId(USER_ID);
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().value(
            RepresentedTypeR.builder().respondentId(USER_ID).build()).build()));
        caseData.setNoticeOfChangeAnswers0(
            NoticeOfChangeAnswers.builder().respondentName(TEST_RESPONDENT_ORGANISATION_NAME).build());
        caseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        when(authTokenGenerator.generate()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        when(idamClient.getUserInfo(DUMMY_AUTHORISATION_TOKEN)).thenReturn(new CaseTestData().getUserInfo());
        when(ccdApi.getCase(DUMMY_AUTHORISATION_TOKEN, DUMMY_AUTHORISATION_TOKEN, TEST_CASE_ID_STRING))
            .thenReturn(caseDetails);
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
            .caseUserAssignments(List.of(CaseUserAssignment.builder()
                                             .caseId(TEST_CASE_ID_STRING)
                                             .userId(DUMMY_USER_ID)
                                             .caseRole(RespondentSolicitorType.SOLICITORA.getLabel())
                                             .build()))
            .build();
        when(restTemplate.exchange(eq("https://test.url.com/case-users?case_ids=1234567890123456"),
                                   eq(HttpMethod.GET),
                                   any(HttpEntity.class),
                                   eq(CaseUserAssignmentData.class))).thenReturn(
                                       new ResponseEntity<>(caseUserAssignmentData, HttpStatus.OK));
        when(restTemplate.exchange(eq("https://test.url.com/case-users"),
                                   eq(HttpMethod.DELETE),
                                   any(HttpEntity.class),
                                   eq(CaseAssignmentUserRolesResponse.class))).thenReturn(
                                       new ResponseEntity<>(CaseAssignmentUserRolesResponse.builder().build(),
                                 HttpStatus.OK));
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails).eventId(UPDATE_CASE_SUBMITTED.name()).token(DUMMY_AUTHORISATION_TOKEN).build();
        when(ccdApi.startEventForCitizen(
            DUMMY_AUTHORISATION_TOKEN,
            DUMMY_AUTHORISATION_TOKEN,
            userInfo.getUid(),
            EMPLOYMENT,
            caseDetails.getCaseTypeId(),
            caseDetails.getId().toString(),
            REMOVE_OWN_REPRESENTATIVE.name()
        )).thenReturn(startEventResponse);
        when(caseDetailsConverter.caseDataContent(eq(startEventResponse), any(CaseData.class)))
            .thenReturn(null);
        when(ccdApi.submitEventForCitizen(
            DUMMY_AUTHORISATION_TOKEN,
            DUMMY_AUTHORISATION_TOKEN,
            userInfo.getUid(),
            EMPLOYMENT,
            caseDetails.getCaseTypeId(),
            caseDetails.getId().toString(),
            true,
            null
        ))
            .thenReturn(caseDetails);
        assertThat(manageCaseRoleService.revokeRespondentSolicitorRole(
            DUMMY_AUTHORISATION_TOKEN, TEST_CASE_ID_STRING, NumberUtils.INTEGER_ZERO.toString()))
            .isNotNull().isEqualTo(caseDetails);
        // When invalid case role then should not return any case details and should throw exception
        CaseAssignedUserRolesResponse caseAssignedUserRolesResponseInvalid = CaseAssignedUserRolesResponse.builder()
            .caseAssignedUserRoles(List.of(CaseAssignmentUserRole.builder()
                                               .caseDataId(TEST_CASE_ID_STRING)
                                               .userId(DUMMY_USER_ID)
                                               .caseRole(USER_CASE_ROLE_DEFENDANT)
                                               .build()))
            .build();
        when(restTemplate.postForObject(eq(CCD_API_URL_PARAMETER_TEST_VALUE
                                               + ManageCaseRoleConstants.CASE_USER_ROLE_CCD_API_POST_METHOD_NAME),
                                        any(HttpEntity.class),
                                        eq(CaseAssignedUserRolesResponse.class)))
            .thenReturn(caseAssignedUserRolesResponseInvalid);
        assertThrows(ManageCaseRoleException.class, () -> manageCaseRoleService.revokeClaimantSolicitorRole(
            DUMMY_AUTHORISATION_TOKEN, TEST_CASE_ID_STRING));
    }
}
