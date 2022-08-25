package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
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
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceUtil;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SuppressWarnings({"PMD.TooManyMethods"})
@Slf4j
@ExtendWith(MockitoExtension.class)
class CaseDocumentServiceTest {

    private static final String AUTH_TOKEN =
        "Bearer eyJ6aXAiOiJOT05FIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYiLCJraWQiOiJNakF6TXpRd09UVTRPVFl4TmpJPSJ9.ey" +
            "JzdWIiOiJldC5kZXZAaG1jdHMubmV0IiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiZmM0NzgxOTctOGZlNC0" +
            "0YTZlLTg5YjQtZWE0ODgyMjA0NzkyIiwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob3N0OjU1NTYiLCJ0b2tlbk5hbWUiOiJhY2Nlc3" +
            "NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiIzOWU1NzNlOS04YmUxLTQxYTUtYTgwNy02MDdkO" +
            "WI3MzhkMzMiLCJhdWQiOiJzeWEtYXBpIiwibmJmIjoxNjYxMjYzMTkyLCJncmFudF90eXBlIjoiYXV0aG9yaXphdGlvbl9jb2RlI" +
            "iwiYXV0aF90aW1lIjoxNjYxMjYzMTkyMjIwLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiXSwicmVhbG0iOiJc" +
            "L2htY3RzIiwiZXhwIjoxNjYxMjkxOTkyLCJpYXQiOjE2NjEyNjMxOTIsImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiIyNWMwYzA1" +
            "Mi1iYjg4LTRmMmEtYTc3NC1lMmNhZGIzODY1NGIifQ.J1gajKWx-hN8fpG2gtyBapQiaUv53XwpeWFf1Yt4SYBsuYcR_3o2_Ml7QG" +
            "dKJcDuIX3RKjNVzem9HZvl29_UlfNz8QPNf8zHYR3fTX7dLQ0vehUHOm4R1eHb1P2xyPSduWRq7rzZRuLPMK1jgqeUMGL7r_eX4KXdt" +
            "8sE0RUHI1M-nYo2oVXHu-Ndws-vTzy-mxP-CR-2hEMkm3rWnzbAB1YNsC6xUrxWuEEAWxwI_NUkAx5JJ1dm8dZNoo0th7OFvd34h" +
            "4XcPkJkmxa5WWOZpzXHfm7lMnQ1CZArcf-XeScOkuPsvGu-66HoCdAmFP22JHJFHskWlH2m8LZwroqGSw";

    private static final String SERVICE_AUTH = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJldF9zeWFfYXBpIiwiZXhwIjoxN" +
        "jYxMjc3NzMwfQ.kyWYDbE4-o_Gmw98bVqWC8OWyNowAk9g18oRlruG4yzCTsbPzn7DcPx4RTqnjmU2XOgWoAHE0mWx6GYUqp6KyA";

    private static final String DOCUMENT_UPLOAD_API_URL = "http://localhost:4455/cases/documents";
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
    private final String FULL_JSON_RESPONSE = ResourceUtil.resourceAsString(
        "responses/caseDocumentUpload.json"
    );

    private CaseDocumentService caseDocumentService;
    private MockRestServiceServer mockServer;

    CaseDocumentServiceTest() throws IOException {
    }

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        AuthTokenGenerator authTokenGenerator = () -> SERVICE_AUTH;
        caseDocumentService = new CaseDocumentService(restTemplate,
                                                      authTokenGenerator,
                                                      DOCUMENT_UPLOAD_API_URL, 3);
        mockServer = MockRestServiceServer.createServer(restTemplate);

    }

    @Test
    void theUploadDocWithFileProducesSuccessWithFileUri() throws CaseDocumentException {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(MOCK_RESPONSE_WITH_DOCUMENT));

        String documentEndpoint =
            caseDocumentService.uploadDocument(MOCK_TOKEN, CASE_TYPE, MOCK_FILE).getLinks().get("self").get("href");

        assertThat(documentEndpoint)
            .hasToString(MOCK_HREF);
    }

    @Test
    void fullJsonResponseIsSuccessful() throws CaseDocumentException {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(FULL_JSON_RESPONSE));

        String documentEndpoint =
            caseDocumentService.uploadDocument(MOCK_TOKEN, CASE_TYPE, MOCK_FILE).getLinks().get("self").get("href");

        assertThat(documentEndpoint)
            .hasToString(MOCK_HREF);
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
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_UPLOAD_API_URL))
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

        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_UPLOAD_API_URL))
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
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_UPLOAD_API_URL))
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
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_UPLOAD_API_URL))
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
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_UPLOAD_API_URL))
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
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_UPLOAD_API_URL))
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
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_UPLOAD_API_URL))
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
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(MOCK_RESPONSE_WITH_DOCUMENT));

        String documentEndpoint = caseDocumentService.uploadDocument(
            MOCK_TOKEN, CASE_TYPE, MOCK_FILE_NAME_SPACING).getLinks().get("self").get("href");

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
        mockServer.expect(ExpectedCount.max(MAX_API_CALL_ATTEMPTS), requestTo(DOCUMENT_UPLOAD_API_URL))
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
}
