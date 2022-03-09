package uk.gov.hmcts.reform.et.syaapi.controllers;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;


@WebMvcTest(
    controllers = {ManageCaseController.class}
)
@Import(ManageCaseController.class)
class ManageCaseControllerTest {

    private static final String CASE_ID = "1646225213651590";
    private static final String CASE_TYPE = "ET_Scotland";
    private static final String EVENT_TYPE = "initiateCaseDraft";
    private static final String JURISDICTION_ID = "EMPLOYMENT";
    private static final String USER_ID = "1234";
    private final CaseDetails expectedDetails = ResourceLoader.fromString(
        "responses/caseDetails.json",
        CaseDetails.class
    );
    private final List<CaseDetails> expectedDetailsList = ResourceLoader.fromStringToList(
        "responses/caseDetailsList.json",
        CaseDetails.class
    );

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CaseService caseService;

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private IdamClient idamClient;

    @Test
    void shouldGetCaseDetailsByUser() throws Exception {
        // given
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(UserDetails.builder().id(USER_ID).build());
        when(caseService.getCaseDataByUser(
            TEST_SERVICE_AUTH_TOKEN,
            JURISDICTION_ID,
            CASE_TYPE, emptyMap()
        ))
            .thenReturn(expectedDetailsList);

        // when
        mockMvc.perform(get("/jurisdictions/{jurisdictionId}/caseTypes/{caseType}/cases",
                            JURISDICTION_ID, CASE_TYPE
            )
                            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            // then
            .andExpect(status().isOk())
            .andExpect(jsonPath("[0].case_type_id").value(expectedDetailsList.get(0).getCaseTypeId()))
            .andExpect(jsonPath("[0].jurisdiction").value(expectedDetailsList.get(0).getJurisdiction()))
            .andExpect(jsonPath("[0].state").value(expectedDetailsList.get(0).getState()))
            .andExpect(jsonPath("[0].created_date").exists())
            .andExpect(jsonPath("[0].last_modified").exists())
            .andExpect(jsonPath("[1].case_type_id").value(expectedDetailsList.get(1).getCaseTypeId()));
    }

    @Test
    void shouldGetCaseDetails() throws Exception {
        // given
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(caseService.getCaseData(TEST_SERVICE_AUTH_TOKEN, CASE_ID))
            .thenReturn(expectedDetails);

        // when
        mockMvc.perform(get("/caseDetails/{caseId}", CASE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDetails.getId()))
            .andExpect(jsonPath("$.case_type_id").value(expectedDetails.getCaseTypeId()))
            .andExpect(jsonPath("$.jurisdiction").value(expectedDetails.getJurisdiction()))
            .andExpect(jsonPath("$.state").value(expectedDetails.getState()))
            .andExpect(jsonPath("$.created_date").exists())
            .andExpect(jsonPath("$.last_modified").exists());
    }

    @Test
    void shouldReturnBadRequestForNonExistingItem() throws Exception {
        Request request = Request.create(
            Request.HttpMethod.GET, "/test", emptyMap(), null, new RequestTemplate());
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(caseService.getCaseData(any(), any())).thenThrow(new FeignException.BadRequest(
            "Bad request",
            request,
            "incorrect payload".getBytes(StandardCharsets.UTF_8),
            emptyMap()
        ));
        mockMvc.perform(get("/caseDetails/{caseId}", CASE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("Bad request - incorrect payload"));
    }

    @Test
    void shouldCreateDraftCase() throws Exception {
        // given
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(caseService.createCase(TEST_SERVICE_AUTH_TOKEN, CASE_TYPE, EVENT_TYPE))
            .thenReturn(expectedDetails);

        // when
        mockMvc.perform(post("/case-type/{caseType}/event-type/{eventType}/case", CASE_TYPE, EVENT_TYPE)
                            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedDetails.getId()))
            .andExpect(jsonPath("$.case_type_id").value(expectedDetails.getCaseTypeId()))
            .andExpect(jsonPath("$.jurisdiction").value(expectedDetails.getJurisdiction()))
            .andExpect(jsonPath("$.state").value(expectedDetails.getState()))
            .andExpect(jsonPath("$.created_date").exists())
            .andExpect(jsonPath("$.last_modified").exists());
    }
}
