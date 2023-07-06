package uk.gov.hmcts.reform.et.syaapi.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentException;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@WebMvcTest(
    controllers = {DocumentUploadController.class}
)
@Import(DocumentUploadController.class)
class DocumentUploadControllerTest {
    private static final String DOCUMENT_NAME = "hello.txt";
    private static final String MOCK_FILE_BODY = "Hello, World!";
    private static final MockMultipartFile MOCK_FILE = new MockMultipartFile(
        "document_upload",
        DOCUMENT_NAME,
        MediaType.TEXT_PLAIN_VALUE,
        MOCK_FILE_BODY.getBytes()
    );
    private static final MockMultipartFile MOCK_FILE_EMPTY = new MockMultipartFile(
        "document_upload",
        DOCUMENT_NAME,
        MediaType.TEXT_PLAIN_VALUE,
        (byte[]) null
    );
    private static final CaseDocument MOCK_RESPONSE = CaseDocument.builder()
        .originalDocumentName(DOCUMENT_NAME).classification("PUBLIC").links(Map.of("self", Map.of("href",
            "TestURL.com"))).build();

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CaseDocumentService caseDocumentService;
    @MockBean
    private VerifyTokenService verifyTokenService;

    @BeforeEach
    public void setUp() {
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
    }

    @SneakyThrows
    @Test
    void givenCallWithCaseNumberAndDocumentProducesUpload() {
        when(caseDocumentService.uploadDocument(TEST_SERVICE_AUTH_TOKEN, ENGLAND_CASE_TYPE,
            MOCK_FILE)).thenReturn(MOCK_RESPONSE);

        mockMvc.perform(multipart("/documents/upload/" + ENGLAND_CASE_TYPE)
                .file(MOCK_FILE)
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void givenPdfServiceExpectionProducesServerError() {
        when(caseDocumentService.uploadDocument(TEST_SERVICE_AUTH_TOKEN, ENGLAND_CASE_TYPE,
            MOCK_FILE)).thenThrow(CaseDocumentException.class);
        mockMvc.perform(multipart("/documents/upload/" + ENGLAND_CASE_TYPE)
                .file(MOCK_FILE)
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isInternalServerError());
    }

    @SneakyThrows
    @Test
    void givenEmptyDocumentProducesBadRequestResponse() {
        mockMvc.perform(multipart("/documents/upload/" + ENGLAND_CASE_TYPE)
                .file(MOCK_FILE_EMPTY)
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void givenNoCaseTypeProducesBadRequestResponse() {
        mockMvc.perform(multipart("/documents/upload/none")
                .file(MOCK_FILE)
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest());
    }
}
