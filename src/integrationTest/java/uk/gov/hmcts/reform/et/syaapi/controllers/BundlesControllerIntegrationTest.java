package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.junit.jupiter.api.BeforeAll;
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
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantBundlesRequest;
import uk.gov.hmcts.reform.et.syaapi.service.BundlesService;
import uk.gov.hmcts.reform.et.syaapi.service.FeatureToggleService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;

@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class BundlesControllerIntegrationTest {
    private static final String CASE_DETAILS_JSON = "responses/caseDetails.json";

    private CaseDetails caseDetailsResponse;

    private static final String AUTH_TOKEN = "testToken";

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private BundlesService bundlesService;
    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private ResourceLoader resourceLoader;


    @BeforeAll
    void setUp() throws IOException {
        caseDetailsResponse = resourceLoader.fromString(
            CASE_DETAILS_JSON,
            CaseDetails.class
        );
    }


    @DisplayName("Should create bundle and return case details")
    @Test
    void submitBundles() throws Exception {
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn("token");
        when(bundlesService.submitBundles(any(), any())).thenReturn(caseDetailsResponse);
        when(featureToggleService.isBundlesEnabled()).thenReturn(true);
        ClaimantBundlesRequest caseRequest = ClaimantBundlesRequest.builder()
            .caseTypeId(SCOTLAND_CASE_TYPE)
            .caseId("12")
            .build();

        mockMvc.perform(
                put("/bundles/submit-bundles")
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(resourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk())
            .andExpect(content().json(getSerialisedMessage(CASE_DETAILS_JSON)));
    }

    private String getSerialisedMessage(String fileName) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).getFile());
            return new String(Files.readAllBytes(file.toPath()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
