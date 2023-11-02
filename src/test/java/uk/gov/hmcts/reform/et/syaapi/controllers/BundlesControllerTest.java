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
import uk.gov.hmcts.et.common.model.ccd.types.HearingBundleType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantBundlesRequest;
import uk.gov.hmcts.reform.et.syaapi.service.BundlesService;
import uk.gov.hmcts.reform.et.syaapi.service.FeatureToggleService;
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
    controllers = {BundlesController.class}
)
@Import(BundlesController.class)
class BundlesControllerTest {
    private static final String CASE_ID = "1646225213651590";
    private static final String CASE_TYPE = "ET_Scotland";

    private final CaseDetails expectedDetails;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private BundlesService bundlesService;

    @MockBean
    private FeatureToggleService featureToggleService;

    BundlesControllerTest() {
        // Default constructor
        expectedDetails = ResourceLoader.fromString(
            "responses/caseDetails.json",
            CaseDetails.class
        );
    }

    @SneakyThrows
    @Test
    void shouldSubmitClaimantBundles() {
        HearingBundleType bundle = new HearingBundleType();
        ClaimantBundlesRequest bundleRequest = ClaimantBundlesRequest.builder()
            .caseId(CASE_ID)
            .caseTypeId(CASE_TYPE)
            .claimantBundles(bundle)
            .build();

        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(featureToggleService.isBundlesEnabled()).thenReturn(true);

        when(bundlesService.submitBundles(any(), any())).thenReturn(expectedDetails);
        mockMvc.perform(
            put("/bundles/submit-bundles", CASE_ID)
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ResourceLoader.toJson(bundleRequest))
        ).andExpect(status().isOk());

        verify(bundlesService, times(1)).submitBundles(
            TEST_SERVICE_AUTH_TOKEN,
            bundleRequest
        );
    }
}
