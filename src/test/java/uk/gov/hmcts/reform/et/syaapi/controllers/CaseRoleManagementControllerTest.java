package uk.gov.hmcts.reform.et.syaapi.controllers;

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
import uk.gov.hmcts.reform.et.syaapi.SyaApiApplication;
import uk.gov.hmcts.reform.et.syaapi.service.CaseRoleManagementService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({CaseRoleManagementController.class})
@ContextConfiguration(classes = SyaApiApplication.class)
class CaseRoleManagementControllerTest {
    private static final String CASE_ID = "1646225213651590";
    private static final String USER_ID = "1234564789";
    private static final String CASE_ROLE = "[DEFENDANT]";
    private static final String AUTH_TOKEN = "some-token";
    private static final String POST_MODIFY_CASE_USER_ROLE_URL = "/caseRoleManagement/modify-case-user-roles";
    private static final String MODIFICATION_TYPE_PARAMETER_NAME = "modificationType";
    private static final String MODIFICATION_TYPE_PARAMETER_VALUE_REVOKE = "Revoke";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private CaseRoleManagementService caseRoleManagementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testModifyUserRoles() throws Exception {
        CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole
            .builder().caseRole(CASE_ROLE).caseDataId(CASE_ID).userId(USER_ID).build();
        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRoles(List.of(caseAssignmentUserRole))
            .build();
        doNothing().when(caseRoleManagementService).modifyUserCaseRoles(any(), any());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(post(POST_MODIFY_CASE_USER_ROLE_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param(MODIFICATION_TYPE_PARAMETER_NAME, MODIFICATION_TYPE_PARAMETER_VALUE_REVOKE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ResourceLoader.toJson(caseAssignmentUserRolesRequest)))
            .andExpect(status().isOk());
    }
}
