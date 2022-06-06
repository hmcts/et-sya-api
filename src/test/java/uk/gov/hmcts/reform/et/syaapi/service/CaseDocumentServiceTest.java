package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(MockitoExtension.class)
class CaseDocumentServiceTest {

    private static final String DOCUMENT_UPLOAD_API_URL = "http://someurl.com";

    private static final String DOCUMENT_NAME = "hello.txt";

    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String MOCK_TOKEN = "Bearer Token";

    private static final String MOCK_HREF = "http://test:8080/img";

    private static final String EMPTY_DOCUMENT_MESSAGE = "Document management failed uploading file: " + DOCUMENT_NAME;

    private static final String SERVER_ERROR_MESSAGE = "Failed to upload Case Document";

    private static final String NO_FILENAME_MESSAGE = "File does not pass validation";

    private static final MockMultipartFile MOCK_FILE = new MockMultipartFile(
        "file",
        DOCUMENT_NAME,
        MediaType.TEXT_PLAIN_VALUE,
        "Hello, World!".getBytes()
      );

    private static final MockMultipartFile MOCK_FILE_WITHOUT_NAME = new MockMultipartFile(
        "file",
        null,
        MediaType.TEXT_PLAIN_VALUE,
        "Hello, World!".getBytes()
    );

    private static final String MOCK_RESPONSE_WITH_DOCUMENT = "{\"documents\":[{\"originalDocumentName\":"
        + "\"claim-submit.png\",\"links\":{\"self\":{\"href\": \"" + MOCK_HREF + "\"}}}]}";

    private static final String MOCK_RESPONSE_WITHOUT_DOCUMENT = "{\"documents\":[]}";

    private static final String MOCK_RESPONSE_WITHOUT_LINKS = "{\"documents\":[{\"originalDocumentName\":"
        + "\"claim-submit.png\"}]}";

    private static final String MOCK_RESPONSE_WITHOUT_HREF = "{\"documents\":[{\"originalDocumentName\":"
        + "\"claim-submit.png\",\"links\":{\"self\":{}}]}";

    private static final String MOCK_RESPONSE_INCORRECT = "{\"doucments\":[{\"originalDocumentName\":"
        + "\"claim-submit.png\",\"links\":{\"self\":{\"href\": \"" + MOCK_HREF + "\"}}}]}";

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
    void theUploadDocWithFileProducesSuccessWithFileUri() throws CaseDocumentException {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(MOCK_RESPONSE_WITH_DOCUMENT));

        URI documentEndpoint = caseDocumentService.uploadDocument(MOCK_TOKEN, CASE_TYPE, MOCK_FILE);

        assertThat(documentEndpoint).hasToString(MOCK_HREF);
    }

    @Test
    void theUploadDocWhenNoFileReturnedProducesDocException() {

        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(MOCK_RESPONSE_WITHOUT_DOCUMENT));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getMessage())
            .isEqualTo(EMPTY_DOCUMENT_MESSAGE);
    }

    @Test
    void theUploadDocWhenRestTemplateFailsProducesDocException() {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getMessage())
            .isEqualTo(SERVER_ERROR_MESSAGE);
    }

    @Test
    void theUploadDocWhenIOExceptionProducesDocException() {

        IOException ioException = new IOException("Test throw");

        MockMultipartFile MOCK_FILE_IO_EXCEPTION = new MockMultipartFile(
            "file",
            DOCUMENT_NAME,
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()
        ) {
            @Override
            public byte[] getBytes() throws IOException {
                throw ioException;
            }
        };

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE_IO_EXCEPTION));

        assertThat(documentException.getCause()).isEqualTo(ioException);
    }

    @Test
    void theUploadDocWhenRestExceptionProducesDocException() {

        RestClientException restClientException = new RestClientException("Test throw");

        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond((response) -> {
                throw restClientException;
            });

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getCause()).isEqualTo(restClientException);
    }

    @Test
    void theUploadDocWhenResponseNoLinkProducesDocException() {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(MOCK_RESPONSE_WITHOUT_LINKS));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getCause().getClass()).isEqualTo(NullPointerException.class);
    }

    @Test
    void theUploadDocWhenResponseNoHrefProducesDocException() {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(MOCK_RESPONSE_WITHOUT_HREF));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getCause().getClass()).isEqualTo(RestClientException.class);
    }

    @Test
    void theUploadDocWhenResponseIncorrectProducesDocException() {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(MOCK_RESPONSE_INCORRECT));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getMessage()).isEqualTo(EMPTY_DOCUMENT_MESSAGE);
    }

    @Test
    void theUploadDocWhenResponseWithoutFilenameProducesDocException() {

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE_WITHOUT_NAME));

        assertThat(documentException.getMessage()).isEqualTo(NO_FILENAME_MESSAGE);
    }

    @Test
    void theUploadDocWhenUnauthorizedProducesHttpException() {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getCause().getClass()).isEqualTo(HttpClientErrorException.Unauthorized.class);
    }



    // TODO: 01/06/2022 What if it converts but doesn't have the values you'd expect?
    // TODO: 01/06/2022 What if the MultiPartFile is corrupt?
}
