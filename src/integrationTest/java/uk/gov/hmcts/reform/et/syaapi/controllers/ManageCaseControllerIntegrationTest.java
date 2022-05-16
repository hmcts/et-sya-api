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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.client.CcdApiClient;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@AutoConfigureMockMvc
public class ManageCaseControllerIntegrationTest {

    private final CaseDetails expectedDetails = ResourceLoader.fromString(
        "responses/caseDetails.json",
        CaseDetails.class
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
    private CcdApiClient ccdApiClient;

    @BeforeEach
    void setUp() {
        final WebRequestTrackingFilter filter = new WebRequestTrackingFilter();
        filter.init(new MockFilterConfig());
        mockMvc = webAppContextSetup(wac).addFilters(filter).build();
    }

    @DisplayName("Should create case")
    @Test
    void caseDetailsEndpoint() throws Exception {

        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn("token");
        when(ccdApiClient.getCase(any(),any(),any())).thenReturn(expectedDetails);

        MvcResult response = mockMvc.perform(get("/caseDetails/1234").header(HttpHeaders.AUTHORIZATION, "abc")).andExpect(status().isOk()).andReturn();

//        assertThat(response.getResponse().getContentAsString()).startsWith("Welcome");
    }
}
