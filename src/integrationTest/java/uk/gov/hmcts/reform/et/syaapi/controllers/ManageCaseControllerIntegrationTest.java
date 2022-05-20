package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@AutoConfigureMockMvc
class ManageCaseControllerIntegrationTest {

    private static final String CASE_DETAILS_JSON = "responses/caseDetails.json";
    private static final String CASE_LIST_DETAILS_JSON = "responses/caseListDetails.json";

    private final CaseDetails caseDetailsResponse = ResourceLoader.fromString(
        CASE_DETAILS_JSON,
        CaseDetails.class
    );

    private final StartEventResponse startEventResponse = ResourceLoader.fromString(
        "responses/caseStartEvent.json",
        StartEventResponse.class
    );

    private static final String AUTH_TOKEN = "testToken";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext wac;
    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private IdamClient idamClient;
    @MockBean
    private CoreCaseDataApi ccdApiClient;

    public ManageCaseControllerIntegrationTest() throws IOException {
        // Just to avoid multiple try-catch
    }

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(wac).build();
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn("token");
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().id("1234").build());
    }

    @DisplayName("Should get single case details")
    @Test
    void caseDetailsEndpoint() throws Exception {
        when(ccdApiClient.getCase(any(),any(),any())).thenReturn(caseDetailsResponse);
        CaseRequest caseRequest = CaseRequest.builder()
            .caseId("1646").build();

        mockMvc.perform(post("/cases/user-case")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .content(ResourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }

    @DisplayName("Should get all case details list by user")
    @Test
    void returnCasesByUserEndpoint() throws Exception {
        when(ccdApiClient.searchForCitizen(any(),any(),any(),any(),any(),any()))
            .thenReturn(Collections.singletonList(caseDetailsResponse));

        mockMvc.perform(get("/cases/user-cases").header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_LIST_DETAILS_JSON)));
    }

    @DisplayName("Should create case and return case details")
    @Test
    void createCaseEndpoint() throws Exception {
        CaseRequest caseRequest = CaseRequest.builder()
            .build();

        when(ccdApiClient.startForCitizen(any(),any(),any(),any(),any(),any()))
            .thenReturn(startEventResponse);

        when(ccdApiClient.submitForCitizen(any(),any(),any(),any(),any(),anyBoolean(),any()))
            .thenReturn(caseDetailsResponse);

        mockMvc.perform(
            post("/cases/initiate-case")
                .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ResourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }

    @DisplayName("Should update case and return case data")
    @Test
    void updateCaseEndpoint() throws Exception {
        CaseRequest caseRequest = CaseRequest.builder()
            .caseTypeId("ET_Scotland")
            .caseId("12")
            .build();

        when(ccdApiClient.startEventForCitizen(any(),any(),any(),any(),any(),any(),any()))
            .thenReturn(startEventResponse);
        when(ccdApiClient.submitEventForCitizen(any(),any(),any(),any(),any(),any(),anyBoolean(),any()))
            .thenReturn(caseDetailsResponse);

        mockMvc.perform(
                put("/cases/update-case")
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ResourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }

    @DisplayName("Should submit case and return case data")
    @Test
    void submitCaseEndpoint() throws Exception {
        CaseRequest caseRequest = CaseRequest.builder()
            .caseTypeId("ET_Scotland")
            .caseId("12")
            .build();

        when(ccdApiClient.startEventForCitizen(any(),any(),any(),any(),any(),any(),any()))
            .thenReturn(startEventResponse);
        when(ccdApiClient.submitEventForCitizen(any(),any(),any(),any(),any(),any(),anyBoolean(),any()))
            .thenReturn(caseDetailsResponse);

        mockMvc.perform(
                put("/cases/submit-case")
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ResourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }

    private String getSerialisedMessage(String fileName) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            return new String(Files.readAllBytes(file.toPath()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
