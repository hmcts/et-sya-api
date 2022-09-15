package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
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
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.ResourceNotFoundException;
import uk.gov.hmcts.reform.et.syaapi.models.DocumentDetailsResponse;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.RESOURCE_NOT_FOUND;

@SuppressWarnings({"PMD.TooManyMethods"})
@Slf4j
@ExtendWith(MockitoExtension.class)
class CaseDocumentServiceTest {

    private static final String DOCUMENT_SERVICE_API_URL = "http://localhost:4455";
    private static final String DOCUMENT_API_URL = "http://localhost:4455/cases/documents";
    private static final String DOCUMENT_API_URL_WITH_SLASH = "http://localhost:4455/cases/documents/";
    private static final String DOCUMENT_NAME = "hello.txt";
    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String MOCK_TOKEN = "Bearer Token";
    private static final String MOCK_HREF = "http://test:8080/img";
    private static final String MOCK_HREF_MALFORMED = "http:/test:80/";
    private static final String EMPTY_DOCUMENT_MESSAGE = "Document management failed uploading file: " + DOCUMENT_NAME;
    private static final String SERVER_ERROR_MESSAGE = "Failed to upload Case Document";
    private static final String FILE_DOES_NOT_PASS_VALIDATION = "File does not pass validation";
    private static final Integer MAX_API_CALL_ATTEMPTS = 4;
    private static final String MOCK_FILE_BODY = "Hello, World!";
    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final Date TEST_DATE = new Date();
    private static final MockMultipartFile MOCK_FILE = new MockMultipartFile(
        "mock_file",
        DOCUMENT_NAME,
        MediaType.TEXT_PLAIN_VALUE,
        MOCK_FILE_BODY.getBytes()
      );
    private static final MockMultipartFile MOCK_FILE_WITHOUT_NAME = new MockMultipartFile(
        "mock_file_without_name",
        null,
        MediaType.TEXT_PLAIN_VALUE,
        MOCK_FILE_BODY.getBytes()
    );
    private static final MockMultipartFile MOCK_FILE_INVALID_NAME = new MockMultipartFile(
        "mock_file_with_invalid_name",
        "invalid",
        MediaType.TEXT_PLAIN_VALUE,
        MOCK_FILE_BODY.getBytes()
    );
    private static final MockMultipartFile MOCK_FILE_NAME_SPACING = new MockMultipartFile(
        "mock_file_with_name_spacing",
        "valid file.xyz",
        MediaType.TEXT_PLAIN_VALUE,
        MOCK_FILE_BODY.getBytes()
    );
    private static final MockMultipartFile MOCK_FILE_NAME_ILLEGAL_CHAR = new MockMultipartFile(
        "mock_file_name_with_illegal_char",
        "@invalid!|.xyz",
        MediaType.TEXT_PLAIN_VALUE,
        MOCK_FILE_BODY.getBytes()
    );
    private static final MockMultipartFile MOCK_FILE_CORRUPT = new MockMultipartFile(
        "mock_file_corrupt",
        DOCUMENT_NAME,
        MediaType.IMAGE_GIF_VALUE,
        (byte[]) null
    );
    private static final String RESPONSE_BODY = "{\"documents\":[{\"originalDocumentName\":";
    private static final String MOCK_RESPONSE_WITH_DOCUMENT = RESPONSE_BODY
        + "\"claim-submit.png\",\"_links\":{\"self\":{\"href\": \"" + MOCK_HREF + "\"}}}]}";
    private static final String MOCK_RESPONSE_WITHOUT_DOCUMENT = "{\"documents\":[]}";
    private static final String MOCK_RESPONSE_WITHOUT_LINKS = RESPONSE_BODY
        + "\"claim-submit.png\"}]}";
    private static final String MOCK_RESPONSE_WITHOUT_HREF = RESPONSE_BODY
        + "\"claim-submit.png\",\"_links\":{\"self\":{}}]}";
    private static final String MOCK_RESPONSE_INCORRECT = "{\"doucments\":[{\"originalDocumentName\":"
        + "\"claim-submit.png\",\"_links\":{\"self\":{\"href\": \"" + MOCK_HREF + "\"}}}]}";
    private static final String MOCK_RESPONSE_WITH_MALFORMED_URI = RESPONSE_BODY
        + "\"claim-submit.png\",\"_links\":{\"self\":{\"href\": \"" + MOCK_HREF_MALFORMED + "\"}}}]}";
    private static final String MOCK_RESPONSE_WITHOUT_SELF = RESPONSE_BODY
        + "\"claim-submit.png\",\"_links\":{}}]}";

    private CaseDocumentService caseDocumentService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        AuthTokenGenerator authTokenGenerator = () -> "Bearer Mock";
        caseDocumentService = new CaseDocumentService(restTemplate,
                                                      authTokenGenerator,
                                                      DOCUMENT_SERVICE_API_URL, 3);
        mockServer = MockRestServiceServer.createServer(restTemplate);

    }

    @Test
    void theUploadDocWithFileProducesSuccessWithFileUri() throws CaseDocumentException {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(MOCK_RESPONSE_WITH_DOCUMENT));

        URI documentEndpoint = caseDocumentService.uploadDocument(MOCK_TOKEN, CASE_TYPE, MOCK_FILE);

        assertThat(documentEndpoint)
            .hasToString(MOCK_HREF);
    }

    @Test
    void theUploadDocWhenNoFileReturnedProducesDocException() {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_API_URL))
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
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getMessage())
            .isEqualTo(SERVER_ERROR_MESSAGE);
    }

    @Test
    void theUploadDocWhenIoExceptionProducesDocException() throws IOException {
        IOException ioException = new IOException("Test throw");

        MockMultipartFile mockMultipartFileSpy = Mockito.spy(new MockMultipartFile("mock_file_spy",
            DOCUMENT_NAME,
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()));

        doThrow(ioException).when(mockMultipartFileSpy).getBytes();

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, mockMultipartFileSpy));

        assertThat(documentException.getCause())
            .isEqualTo(ioException);
    }

    @Test
    void theUploadDocWhenRestExceptionProducesDocException() {
        RestClientException restClientException = new RestClientException("Test throw");

        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond((response) -> {
                throw restClientException;
            });

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getCause())
            .isEqualTo(restClientException);
    }

    @Test
    void theUploadDocWhenResponseNoLinkProducesDocException() {
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(MOCK_RESPONSE_WITHOUT_LINKS));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getMessage())
            .isEqualTo(EMPTY_DOCUMENT_MESSAGE);
    }

    @Test
    void theUploadDocWhenResponseNoHrefProducesDocException() {
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(MOCK_RESPONSE_WITHOUT_HREF));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getCause().getClass())
            .isEqualTo(RestClientException.class);
    }

    @Test
    void theUploadDocWhenResponseNoSelfNameProducesDocException() {
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(MOCK_RESPONSE_WITHOUT_SELF));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getMessage())
            .isEqualTo(EMPTY_DOCUMENT_MESSAGE);
    }

    @Test
    void theUploadDocWhenResponseIncorrectProducesDocException() {
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(MOCK_RESPONSE_INCORRECT));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getMessage())
            .isEqualTo(EMPTY_DOCUMENT_MESSAGE);
    }

    @Test
    void theUploadDocWhenResponseWithoutFilenameProducesDocException() {
        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE_WITHOUT_NAME));

        assertThat(documentException.getMessage())
            .isEqualTo(FILE_DOES_NOT_PASS_VALIDATION);
    }

    @Test
    void theUploadDocWhenUnauthorizedProducesHttpException() {
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getCause().getClass())
            .isEqualTo(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    void theUploadDocWhenInvalidFilenameProducesDocException() {
        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE_INVALID_NAME));

        assertThat(documentException.getMessage())
            .isEqualTo(FILE_DOES_NOT_PASS_VALIDATION);
    }

    @Test
    void theUploadDocWhenFilenameWithSpaceProducesSuccessWithFileUri() throws CaseDocumentException {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(MOCK_RESPONSE_WITH_DOCUMENT));

        URI documentEndpoint = caseDocumentService.uploadDocument(
            MOCK_TOKEN, CASE_TYPE, MOCK_FILE_NAME_SPACING);

        assertThat(documentEndpoint)
            .hasToString(MOCK_HREF);
    }

    @Test
    void theUploadDocWhenFilenameWithIllegalCharProducesDocException() {
        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE_NAME_ILLEGAL_CHAR));

        assertThat(documentException.getMessage())
            .isEqualTo(FILE_DOES_NOT_PASS_VALIDATION);
    }

    @Test
    void theUploadDocWhenResponseUriInvalidProducesException() {
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(MOCK_RESPONSE_WITH_MALFORMED_URI));

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE));

        assertThat(documentException.getMessage())
            .isEqualTo(EMPTY_DOCUMENT_MESSAGE);
    }

    @Test
    void theUploadDocWhenContentTypeDoesNotMatchActualFileTypeProducesDocException() {
        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, MOCK_FILE_CORRUPT));

        assertThat(documentException.getMessage())
            .isEqualTo(FILE_DOES_NOT_PASS_VALIDATION);
    }

    @Test
    void downloadDocumentSuccess() {
        ByteArrayResource mockByteArrayResponse = new ByteArrayResource("test document content".getBytes());
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_API_URL_WITH_SLASH + DOCUMENT_ID + "/binary"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(mockByteArrayResponse));

        assertThat(caseDocumentService.downloadDocument(MOCK_TOKEN, DOCUMENT_ID).getBody())
            .isEqualTo(mockByteArrayResponse);
    }

    @Test
    void downloadDocumentResourceNotFound() {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_API_URL_WITH_SLASH + DOCUMENT_ID + "/binary"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResourceNotFoundException resourceNotFoundException = assertThrows(
            ResourceNotFoundException.class, () -> caseDocumentService.downloadDocument(MOCK_TOKEN, DOCUMENT_ID));

        assertThat(resourceNotFoundException.getMessage())
            .isEqualTo(String.format(RESOURCE_NOT_FOUND, DOCUMENT_ID, "404 Not Found: [no body]"));
    }

    @Test
    void documentDetailsSuccess() {
        DocumentDetailsResponse mockDocumentDetailsResponse = new DocumentDetailsResponse(
            Classification.PUBLIC,
            100L,
            "mimeType",
            "docName",
            "token",
            TEST_DATE,
            "createdBy",
            "lastModifiedBy",
            TEST_DATE,
            TEST_DATE,
            Map.of("test1", "test2")
        );
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_API_URL_WITH_SLASH + DOCUMENT_ID))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(ResourceLoader.toJson(mockDocumentDetailsResponse)));

        assertThat(caseDocumentService.getDocumentDetails(MOCK_TOKEN, DOCUMENT_ID).getBody())
            .usingRecursiveComparison()
            .isEqualTo(mockDocumentDetailsResponse);
    }

    @Test
    void documentDetailsResourceNotFound() {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_API_URL_WITH_SLASH + DOCUMENT_ID))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResourceNotFoundException resourceNotFoundException = assertThrows(
            ResourceNotFoundException.class, () -> caseDocumentService.getDocumentDetails(MOCK_TOKEN, DOCUMENT_ID));

        assertThat(resourceNotFoundException.getMessage())
            .isEqualTo(String.format(RESOURCE_NOT_FOUND, DOCUMENT_ID, "404 Not Found: [no body]"));
    }

}
