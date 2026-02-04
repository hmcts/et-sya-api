package uk.gov.hmcts.reform.et.syaapi.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.SyaApiApplication;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseAssignmentResponse;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.ManageCaseRoleService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;

@RunWith(SpringRunner.class)
@WebMvcTest({ManageCaseRoleController.class})
@ContextConfiguration(classes = SyaApiApplication.class)
class ManageCaseRoleControllerTest {
    private static final String CASE_ID = "1646225213651590";
    private static final String USER_ID = "1234564789";
    private static final String CASE_ROLE = "[DEFENDANT]";
    private static final String AUTH_TOKEN = "some-token";
    private static final String POST_MODIFY_CASE_USER_ROLE_URL = "/manageCaseRole/modifyCaseUserRoles";
    private static final String REVOKE_CLAIMANT_SOLICITOR_ROLE_URL = "/manageCaseRole/revokeClaimantSolicitorRole";
    private static final String REVOKE_RESPONDENT_SOLICITOR_ROLE_URL = "/manageCaseRole/revokeRespondentSolicitorRole";
    private static final String POST_FIND_CASE_FOR_ROLE_MODIFICATION
        = "/manageCaseRole/findCaseForRoleModification";
    private static final String MODIFICATION_TYPE_PARAMETER_NAME = "modificationType";
    private static final String MODIFICATION_TYPE_PARAMETER_VALUE_REVOKE = "Revoke";
    private static final String CASE_SUBMISSION_REFERENCE = "1234567890123456";
    private static final String RESPONDENT_NAME = "Respondent Name";
    private static final String CLAIMANT_FIRST_NAMES = "Claimant First Names";
    private static final String CLAIMANT_LAST_NAME = "Claimant Last Name";
    private static final String STRING_ZERO = "0";
    private static final String APPLICATION_NAME_VALUE = "et-sya-frontend";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private ManageCaseRoleService manageCaseRoleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @SneakyThrows
    void modifyUserRoles() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        ModifyCaseUserRole modifyCaseUserRole = ModifyCaseUserRole
            .builder()
            .caseRole(CASE_ROLE)
            .caseDataId(CASE_ID)
            .userId(USER_ID)
            .caseTypeId(ENGLAND_CASE_TYPE)
            .respondentName(RESPONDENT_NAME).build();
        ModifyCaseUserRolesRequest modifyCaseUserRolesRequest = ModifyCaseUserRolesRequest
                .builder()
                .modifyCaseUserRoles(List.of(modifyCaseUserRole))
                .build();
        when(manageCaseRoleService.modifyUserCaseRoles(any(), any(), any())).thenReturn(
            CaseAssignmentResponse.builder()
                .caseDetails(List.of(new CaseTestData().getCaseDetails()))
                .status(CaseAssignmentResponse.AssignmentStatus.ASSIGNED)
                .message("User successfully assigned to case")
                .build());
        mockMvc.perform(post(POST_MODIFY_CASE_USER_ROLE_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param(MODIFICATION_TYPE_PARAMETER_NAME, MODIFICATION_TYPE_PARAMETER_VALUE_REVOKE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ResourceLoader.toJson(modifyCaseUserRolesRequest)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void modifyUserRolesWithoutUserId() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole
            .builder().caseRole(CASE_ROLE).caseDataId(CASE_ID).userId(null).build();
        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRoles(List.of(caseAssignmentUserRole))
            .build();

        when(manageCaseRoleService.modifyUserCaseRoles(any(), any(), any())).thenReturn(
            CaseAssignmentResponse.builder()
                .caseDetails(List.of(new CaseTestData().getCaseDetails()))
                .status(CaseAssignmentResponse.AssignmentStatus.ASSIGNED)
                .message("User successfully assigned to case")
                .build());
        mockMvc.perform(post(POST_MODIFY_CASE_USER_ROLE_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param(MODIFICATION_TYPE_PARAMETER_NAME, MODIFICATION_TYPE_PARAMETER_VALUE_REVOKE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ResourceLoader.toJson(caseAssignmentUserRolesRequest)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void findCaseForRoleModification() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest = FindCaseForRoleModificationRequest
            .builder()
            .caseSubmissionReference(CASE_SUBMISSION_REFERENCE)
            .respondentName(RESPONDENT_NAME)
            .claimantFirstNames(CLAIMANT_FIRST_NAMES)
            .claimantLastName(CLAIMANT_LAST_NAME)
            .applicationName(APPLICATION_NAME_VALUE)
            .build();
        when(manageCaseRoleService.findCaseForRoleModification(any(), any()))
            .thenReturn(CaseDetails.builder()
                            .id(Long.parseLong(CASE_ID))
                            .caseTypeId(ENGLAND_CASE_TYPE)
                            .build());
        mockMvc.perform(post(POST_FIND_CASE_FOR_ROLE_MODIFICATION)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ResourceLoader.toJson(findCaseForRoleModificationRequest)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void revokeClaimantSolicitorRole() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        when(manageCaseRoleService.revokeClaimantSolicitorRole(AUTH_TOKEN, CASE_ID)).thenReturn(
            new CaseTestData().getCaseDetails());
        mockMvc.perform(post(REVOKE_CLAIMANT_SOLICITOR_ROLE_URL
                                 + "?caseSubmissionReference=" + CASE_ID)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void revokeRespondentSolicitorRole() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        when(manageCaseRoleService.revokeRespondentSolicitorRole(AUTH_TOKEN, CASE_ID, STRING_ZERO)).thenReturn(
            new CaseTestData().getCaseDetails());
        mockMvc.perform(post(REVOKE_RESPONDENT_SOLICITOR_ROLE_URL
                                 + "?caseSubmissionReference=" + CASE_ID + "&respondentIndex=" + STRING_ZERO)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk());
    }
}
