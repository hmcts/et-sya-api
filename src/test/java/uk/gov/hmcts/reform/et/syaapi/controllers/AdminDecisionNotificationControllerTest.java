package uk.gov.hmcts.reform.et.syaapi.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.models.AdminDecisionNotificationStateUpdateRequest;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.AdminDecisionNotificationService;
import uk.gov.hmcts.reform.et.syaapi.service.ApplicationService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@WebMvcTest(
    controllers = {AdminDecisionNotificationController.class}
)
@Import(AdminDecisionNotificationController.class)
class AdminDecisionNotificationControllerTest {

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private AdminDecisionNotificationService adminDecisionNotificationService;

    private static final String CASE_ID = "1646225213651590";
    private static final String CASE_TYPE = "ET_Scotland";

    private final CaseDetails expectedDetails;

    @Autowired
    private MockMvc mockMvc;


    AdminDecisionNotificationControllerTest() {
        // Default constructor
        expectedDetails = ResourceLoader.fromString(
            "responses/caseDetails.json",
            CaseDetails.class
        );
    }

    @SneakyThrows
    @Test
    void shouldUpdateSendNotificationState() {
        AdminDecisionNotificationStateUpdateRequest request = AdminDecisionNotificationStateUpdateRequest.builder()
            .caseTypeId(CASE_TYPE)
            .caseId(CASE_ID)
            .adminDecisionId("1")
            .build();

        // when
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);

        when(applicationService.submitApplication(anyString(), any(ClaimantApplicationRequest.class), anyString()))
            .thenReturn(expectedDetails);
        mockMvc.perform(
            put("/tseAdmin/update-admin-decision-state", CASE_ID)
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ResourceLoader.toJson(request))
        ).andExpect(status().isOk());

        verify(adminDecisionNotificationService, times(1)).updateAdminDecisionNotificationState(
            TEST_SERVICE_AUTH_TOKEN,
            request
        );
    }
}
