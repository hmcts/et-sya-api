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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.StoredApplicationService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;

@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class StoreCaseControllerIntegrationTest {

    private static final String CASE_DETAILS_JSON = "responses/caseDetails.json";
    private CaseDetails caseDetailsResponse;

    private static final String CASE_ID = "123456789";
    private static final String AUTH_TOKEN = "testToken";
    private static final String APP_ID = "987654321";

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private IdamClient idamClient;
    @MockBean
    private StoredApplicationService storedApplicationService;

    @Autowired
    private ResourceLoader resourceLoader;

    @BeforeAll
    void setUp() throws IOException {
        caseDetailsResponse = resourceLoader.fromString(
            CASE_DETAILS_JSON,
            CaseDetails.class
        );
    }

    @BeforeEach
    void setUpBeforeEach() {
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn("token");
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid("1234").build());
        when(storedApplicationService.submitStoredApplication(any(),any())).thenReturn(caseDetailsResponse);
    }

    @DisplayName("Should update tribunal response as viewed")
    @Test
    void submitStoredApplicationRequest() throws Exception {
        SubmitStoredApplicationRequest caseRequest = SubmitStoredApplicationRequest.builder()
            .caseId(CASE_ID)
            .caseTypeId(SCOTLAND_CASE_TYPE)
            .applicationId(APP_ID)
            .build();

        mockMvc.perform(
                put("/store/submit-stored-claimant-application")
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(resourceLoader.toJson(caseRequest)))
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
