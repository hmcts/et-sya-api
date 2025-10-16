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
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationAddResponseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationStateUpdateRequest;
import uk.gov.hmcts.reform.et.syaapi.service.ApplicationService;
import uk.gov.hmcts.reform.et.syaapi.service.SendNotificationService;
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
    controllers = {SendNotificationController.class}
)
@Import(SendNotificationController.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
class SendNotificationControllerTest {


    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private SendNotificationService sendNotificationService;

    private static final String CASE_ID = "1646225213651590";
    private static final String CASE_TYPE = "ET_Scotland";

    private final CaseDetails expectedDetails;

    @Autowired
    private MockMvc mockMvc;


    SendNotificationControllerTest() {
        // Default constructor
        expectedDetails = ResourceLoader.fromString(
            "responses/caseDetails.json",
            CaseDetails.class
        );
    }

    @SneakyThrows
    @Test
    void shouldUpdateSendNotificationState() {
        SendNotificationStateUpdateRequest request = SendNotificationStateUpdateRequest.builder()
            .caseTypeId(CASE_TYPE)
            .caseId(CASE_ID)
            .sendNotificationId("1")
            .build();

        // when
        when(verifyTokenService.verifyTokenSignature(anyString())).thenReturn(true);

        when(applicationService.submitApplication(anyString(), any(ClaimantApplicationRequest.class)))
            .thenReturn(expectedDetails);
        mockMvc.perform(
            put("/sendNotification/update-notification-state", CASE_ID)
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ResourceLoader.toJson(request))
        ).andExpect(status().isOk());

        verify(sendNotificationService, times(1)).updateSendNotificationState(
            TEST_SERVICE_AUTH_TOKEN,
            request
        );
    }

    @SneakyThrows
    @Test
    void shouldAddClaimantResponseSendNotification() {
        SendNotificationAddResponseRequest request = SendNotificationAddResponseRequest.builder()
            .caseTypeId(CASE_TYPE)
            .caseId(CASE_ID)
            .sendNotificationId("1")
            .pseResponseType(PseResponseType.builder().build())
            .build();

        // when
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);

        mockMvc.perform(
            put("/sendNotification/add-response-send-notification", CASE_ID)
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ResourceLoader.toJson(request))
        ).andExpect(status().isOk());

        verify(sendNotificationService, times(1)).addClaimantResponseSendNotification(
            TEST_SERVICE_AUTH_TOKEN,
            request
        );
    }

}
