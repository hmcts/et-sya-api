package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
class CaseDocumentServiceTest {

    private static final String DOCUMENT_UPLOAD_API_URL = "http://someurl.com";

    private static final String DOCUMENT_NAME = "hello.txt";

    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String MOCK_TOKEN = "Bearer Token";

    private static final String MOCK_HREF = "http://test:8080/img";

    private static final String EMPTY_DOCUMENT_MESSAGE = "Document management failed uploading file: " + DOCUMENT_NAME;

    private static final String SERVER_ERROR_MESSAGE = "Failed to connect with case document upload API";

    private static final MockMultipartFile MOCK_FILE = new MockMultipartFile(
        "file",
        DOCUMENT_NAME,
        MediaType.TEXT_PLAIN_VALUE,
        "Hello, World!".getBytes()
      );

    private static final String MOCK_RESPONSE_WITH_DOCUMENT = "{\"documents\":[{\"originalDocumentName\":"
        + "\"claim-submit.png\",\"links\":{\"self\":{\"href\": \"" + MOCK_HREF + "\"}}}]}";

    private static final String MOCK_RESPONSE_WITHOUT_DOCUMENT = "{\"documents\":[]}";

    private CaseDocumentService caseDocumentService;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        AuthTokenGenerator authTokenGenerator = () -> "test";
        caseDocumentService = new CaseDocumentService(restTemplate,
                                                      authTokenGenerator,
                                                      DOCUMENT_UPLOAD_API_URL);
        mockServer = MockRestServiceServer.createServer(restTemplate);

    }

    @Test
    void theUploadDocWithFileProducesSuccessWithFileUri() {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(MOCK_RESPONSE_WITH_DOCUMENT));

        URI documentEndpoint = caseDocumentService.uploadDocument(MOCK_TOKEN, CASE_TYPE, MOCK_FILE);

        assertThat(documentEndpoint.toString()).isEqualTo(MOCK_HREF);
    }

    @Test
    void theUploadDocWhenNoFileReturnedProducesDocException() {

        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(MOCK_RESPONSE_WITHOUT_DOCUMENT));

        DocumentManagementException documentException = assertThrows(
            DocumentManagementException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getMessage())
            .isEqualTo(EMPTY_DOCUMENT_MESSAGE);
    }

    @Test
    void theUploadDocWhenRestTemplateFailsProducesDocException() {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        DocumentManagementException documentException = assertThrows(
            DocumentManagementException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getMessage())
            .isEqualTo(SERVER_ERROR_MESSAGE);
    }
}
