package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimCaseDocument;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@Slf4j
class DocumentGenerationServiceTest {

    private static final String TORNADO_API_URL = "http://someurl.com";
    private static final byte[] RESPONSE_BODY = "Some response content".getBytes();
    private DocumentGenerationService documentGenerationService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        documentGenerationService = new DocumentGenerationService(restTemplate,
            TORNADO_API_URL, "somekey");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void genDocumentWithDataRetrievesPdfDocument()
        throws DocumentGenerationException {
        ClaimCaseDocument claimCaseDocument = createClaimCase();

        mockServer.expect(ExpectedCount.once(), requestTo(TORNADO_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.accessKey").value("somekey"))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_PDF)
                .body(RESPONSE_BODY));

        byte[] pdfDocument = documentGenerationService.genPdfDocument(
            "EM-TRB-helloworld.docx",
            "document.pdf",
            claimCaseDocument);
        assertThat(pdfDocument).hasSameSizeAs(RESPONSE_BODY.length);
    }

    @Test
    void genDocumentWithEmptySourceDataCausesDocumentGenerationException() {
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "EM-TRB-helloworld.docx",
                "output.pdf",
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("blah");   // todo - change 'blah' at the right time!!!
    }

    @Test
    void genDocumentWithNullSourceDataCausesNullPointerException() {
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> documentGenerationService.genPdfDocument(
                "EM-TRB-helloworld.docx",
                "output.pdf",
                null));
        assertThat(exception.getMessage()).isEqualTo("blah");
    }

    @Test
    void genDocumentWithUnknownTemplateNameCausesDocumentGenerationException() {
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "a.docx",
                "output.pdf",
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("Failed to generate the PDF document: a.docx");
    }

    @Test
    void genDocumentWithUnknownTemplateFileFormatCausesDocumentGenerationException() {
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "EM-TRB-helloworld.xlsx",
                "output.pdf",
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("Unknown Template: EM-TRB-ET-00001.xlsx");
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
        testForInvalidTemplateOrOutputFilename("EM-TRB-helloworld.docx", "", "Invalid output file name: ");
    }

    @Test
    void genDocumentWithNullOutputFileNameCausesNullPointerException() {
        testForInvalidTemplateOrOutputFilename("EM-TRB-helloworld.docx", null, "Invalid output file name: null");
    }

    @Test
    void genDocumentWithUnknownOutputFileFormatCausesDocumentGenerationException() {
        testForInvalidTemplateOrOutputFilename("EM-TRB-helloworld.docx", "output.xlsx",
            "Invalid output file name: output.xlsx");
    }

    private void testForInvalidTemplateOrOutputFilename(String templateName, String outputFilename,
                                                        String expectedExpectedMessage) {
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                templateName,
                outputFilename,
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo(expectedExpectedMessage);
    }

    @Test
    void genDocumentFailsToConnectToDocmosisCausesDocumentGenerationException() {
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "EM-TRB-helloworld.docx",
                "somefile.pdf",
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("Failed to connect with Docmosis");
        assertThat(exception.getCause()).isInstanceOf(IOException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo(
            "blah"); // todo - populate once we've seen what really comes back
    }

    private ClaimCaseDocument createClaimCase() {
        ClaimCaseDocument claimCaseDocument = new ClaimCaseDocument();
        claimCaseDocument.setTest_message("Hello World");
        return claimCaseDocument;
    }
}
