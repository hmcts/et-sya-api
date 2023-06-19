package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
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
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantTseApplicationStateUpdateRequest;
import uk.gov.hmcts.reform.et.syaapi.service.ApplicationService;
import uk.gov.hmcts.reform.et.syaapi.service.ClaimantTseApplicationStateService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@WebMvcTest(
    controllers = {ClaimantTseApplicationStateController.class}
)
@Import(ClaimantTseApplicationStateController.class)
class ClaimantTseApplicationStateControllerTest {

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private ClaimantTseApplicationStateService claimantTseApplicationStateService;

    private static final String CASE_ID = "1646225213651590";
    private static final String CASE_TYPE = "ET_EnglandWales";

    private final CaseDetails expectedDetails;

    @Autowired
    private MockMvc mockMvc;


    ClaimantTseApplicationStateControllerTest() {
        // Default constructor
        expectedDetails = ResourceLoader.fromString(
            "responses/caseDetails.json",
            CaseDetails.class
        );
    }

    @SneakyThrows
    @Test
    void shouldUpdateSendNotificationState() {
        ClaimantTseApplicationStateUpdateRequest request = ClaimantTseApplicationStateUpdateRequest.builder()
            .caseTypeId(CASE_TYPE)
            .caseId(CASE_ID)
            .applicationId("1")
            .applicationState("viewed")
            .build();

        // when
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);

        when(claimantTseApplicationStateService.updateClaimantTseApplicationState(any(), any())).thenReturn(
            expectedDetails);
        mockMvc.perform(
            put("/applicationState/update-claimant-tse-application-state", CASE_ID)
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ResourceLoader.toJson(request))
        ).andExpect(status().isOk());

        verify(claimantTseApplicationStateService, times(1)).updateClaimantTseApplicationState(
            TEST_SERVICE_AUTH_TOKEN,
            request
        );
    }
}
