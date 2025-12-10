package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRolesRequest;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.models.CaseAssignmentResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageCaseRoleServiceProfessionalUserTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private CoreCaseDataApi ccdApi;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private IdamClient idamClient;
    @Mock
    private ET3Service et3Service;
    @Mock
    private CaseService caseService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private ManageCaseRoleService manageCaseRoleService;

    private static final String MODIFICATION_TYPE_ASSIGNMENT = "Assignment";
    private static final String CASE_ID = "1646225213651590";
    private static final String USER_ID = "1234564789";
    private static final String CASE_ROLE_DEFENDANT = "[DEFENDANT]";
    private static final String AUTHORISATION = "Bearer token";
    private static final String SERVICE_TOKEN = "ServiceToken";
    private static final String ADMIN_TOKEN = "AdminToken";
    private static final String CCD_API_URL = "http://localhost:4452";
    private static final String RESPONDENT_NAME = "Test Respondent";
    private static final String CASE_TYPE_ID = "ET_EnglandWales";

    private static final String PROFESSIONAL_USER_ERROR_MESSAGE =
        "{\"exception\":\"uk.gov.hmcts.ccd.endpoint.exceptions.BadRequestException\",\"status\":400,"
        + "\"error\":\"Bad Request\",\"message\":\"Client error when creating Role Assignments "
        + "from Role Assignment Service because of 422 : \"{\\\"roleAssignmentResponse\\\":{"
        + "\\\"requestedRoles\\\":[{\\\"roleCategory\\\":\\\"PROFESSIONAL\\\"}]}}\"\"}";

    @BeforeEach
    void setup() {
        manageCaseRoleService = new ManageCaseRoleService(
            adminUserService,
            authTokenGenerator,
            idamClient,
            restTemplate,
            ccdApi,
            et3Service,
            caseService,
            caseDetailsConverter
        );
        ReflectionTestUtils.setField(manageCaseRoleService, "ccdApiUrl", CCD_API_URL);

        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Test
    void shouldReturnProfessionalUserStatusWhenRoleCategoryIsProfessional() throws IOException {
        ModifyCaseUserRole modifyCaseUserRole = ModifyCaseUserRole.builder()
            .caseDataId(CASE_ID)
            .userId(USER_ID)
            .caseRole(CASE_ROLE_DEFENDANT)
            .caseTypeId(CASE_TYPE_ID)
            .respondentName(RESPONDENT_NAME)
            .build();

        ModifyCaseUserRolesRequest request = ModifyCaseUserRolesRequest.builder()
            .modifyCaseUserRoles(List.of(modifyCaseUserRole))
            .build();

        HttpClientErrorException exception = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            null,
            PROFESSIONAL_USER_ERROR_MESSAGE.getBytes(),
            null
        );

        when(restTemplate.exchange(
            eq(CCD_API_URL + ManageCaseRoleConstants.CASE_USERS_API_URL),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(CaseAssignmentUserRolesResponse.class)
        )).thenThrow(exception);

        CaseAssignmentResponse response = manageCaseRoleService.modifyUserCaseRoles(
            AUTHORISATION,
            request,
            MODIFICATION_TYPE_ASSIGNMENT
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(CaseAssignmentResponse.AssignmentStatus.PROFESSIONAL_USER);
        assertThat(response.getMessage()).contains("professional user");
    }

    @Test
    void shouldHandleProfessionalUserExceptionInternally() throws IOException {
        HttpClientErrorException exception = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            null,
            PROFESSIONAL_USER_ERROR_MESSAGE.getBytes(),
            null
        );

        when(restTemplate.exchange(
            eq(CCD_API_URL + ManageCaseRoleConstants.CASE_USERS_API_URL),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(CaseAssignmentUserRolesResponse.class)
        )).thenThrow(exception);

        ModifyCaseUserRole modifyCaseUserRole = ModifyCaseUserRole.builder()
            .caseDataId(CASE_ID)
            .userId(USER_ID)
            .caseRole(CASE_ROLE_DEFENDANT)
            .caseTypeId(CASE_TYPE_ID)
            .respondentName(RESPONDENT_NAME)
            .build();

        ModifyCaseUserRolesRequest request = ModifyCaseUserRolesRequest.builder()
            .modifyCaseUserRoles(List.of(modifyCaseUserRole))
            .build();

        CaseAssignmentResponse response = manageCaseRoleService.modifyUserCaseRoles(
            AUTHORISATION,
            request,
            MODIFICATION_TYPE_ASSIGNMENT
        );

        assertEquals(CaseAssignmentResponse.AssignmentStatus.PROFESSIONAL_USER, response.getStatus());
        assertThat(response.getMessage()).contains("professional user");
    }

    @Test
    void shouldNotThrowProfessionalUserExceptionForOtherErrors() {
        ModifyCaseUserRole modifyCaseUserRole = ModifyCaseUserRole.builder()
            .caseDataId(CASE_ID)
            .userId(USER_ID)
            .caseRole(CASE_ROLE_DEFENDANT)
            .caseTypeId(CASE_TYPE_ID)
            .respondentName(RESPONDENT_NAME)
            .build();

        ModifyCaseUserRolesRequest request = ModifyCaseUserRolesRequest.builder()
            .modifyCaseUserRoles(List.of(modifyCaseUserRole))
            .build();

        String regularErrorMessage = "400 : {\"error\":\"Some other bad request error\"}";
        HttpClientErrorException exception = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            null,
            regularErrorMessage.getBytes(),
            null
        );

        when(restTemplate.exchange(
            eq(CCD_API_URL + ManageCaseRoleConstants.CASE_USERS_API_URL),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(CaseAssignmentUserRolesResponse.class)
        )).thenThrow(exception);

        assertThrows(HttpClientErrorException.class, () -> 
            manageCaseRoleService.modifyUserCaseRoles(
                AUTHORISATION,
                request,
                MODIFICATION_TYPE_ASSIGNMENT
            )
        );
    }
}
