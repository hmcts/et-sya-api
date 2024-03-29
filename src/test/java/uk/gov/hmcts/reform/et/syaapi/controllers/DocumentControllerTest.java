package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.ResourceNotFoundException;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;


@WebMvcTest(
    controllers = {DocumentController.class}
)
class DocumentControllerTest {

    private static final UUID DOCUMENT_ID = UUID.fromString("0d94b4e4-4659-47ad-a640-c63517c76706");
    private static final String NOT_FOUND_MESSAGE = "Document not found";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CaseDocumentService caseDocumentService;

    @MockBean
    private VerifyTokenService verifyTokenService;

    @Test
    void documentBinaryContentSuccess() throws Exception {
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(caseDocumentService.downloadDocument(TEST_SERVICE_AUTH_TOKEN, DOCUMENT_ID))
            .thenReturn(getDocumentBinaryContent());

        mockMvc.perform(get("/document/download/" + DOCUMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().bytes(getDocumentBinaryContent().getBody().getByteArray()));
    }

    @Test
    void documentBinaryContentResourceNotFound() throws Exception {
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(caseDocumentService.downloadDocument(TEST_SERVICE_AUTH_TOKEN, DOCUMENT_ID))
            .thenThrow(new ResourceNotFoundException(NOT_FOUND_MESSAGE, null));

        mockMvc.perform(get("/document/download/" + DOCUMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("Document not found"));
    }

    @Test
    void documentDetailsSuccess() throws Exception {
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(caseDocumentService.getDocumentDetails(TEST_SERVICE_AUTH_TOKEN, DOCUMENT_ID))
            .thenReturn(getDocumentDetails());

        mockMvc.perform(get("/document/details/" + DOCUMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isOk())
            .andExpect(content().string(ResourceLoader.toJson(getDocumentDetails().getBody())));
    }

    @Test
    void documentDetailsResourceNotFound() throws Exception {
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);
        when(caseDocumentService.getDocumentDetails(TEST_SERVICE_AUTH_TOKEN, DOCUMENT_ID))
            .thenThrow(new ResourceNotFoundException(NOT_FOUND_MESSAGE, null));

        mockMvc.perform(get("/document/details/" + DOCUMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("Document not found"));
    }

    private ResponseEntity<ByteArrayResource> getDocumentBinaryContent() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<>(
            new ByteArrayResource("test document content".getBytes()),
            headers,
            HttpStatus.OK
        );
    }

    private ResponseEntity<CaseDocument> getDocumentDetails() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<>(
            CaseDocument.builder()
                .size("size").mimeType("mimeType").hashToken("token").createdOn("createdOn").createdBy("createdBy")
                .lastModifiedBy("lastModifiedBy").modifiedOn("modifiedOn").ttl("ttl")
                .metadata(Map.of("test", "test"))
                .originalDocumentName("docName.txt").classification("PUBLIC")
                .links(Map.of("self", Map.of("href", "TestURL.com"))).build(),
            headers,
            HttpStatus.OK
        );
    }

}
