package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.TribunalResponseViewedRequest;
import uk.gov.hmcts.reform.et.syaapi.service.ApplicationService;
import uk.gov.hmcts.reform.et.syaapi.service.BundlesService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;

@SuppressWarnings({"PMD.TooManyMethods"})
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class ManageCaseControllerIntegrationTest {


    private static final String CASE_DETAILS_JSON = "responses/caseDetails.json";

    private CaseDetails caseDetailsResponse;
    private StartEventResponse startEventResponse;

    private static final String AUTH_TOKEN = "testToken";

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private IdamClient idamClient;
    @MockBean
    private CoreCaseDataApi ccdApiClient;
    @MockBean
    private ApplicationService applicationService;
    @MockBean
    private BundlesService bundlesService;
    @Autowired
    private ResourceLoader resourceLoader;

    @BeforeAll
    void setUp() throws IOException {
        caseDetailsResponse = resourceLoader.fromString(
            CASE_DETAILS_JSON,
            CaseDetails.class
        );
        startEventResponse = resourceLoader.fromString(
            "responses/caseStartEvent.json",
            StartEventResponse.class
        );
    }

    @BeforeEach
    void setUpBeforeEach() throws NotificationClientException {
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn("token");
        when(idamClient.getUserInfo(any())).thenReturn(
            UserInfo.builder()
                .uid("1234")
                .givenName("TestGivenName")
                .familyName("TestFamilyName")
                .build()
        );
        when(applicationService.submitApplication(any(), any())).thenReturn(caseDetailsResponse);
        when(applicationService.respondToApplication(any(), any())).thenReturn(caseDetailsResponse);
        when(applicationService.updateTribunalResponseAsViewed(any(), any())).thenReturn(caseDetailsResponse);
        when(bundlesService.submitBundles(any(), any())).thenReturn(caseDetailsResponse);

        uk.gov.hmcts.reform.ccd.client.model.SearchResult mockSearchResult =
            uk.gov.hmcts.reform.ccd.client.model.SearchResult.builder()
                .cases(Collections.singletonList(caseDetailsResponse))
                .build();
        when(ccdApiClient.searchCases(any(), any(), any(), any())).thenReturn(mockSearchResult);
    }

    @DisplayName("Should get single case details")
    @Test
    void caseDetailsEndpoint() throws Exception {
        when(ccdApiClient.getCase(any(), any(), any())).thenReturn(caseDetailsResponse);
        CaseRequest caseRequest = CaseRequest.builder()
            .caseId("1646").build();

        mockMvc.perform(post("/cases/user-case")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .content(resourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }


    @DisplayName("Should create case and return case details")
    @Test
    void createCaseEndpoint() throws Exception {
        CaseRequest caseRequest = CaseRequest.builder()
            .build();

        when(ccdApiClient.startForCitizen(any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventResponse);

        when(ccdApiClient.submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any()))
            .thenReturn(caseDetailsResponse);

        mockMvc.perform(
                post("/cases/initiate-case")
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(resourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }

    @DisplayName("Should update draft case and return case data")
    @Test
    void updateDraftCaseEndpoint() throws Exception {
        CaseRequest caseRequest = CaseRequest.builder()
            .caseTypeId(SCOTLAND_CASE_TYPE)
            .caseId("12")
            .build();

        when(ccdApiClient.startEventForCitizen(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventResponse);
        when(ccdApiClient.submitEventForCitizen(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .thenReturn(caseDetailsResponse);

        mockMvc.perform(
                put("/cases/update-case")
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(resourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }

    @DisplayName("Should submit case and return case data")
    @Test
    void submitCaseEndpoint() throws Exception {
        // Use realistic case data as would be sent by a real client, matching the CaseRequest model
        // and what the service expects. Load from the same JSON file used in production/functional tests.
        java.util.Map<String, Object> caseData;
        try (java.io.InputStream inputStream = getClass().getClassLoader()
            .getResourceAsStream("requests/caseData.json")) {
            if (inputStream == null) {
                throw new RuntimeException("caseData.json not found");
            }
            caseData = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                inputStream,
                new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {}
            );
        }

        CaseRequest caseRequest = CaseRequest.builder()
            .caseTypeId(SCOTLAND_CASE_TYPE)
            .caseId("12")
            .caseData(caseData)
            .build();

        when(ccdApiClient.startEventForCitizen(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventResponse);
        when(ccdApiClient.submitEventForCitizen(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .thenReturn(caseDetailsResponse);

        mockMvc.perform(
                put("/cases/submit-case")
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(resourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }

    @DisplayName("Should update submitted case and return case data")
    @Test
    void updateSubmittedCaseEndpoint() throws Exception {
        CaseRequest caseRequest = CaseRequest.builder()
            .caseTypeId(SCOTLAND_CASE_TYPE)
            .caseId("12")
            .build();

        when(ccdApiClient.startEventForCitizen(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(startEventResponse);
        when(ccdApiClient.submitEventForCitizen(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .thenReturn(caseDetailsResponse);

        mockMvc.perform(
                post("/cases/update-case-submitted")
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(resourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }

    @DisplayName("Should create new TSE application and return case details")
    @Test
    void createTseApplicationEndpoint() throws Exception {
        ClaimantApplicationRequest caseRequest = ClaimantApplicationRequest.builder()
            .caseTypeId(SCOTLAND_CASE_TYPE)
            .caseId("12")
            .build();

        mockMvc.perform(
                put("/cases/submit-claimant-application")
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(resourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }

    @DisplayName("Should update TseApplication with response and return case details")
    @Test
    void addResponseEndpoint() throws Exception {
        RespondToApplicationRequest caseRequest = RespondToApplicationRequest.builder()
            .caseTypeId(SCOTLAND_CASE_TYPE)
            .caseId("12")
            .applicationId("1234")
            .response(new TseRespondType())
            .build();

        mockMvc.perform(
                put("/cases/respond-to-application")
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(resourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }

    @DisplayName("Should update tribunal response as viewed")
    @Test
    void updateTribunalResponse() throws Exception {
        TribunalResponseViewedRequest caseRequest = TribunalResponseViewedRequest.builder()
            .caseTypeId(SCOTLAND_CASE_TYPE)
            .caseId("12")
            .appId("1234")
            .responseId("1")
            .build();

        mockMvc.perform(
                put("/cases/tribunal-response-viewed")
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(resourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }

    private String getSerialisedMessage(String fileName) {
        try (java.io.InputStream inputStream = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("Unable to find the file: " + fileName);
            }
            return new String(
                inputStream.readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
