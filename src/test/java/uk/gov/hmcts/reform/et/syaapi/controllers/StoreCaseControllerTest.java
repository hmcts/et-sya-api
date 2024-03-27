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
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredRespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.UpdateStoredRespondToTribunalRequest;
import uk.gov.hmcts.reform.et.syaapi.service.StoredApplicationService;
import uk.gov.hmcts.reform.et.syaapi.service.StoredRespondToApplicationSubmitService;
import uk.gov.hmcts.reform.et.syaapi.service.StoredRespondToTribunalSubmitService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@WebMvcTest(
    controllers = {StoreCaseController.class}
)
@Import(StoreCaseController.class)
class StoreCaseControllerTest {

    private static final String CASE_ID = "1646225213651590";
    private static final String CASE_TYPE = "ET_Scotland";
    private final CaseDetails expectedDetails;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private StoredApplicationService storedApplicationService;
    @MockBean
    private StoredRespondToApplicationSubmitService storedRespondToApplicationSubmitService;
    @MockBean
    private StoredRespondToTribunalSubmitService storedRespondToTribunalSubmitService;

    StoreCaseControllerTest() {
        // Default constructor
        expectedDetails = ResourceLoader.fromString(
            "responses/caseDetails.json",
            CaseDetails.class
        );
    }

    @SneakyThrows
    @Test
    void submitStoredRespondToApplication() {
        SubmitStoredRespondToApplicationRequest caseRequest = SubmitStoredRespondToApplicationRequest.builder()
            .caseTypeId(CASE_TYPE)
            .caseId(CASE_ID)
            .applicationId("123")
            .storedRespondId("456")
            .isRespondingToRequestOrOrder(true)
            .build();

        // when
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(storedRespondToApplicationSubmitService.submitRespondToApplication(any(), any()))
            .thenReturn(expectedDetails);

        mockMvc.perform(
            put("/store/submit-stored-respond-to-application", CASE_ID)
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ResourceLoader.toJson(caseRequest))
        ).andExpect(status().isOk());

        verify(storedRespondToApplicationSubmitService, times(1)).submitRespondToApplication(
            TEST_SERVICE_AUTH_TOKEN,
            caseRequest
        );
    }

    @SneakyThrows
    @Test
    void submitStoredRespondToTribunal() {
        UpdateStoredRespondToTribunalRequest caseRequest = UpdateStoredRespondToTribunalRequest.builder()
            .caseTypeId(CASE_TYPE)
            .caseId(CASE_ID)
            .orderId("123")
            .respondId("456")
            .isRespondingToRequestOrOrder(true)
            .build();

        // when
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(storedRespondToTribunalSubmitService.submitRespondToTribunal(any(), any())).thenReturn(expectedDetails);

        mockMvc.perform(
            put("/store/submit-stored-respond-to-tribunal", CASE_ID)
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ResourceLoader.toJson(caseRequest))
        ).andExpect(status().isOk());

        verify(storedRespondToTribunalSubmitService, times(1)).submitRespondToTribunal(
            TEST_SERVICE_AUTH_TOKEN,
            caseRequest
        );
    }
}
