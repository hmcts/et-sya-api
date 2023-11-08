package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimCaseDocument;

import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SuppressWarnings({"PMD.TooManyMethods"})
class DocumentGenerationServiceTest {

    private static final String TORNADO_API_URL = "http://someurl.com";
    private static final byte[] RESPONSE_BODY = "Some response content".getBytes();
    public static final String SOME_KEY = "some_key";
    public static final String ACCESS_KEY = "$.accessKey";
    public static final String EM_TRB_HELLO_WORLD_DOCX = "EM-TRB-helloworld.docx";
    public static final String DOCUMENT_PDF = "document.pdf";
    public static final String FAILED_TO_CONNECT_WITH_TORNADO = "Failed to connect with Tornado";
    private DocumentGenerationService documentGenerationService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        documentGenerationService = new DocumentGenerationService(restTemplate, objectMapper,
            TORNADO_API_URL, SOME_KEY);
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void genDocumentWithDataRetrievesPdfDocument()
        throws DocumentGenerationException {
        ClaimCaseDocument claimCaseDocument = createClaimCase();

        mockServer.expect(ExpectedCount.once(), requestTo(TORNADO_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath(ACCESS_KEY).value(SOME_KEY))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_PDF)
                .body(RESPONSE_BODY));

        byte[] pdfDocument = documentGenerationService.genPdfDocument(
            EM_TRB_HELLO_WORLD_DOCX, DOCUMENT_PDF, claimCaseDocument);
        assertThat(pdfDocument).hasSize(RESPONSE_BODY.length);
    }

    @Test
    void genDocumentWithDataFailingToConvertToJsonThrowsDocumentGenerationException()
        throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        given(objectMapper.writeValueAsString(any()))
            .willThrow(new JsonParseException(null, "wellthatworkednot"));
        DocumentGenerationService localDocumentGenerationService = new DocumentGenerationService(restTemplate,
            objectMapper,
            TORNADO_API_URL, SOME_KEY);

        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> localDocumentGenerationService.genPdfDocument(
                EM_TRB_HELLO_WORLD_DOCX, DOCUMENT_PDF, new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("Failed to convert the TornadoRequestWrapper to a string");
    }

    @Test
    void genDocumentWithNullSourceDataCausesNullPointerException() {
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                EM_TRB_HELLO_WORLD_DOCX, DOCUMENT_PDF, null));
        assertThat(exception.getMessage()).isEqualTo("sourceData MUST NOT be null");
    }

    @Test
    void genDocumentWithUnknownTemplateNameCausesDocumentGenerationException() {
        mockServer.expect(ExpectedCount.once(), requestTo(TORNADO_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath(ACCESS_KEY).value(SOME_KEY))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PDF)
                .body("{\"succeeded\":false,\"shortMsg\":\"Unable to render - template does not exist [a.docx]\"}"));

        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "a.docx", DOCUMENT_PDF, new ClaimCaseDocument()));

        assertThat(exception.getMessage()).isEqualTo(FAILED_TO_CONNECT_WITH_TORNADO);
        assertThat(exception.getCause().getMessage()).isEqualTo("400 Bad Request: \"{\"succeeded\":false,"
            + "\"shortMsg\":\"Unable to render - template does not exist [a.docx]\"}\"");
    }

    @Test
    void genDocumentWithUnknownTemplateFileFormatCausesDocumentGenerationException() {
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "EM-TRB-helloworld.xlsx", DOCUMENT_PDF, new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("Unknown Template: EM-TRB-helloworld.xlsx");
    }

    @Test
    void genDocumentWithBlankTemplateNameCausesDocumentGenerationException() {
        testForInvalidTemplateOrOutputFilename("", "output.pdf", "Unknown Template: ");
    }

    @Test
    void genDocumentWithNullTemplateNameCausesNullPointerException() {
        testForInvalidTemplateOrOutputFilename(null, "output.pdf", "Unknown Template: null");
    }

    @Test
    void genDocumentWithBlankOutputFileNameCausesDocumentGenerationException() {
        testForInvalidTemplateOrOutputFilename(EM_TRB_HELLO_WORLD_DOCX, "", "Invalid output file name: ");
    }

    @Test
    void genDocumentWithNullOutputFileNameCausesNullPointerException() {
        testForInvalidTemplateOrOutputFilename(EM_TRB_HELLO_WORLD_DOCX, null, "Invalid output file name: null");
    }

    @Test
    void genDocumentWithUnknownOutputFileFormatCausesDocumentGenerationException() {
        testForInvalidTemplateOrOutputFilename(EM_TRB_HELLO_WORLD_DOCX, "output.xlsx",
            "Invalid output file name: output.xlsx");
    }

    private void testForInvalidTemplateOrOutputFilename(String templateName, String outputFilename,
                                                        String expectedExpectedMessage) {
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                templateName, outputFilename, new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo(expectedExpectedMessage);
    }

    @Test
    void genDocumentFailsToConnectToTornadoCausesDocumentGenerationException() {

        mockServer.expect(ExpectedCount.once(), requestTo(TORNADO_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath(ACCESS_KEY).value(SOME_KEY))
            .andRespond(withException(new UnknownHostException()));

        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                EM_TRB_HELLO_WORLD_DOCX, DOCUMENT_PDF, new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo(FAILED_TO_CONNECT_WITH_TORNADO);
        assertThat(exception.getCause()).isInstanceOf(ResourceAccessException.class);
        assertThat(exception.getCause().getCause()).isInstanceOf(UnknownHostException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("I/O error on POST request for \"" + TORNADO_API_URL
            + "\": null");
    }

    @Test
    void genDocumentFailsToConnectToTornadoCausedByUnsupportedMediaType() {

        mockServer.expect(ExpectedCount.once(), requestTo(TORNADO_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath(ACCESS_KEY).value(SOME_KEY))
            .andRespond(withStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body("idontcare"));

        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                EM_TRB_HELLO_WORLD_DOCX, DOCUMENT_PDF, new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo(FAILED_TO_CONNECT_WITH_TORNADO);
        assertThat(exception.getCause()).isInstanceOf(HttpClientErrorException.UnsupportedMediaType.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("415 Unsupported Media Type: \"idontcare\"");
    }

    @Test
    void genDocumentWithUnknownAccessKeyCausesDocumentGenerationException() {
        mockServer.expect(ExpectedCount.once(), requestTo(TORNADO_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath(ACCESS_KEY).value(SOME_KEY))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PDF)
                .body("{\"succeeded\":false,\"shortMsg\":\"Bad request for render:Invalid accessKey\"}"));

        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "a.docx", DOCUMENT_PDF, new ClaimCaseDocument()));

        assertThat(exception.getMessage()).isEqualTo(FAILED_TO_CONNECT_WITH_TORNADO);
        assertThat(exception.getCause().getMessage()).isEqualTo("400 Bad Request: \"{\"succeeded\":false,"
            + "\"shortMsg\":\"Bad request for render:Invalid accessKey\"}\"");
    }

    private ClaimCaseDocument createClaimCase() {
        ClaimCaseDocument claimCaseDocument = new ClaimCaseDocument();
        claimCaseDocument.setTestMessage("Hello World");
        return claimCaseDocument;
    }
}
