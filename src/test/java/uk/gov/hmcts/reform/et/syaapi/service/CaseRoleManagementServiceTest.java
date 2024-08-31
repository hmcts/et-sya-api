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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignedUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseRoleManagementException;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;
import uk.gov.hmcts.reform.et.syaapi.search.ElasticSearchQueryBuilder;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
class CaseRoleManagementServiceTest {

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

    private CaseRoleManagementService caseRoleManagementService;
    private UserInfo userInfo;
    private CaseAssignmentUserRole caseAssignmentUserRole1;
    private CaseAssignmentUserRole caseAssignmentUserRole2;
    private CaseAssignmentUserRole caseAssignmentUserRole3;

    private static final String INVALID_MODIFICATION_TYPE_EXPECTED_EXCEPTION_MESSAGE = "Invalid modification type";
    private static final String INVALID_CASE_ROLE_REQUEST_EXCEPTION_MESSAGE =
        "java.lang.Exception: Request to modify roles is empty";
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

    @BeforeEach
    void setup() {
        caseRoleManagementService = new CaseRoleManagementService(
            adminUserService, restTemplate, authTokenGenerator, ccdApi, idamClient);
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
    }

    @ParameterizedTest
    @MethodSource("provideModifyUserCaseRolesTestData")
    @SneakyThrows
    void theModifyUserCaseRoles(CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest,
                                 String modificationType) {
        if (StringUtils.isEmpty(modificationType)
            || !MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)
            && !MODIFICATION_TYPE_REVOKE.equals(modificationType)) {
            CaseRoleManagementException exception = assertThrows(CaseRoleManagementException.class, () ->
                caseRoleManagementService.modifyUserCaseRoles(caseAssignmentUserRolesRequest, modificationType));
            assertThat(exception.getMessage()).isEqualTo(INVALID_MODIFICATION_TYPE_EXPECTED_EXCEPTION_MESSAGE);
            return;
        }
        if (ObjectUtils.isEmpty(caseAssignmentUserRolesRequest)
            || CollectionUtils.isEmpty(caseAssignmentUserRolesRequest.getCaseAssignmentUserRoles())) {
            CaseRoleManagementException exception = assertThrows(CaseRoleManagementException.class, () ->
                caseRoleManagementService.modifyUserCaseRoles(caseAssignmentUserRolesRequest, modificationType));
            assertThat(exception.getMessage()).isEqualTo(INVALID_CASE_ROLE_REQUEST_EXCEPTION_MESSAGE);
            return;

        }
        HttpMethod httpMethod = MODIFICATION_TYPE_REVOKE.equals(modificationType) ? HttpMethod.DELETE : HttpMethod.POST;
        when(restTemplate.exchange(anyString(), eq(httpMethod), any(), eq(CaseAssignmentUserRolesResponse.class)))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(adminUserService.getAdminUserToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        assertDoesNotThrow(() -> caseRoleManagementService
            .modifyUserCaseRoles(caseAssignmentUserRolesRequest, modificationType));
    }

    private static Stream<Arguments> provideModifyUserCaseRolesTestData() {
        CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole.builder()
            .userId(USER_ID)
            .caseDataId(CASE_ID)
            .caseRole(CASE_ROLE_DEFENDANT)
            .build();
        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest = CaseAssignmentUserRolesRequest
            .builder().caseAssignmentUserRoles(List.of(caseAssignmentUserRole)).build();
        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequestEmptyCaseAssignmentUserRole =
            CaseAssignmentUserRolesRequest.builder().build();

        return Stream.of(Arguments.of(caseAssignmentUserRolesRequest, MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseAssignmentUserRolesRequest, MODIFICATION_TYPE_REVOKE),
                         Arguments.of(null, MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(null, MODIFICATION_TYPE_REVOKE),
                         Arguments.of(caseAssignmentUserRolesRequestEmptyCaseAssignmentUserRole,
                                      MODIFICATION_TYPE_ASSIGNMENT),
                         Arguments.of(caseAssignmentUserRolesRequestEmptyCaseAssignmentUserRole,
                                      MODIFICATION_TYPE_REVOKE)
            );
    }

    @Test
    void theFindCaseForRoleModificationForEnglandWales() {
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest =
            FindCaseForRoleModificationRequest.builder()
                .caseSubmissionReference(CASE_SUBMISSION_REFERENCE)
                .respondentName(RESPONDENT_NAME)
                .claimantFirstNames(CLAIMANT_FIRST_NAMES)
                .claimantLastName(CLAIMANT_LAST_NAME)
                .build();
        String elasticSearchQuery = ElasticSearchQueryBuilder.buildElasticSearchQueryForRoleModification(
            findCaseForRoleModificationRequest
        );
        when(adminUserService.getAdminUserToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApi.searchCases(TEST_SERVICE_AUTH_TOKEN,
                                TEST_SERVICE_AUTH_TOKEN,
                                ENGLAND_CASE_TYPE,
                                elasticSearchQuery))
            .thenReturn(SearchResult.builder().cases(List.of(CaseDetails.builder()
                                                                 .caseTypeId(ENGLAND_CASE_TYPE)
                                                                 .id(Long.parseLong(CASE_SUBMISSION_REFERENCE))
                                                                 .build())).total(1).build());
        assertThat(caseRoleManagementService.findCaseForRoleModification(findCaseForRoleModificationRequest))
            .isNotNull();
        assertThat(caseRoleManagementService.findCaseForRoleModification(findCaseForRoleModificationRequest)
                       .getId().toString()).isEqualTo(CASE_SUBMISSION_REFERENCE);
        assertThat(caseRoleManagementService.findCaseForRoleModification(findCaseForRoleModificationRequest)
                       .getCaseTypeId()).isEqualTo(ENGLAND_CASE_TYPE);

    }

    @Test
    void theFindCaseForRoleModificationForScotland() {
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest =
            FindCaseForRoleModificationRequest.builder()
                .caseSubmissionReference(CASE_SUBMISSION_REFERENCE)
                .respondentName(RESPONDENT_NAME)
                .claimantFirstNames(CLAIMANT_FIRST_NAMES)
                .claimantLastName(CLAIMANT_LAST_NAME)
                .build();
        String elasticSearchQuery = ElasticSearchQueryBuilder.buildElasticSearchQueryForRoleModification(
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
                                elasticSearchQuery))
            .thenReturn(SearchResult.builder().cases(List.of(CaseDetails.builder()
                                                                 .caseTypeId(SCOTLAND_CASE_TYPE)
                                                                 .id(Long.parseLong(CASE_SUBMISSION_REFERENCE))
                                                                 .build())).total(1).build());
        assertThat(caseRoleManagementService.findCaseForRoleModification(findCaseForRoleModificationRequest))
            .isNotNull();
        assertThat(caseRoleManagementService.findCaseForRoleModification(findCaseForRoleModificationRequest)
                       .getId().toString()).isEqualTo(CASE_SUBMISSION_REFERENCE);
        assertThat(caseRoleManagementService.findCaseForRoleModification(findCaseForRoleModificationRequest)
                       .getCaseTypeId()).isEqualTo(SCOTLAND_CASE_TYPE);
    }

    @Test
    void theFindCaseForRoleModificationNotFound() {
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest =
            FindCaseForRoleModificationRequest.builder()
                .caseSubmissionReference(CASE_SUBMISSION_REFERENCE)
                .respondentName(RESPONDENT_NAME)
                .claimantFirstNames(CLAIMANT_FIRST_NAMES)
                .claimantLastName(CLAIMANT_LAST_NAME)
                .build();
        String elasticSearchQuery = ElasticSearchQueryBuilder.buildElasticSearchQueryForRoleModification(
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
        assertThat(caseRoleManagementService.findCaseForRoleModification(findCaseForRoleModificationRequest))
            .isNull();
    }

    @Test
    void theGenerateCaseAssignmentUserRolesRequestWithUserIds() {
        UserInfo userInfo = new CaseTestData().getUserInfo();
        CaseAssignmentUserRole caseAssignmentUserRoleWithoutUserId = CaseAssignmentUserRole.builder()
            .userId(null)
            .caseRole(USER_CASE_ROLE_DEFENDANT).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .build();
        CaseAssignmentUserRole caseAssignmentUserRoleWithUserId = CaseAssignmentUserRole.builder()
            .userId(DUMMY_USER_ID)
            .caseRole(USER_CASE_ROLE_DEFENDANT).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .build();
        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRoles(List.of(caseAssignmentUserRoleWithoutUserId, caseAssignmentUserRoleWithUserId))
            .build();
        when(idamClient.getUserInfo(DUMMY_AUTHORISATION_TOKEN)).thenReturn(userInfo);
        CaseAssignmentUserRolesRequest  actualCaseAssignmentUserRolesRequest = caseRoleManagementService
            .generateCaseAssignmentUserRolesRequestWithUserIds(
                DUMMY_AUTHORISATION_TOKEN, caseAssignmentUserRolesRequest);
        assertThat(actualCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles()).hasSize(2);
        assertThat(actualCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().get(0).getUserId())
            .isEqualTo(DUMMY_USER_ID);
        assertThat(actualCaseAssignmentUserRolesRequest.getCaseAssignmentUserRoles().get(1).getUserId())
            .isEqualTo(userInfo.getUid());
    }

    @Test
    @SneakyThrows
    void theGetCaseUserRolesByCaseAndUserIds() {
        CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole.builder()
            .userId(DUMMY_USER_ID)
            .caseRole(USER_CASE_ROLE_DEFENDANT).caseDataId(DUMMY_CASE_SUBMISSION_REFERENCE)
            .build();
        CaseAssignedUserRolesResponse expectedCaseAssignedUserRolesResponse = CaseAssignedUserRolesResponse.builder()
            .caseAssignedUserRoles(List.of(caseAssignmentUserRole))
            .build();
        when(idamClient.getUserInfo(DUMMY_AUTHORISATION_TOKEN)).thenReturn(userInfo);
        when(restTemplate.exchange(
            anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(CaseAssignedUserRolesResponse.class)))
            .thenReturn(new ResponseEntity<>(expectedCaseAssignedUserRolesResponse, HttpStatus.OK));
        CaseAssignedUserRolesResponse actualCaseAssignedUserRolesResponse =
            caseRoleManagementService.getCaseUserRolesByCaseAndUserIds(DUMMY_AUTHORISATION_TOKEN,
                List.of(new CaseTestData().getCaseDetails()));
        assertThat(actualCaseAssignedUserRolesResponse.getCaseAssignedUserRoles()).isNotNull();
        assertThat(actualCaseAssignedUserRolesResponse.getCaseAssignedUserRoles()).hasSize(1);
        assertThat(actualCaseAssignedUserRolesResponse).isEqualTo(expectedCaseAssignedUserRolesResponse);
    }

    @ParameterizedTest
    @MethodSource("provideGetCaseDetailsByCaseUserRoleTestData")
    void theGetCaseDetailsByCaseUserRole(List<CaseDetails> caseDetailsList,
                                         List<CaseAssignmentUserRole> caseAssignmentUserRoles,
                                         String caseUserRole) {
        List<CaseDetails> caseDetailsListByCaseUserRole =
            CaseRoleManagementService.getCaseDetailsByCaseUserRole(caseDetailsList,
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

}
