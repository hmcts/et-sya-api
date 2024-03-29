package uk.gov.hmcts.reform.et.syaapi.controllers;

import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@WebMvcTest(
    controllers = {DocumentGenerationController.class}
)
@Import(DocumentGenerationController.class)

class DocumentGenerationControllerTest {

    private String requestJson;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VerifyTokenService verifyTokenService;

    @BeforeEach
    public void setUp() throws IOException {
        requestJson = ResourceUtil.resourceAsString(
            "requests/caseId.json"
        );
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
    }

    @SneakyThrows
    @Test
    void happyRequestReturnsExpectedByteArray() {

        byte[] expectedResult = Files.readAllBytes(Paths.get("src/main/resources/HelloWorld.pdf"));

        MvcResult result = mockMvc.perform(post("/generate-pdf")
                            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_PDF_VALUE)
            .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/pdf"))
            .andReturn();

        byte[] receivedContent = result.getResponse().getContentAsByteArray();
        Assert.assertArrayEquals(receivedContent, expectedResult);

    }

    @SneakyThrows
    @Test
    void noRequestBodyProvidedReturnsBadRequest() {

        mockMvc.perform(post("/generate-pdf")
                            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isBadRequest());

    }
}
