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

@Slf4j
@ExtendWith(MockitoExtension.class)
class CaseDocumentServiceTest {

    private static String AUTH_TOKEN_TOKEN = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJldF9zeWFfYXBpIiwiZX" +
        "hwIjoxNjU0NjIzNTM4fQ.X3CfXLygNoxCYcPlx5P1OHMp9JmX7sXFoz6Q0s7r0bsv4yX6sxGVWv7" +
        "IdUa9Ak4mCiUxh6hzj2isVxHxB7oVfw";

    private static String BEARER_TOKEN = "eyJ6aXAiOiJOT05FIiwidHlwIjoiSldU" +
        "IiwiYWxnIjoiUlMyNTYiLCJraWQiOiJNVFV5TWpReU5UUTJORFl5TWprMiJ9.eyJzdWIiOiJl" +
        "dC5kZXZAaG1jdHMubmV0IiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiOTdhZG" +
        "M5NGQtMjdlZC00ODg2LTkzZmYtZjY5MDcwZWVjM2ExIiwiaXNzIjoiaHR0cDpcL1wvbG9jYWxob" +
        "3N0OjU1NTYiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIi" +
        "wiYXV0aEdyYW50SWQiOiJlNGVmOTU5Zi02MThlLTQxMWUtOWY0Yi05OTM2Yjc1ODZiMDkiLCJhdWQiOiJ" +
        "zeWEtYXBpIiwibmJmIjoxNjU0NjA0NzU1LCJncmFudF90eXBlIjoiYXV0aG9yaXphdGlvbl9jb2RlIi" +
        "wiYXV0aF90aW1lIjoxNjU0NjA0NzU1MjAwLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9" +
        "sZXMiXSwicmVhbG0iOiJcL2htY3RzIiwiZXhwIjoxNjU0NjMzNTU1LCJpYXQiOjE2NTQ2MDQ3NTUsIm" +
        "V4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiIwZTY5M2EyYi02ZDQ4LTRhZmMtOTM2Ni02NjI5NmFmYmFl" +
        "MDUifQ.eWU-w1PI4Rc7m601_sRZlSfJ5nVb5i_HhSUGCb-2Cb4kMpeVLaEmVaZOy-7JGsJqig72H0zC" +
        "5Bez2cN4a5wyKUGiQpOJYZhCipzXaoGxPXoFze6Tbq56yzSdkHFdywFOXtYHrEc9pisXsYoZsL1uw96" +
        "7x4Flnsy53YObCEv5yAWoIQeTztxHysripgF6GDF3z_5pcFA96UGrMS7e1Hh79bBkYrdJTrH3fGQxgm" +
        "cWwYspXlgqjkDx9cQdkUqK8Ze2JsxDKpKUKrEQlEmFjtlqjPmddA9vXOP_z0wGtr9lsE2wlrjXy06y" +
        "vTy50YsSoBq1uEZE4_koGxoWCimWiHKDUw";

    private static final String DOCUMENT_UPLOAD_API_URL = "http://localhost:4455/cases/documents";

    private static final String DOCUMENT_NAME = "hello.txt";

    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String MOCK_TOKEN = "Bearer Token";

    private static final String MOCK_HREF = "http://test:8080/img";

    private static final String MOCK_HREF_MALFORMED = "http:/test:80/";

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

    private static final String MOCK_RESPONSE_WITH_MALFORMED_URI = "{\"documents\":[{\"originalDocumentName\":"
        + "\"claim-submit.png\",\"links\":{\"self\":{\"href\": \"" + MOCK_HREF_MALFORMED + "\"}}}]}";

    private CaseDocumentService caseDocumentService;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        AuthTokenGenerator authTokenGenerator = () -> "Bearer Mock";
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

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
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
                MOCK_TOKEN, CASE_TYPE, mockMultipartFile));

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

    @Test
    void theUploadDocWhenInvalidFilenameProducesDocException() {
        MockMultipartFile fileWithInvalidName = new MockMultipartFile(
            "file",
            "invalid",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()
        );

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, fileWithInvalidName));

        assertThat(documentException.getMessage()).isEqualTo(NO_FILENAME_MESSAGE);
    }

    @Test
    void theUploadDocWhenFilenameWithSpaceProducesSuccessWithFileUri() throws CaseDocumentException {
        MockMultipartFile fileWithInvalidName = new MockMultipartFile(
            "file",
            "valid file.xyz",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()
        );

        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(MOCK_RESPONSE_WITH_DOCUMENT));

        URI documentEndpoint = caseDocumentService.uploadDocument(MOCK_TOKEN, CASE_TYPE, fileWithInvalidName);

        assertThat(documentEndpoint).hasToString(MOCK_HREF);
    }

    @Test
    void theUploadDocWhenFilenameWithIllegalCharProducesDocException() {
        MockMultipartFile fileWithInvalidName = new MockMultipartFile(
            "file",
            "@invalid!|.xyz",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()
        );

        CaseDocumentException documentException = assertThrows(
            CaseDocumentException.class, () -> caseDocumentService.uploadDocument(
                MOCK_TOKEN, CASE_TYPE, fileWithInvalidName));

        assertThat(documentException.getMessage()).isEqualTo(NO_FILENAME_MESSAGE);
    }

    @Test
    void theUploadDocWhenResponseUriInvalidProducesException() {
        mockServer.expect(ExpectedCount.once(), requestTo(DOCUMENT_UPLOAD_API_URL))
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
    void callAPI() throws CaseDocumentException {

        RestTemplate restTemplate = new RestTemplate();
        AuthTokenGenerator authTokenGenerator = () -> AUTH_TOKEN_TOKEN;
        CaseDocumentService apiService = new CaseDocumentService(restTemplate,
            authTokenGenerator,
            DOCUMENT_UPLOAD_API_URL);

        URI result = apiService.uploadDocument(BEARER_TOKEN, CASE_TYPE, MOCK_FILE);

        log.info(result.toString());
    }

    // TODO: 01/06/2022 What if the MultiPartFile is corrupt?
    // create 4-byte array with control ascii characters as corrupt data
    // find out how api manages file uploads
}
