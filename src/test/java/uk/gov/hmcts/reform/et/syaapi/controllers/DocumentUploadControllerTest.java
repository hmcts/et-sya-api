package uk.gov.hmcts.reform.et.syaapi.controllers;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentException;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

@WebMvcTest(
    controllers = {DocumentUploadController.class}
)
@Import(DocumentUploadController.class)
public class DocumentUploadControllerTest {
    private static final String DOCUMENT_NAME = "hello.txt";
    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String MOCK_FILE_BODY = "Hello, World!";
    private static final MockMultipartFile MOCK_FILE = new MockMultipartFile(
        "mock_file",
        DOCUMENT_NAME,
        MediaType.TEXT_PLAIN_VALUE,
        MOCK_FILE_BODY.getBytes()
    );

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CaseDocumentService caseDocumentService;

    @MockBean
    private VerifyTokenService verifyTokenService;

    DocumentUploadControllerTest() {

    }

    @SneakyThrows
    @Test
    void givenCallWithCaseNumberAndDocumentProducesUpload() {

        when(caseDocumentService.uploadDocument(TEST_SERVICE_AUTH_TOKEN, CASE_TYPE,
            MOCK_FILE)).thenReturn(URI.create("Success"));
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);

        mockMvc.perform(post("/cases/convert-to-pdf")
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void givenPdfServiceExpectionProducesServerError() {
        CaseDocumentRequest caseRequest = CaseDocumentRequest.builder()
            .caseTypeId(CASE_TYPE).multipartFile(MOCK_FILE).build();

        when(caseDocumentService.uploadDocument(TEST_SERVICE_AUTH_TOKEN, caseRequest.getCaseTypeId(),
            caseRequest.getMultipartFile())).thenThrow(CaseDocumentException.class);
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);

        mockMvc.perform(post("/generate-pdf")
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ResourceLoader.toJson(caseRequest)))
            .andExpect(status().isInternalServerError());
    }

//
//    @SneakyThrows
//    @Test
//    void givenEmptyCaseProduces() {
//
//    }
}
