package uk.gov.hmcts.reform.et.syaapi.controllers;

import com.microsoft.applicationinsights.web.internal.WebRequestTrackingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.client.CcdApiClient;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@AutoConfigureMockMvc
public class ManageCaseControllerIntegrationTest {

    private static final String USER_ID = "1234";

    private final CaseDetails expectedCaseDetails = ResourceLoader.fromString(
        "responses/caseDetails.json",
        CaseDetails.class
    );

    StartEventResponse startEventResponse = ResourceLoader.fromString(
        "responses/caseStartEvent.json",
        StartEventResponse.class
    );

    private final String requestCaseData = ResourceUtil.resourceAsString(
        "requests/caseData.json"
    );

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
    private CcdApiClient ccdApiClient;

    public ManageCaseControllerIntegrationTest() throws IOException {
    }

    @BeforeEach
    void setUp() {
        final WebRequestTrackingFilter filter = new WebRequestTrackingFilter();
        filter.init(new MockFilterConfig());
        mockMvc = webAppContextSetup(wac).addFilters(filter).build();

        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn("token");
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().id(USER_ID).build());
    }

    @DisplayName("Should get case details")
    @Test
    void caseDetailsEndpoint() throws Exception {
        when(ccdApiClient.getCase(any(),any(),any())).thenReturn(expectedCaseDetails);

        mockMvc.perform(get("/caseDetails/1234").header(HttpHeaders.AUTHORIZATION, "abc"))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage("responses/caseDetails.json")));
    }

    @DisplayName("Should get case details list by user")
    @Test
    void getCaseByUserEndpoint() throws Exception {
        when(ccdApiClient.searchForCitizen(any(),any(),any(),any(),any(),any()))
            .thenReturn(Collections.singletonList(expectedCaseDetails));

        mockMvc.perform(get("/caseTypes/ET_Scotland/cases").header(HttpHeaders.AUTHORIZATION, "abc"))
            .andExpect(status().isOk())
            .andExpect(content().json("[" + getSerialisedMessage("responses/caseDetails.json") + "]"));
    }

    @DisplayName("Should create case and return case details")
    @Test
    void createCaseEndpoint() throws Exception {
        when(ccdApiClient.startForCitizen(any(),any(),any(),any(),any(),any()))
            .thenReturn(startEventResponse);

        when(ccdApiClient.submitForCitizen(any(),any(),any(),any(),any(),anyBoolean(),any()))
            .thenReturn(expectedCaseDetails);

        mockMvc.perform(
            post("/case-type/ET_Scotland/event-type/INITIATE_CASE_DRAFT/case")
                .header(HttpHeaders.AUTHORIZATION, "abc")
                .content(requestCaseData))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage("responses/caseDetails.json")));
    }

    @DisplayName("Should update case and return case data")
    @Test
    void updateCaseEndpoint() throws Exception {
        when(ccdApiClient.startEventForCitizen(any(),any(),any(),any(),any(),any(),any()))
            .thenReturn(startEventResponse);

        when(ccdApiClient.submitEventForCitizen(any(),any(),any(),any(),any(),any(),anyBoolean(),any()))
            .thenReturn(expectedCaseDetails);

        mockMvc.perform(
                put("/case-type/ET_Scotland/event-type/INITIATE_CASE_DRAFT/1646225213651590")
                    .header(HttpHeaders.AUTHORIZATION, "abc")
                    .content(requestCaseData))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage("requests/caseData.json")));
    }

    private String getSerialisedMessage(String fileName) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            return new String(Files.readAllBytes(file.toPath()));

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
