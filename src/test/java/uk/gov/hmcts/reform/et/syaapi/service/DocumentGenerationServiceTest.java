package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimCaseDocument;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentGenerationServiceTest {

    @Test
    void genDocumentWithDataRetrievesPdfDocument()
        throws DocumentGenerationException {
        DocumentGenerationService documentGenerationService = new DocumentGenerationService();
        byte[] pdfDocument = documentGenerationService.genPdfDocument(
            "EM-TRB-ET-00001.docx",
            "output.pdf",
            new ClaimCaseDocument());
        assertThat(pdfDocument).isNotEmpty();
    }

    @Test
    void genDocumentWithEmptySourceDataCausesDocumentGenerationException() {
        DocumentGenerationService documentGenerationService = new DocumentGenerationService();
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "EM-TRB-ET-00001.docx",
                "output.pdf",
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("blah");   // todo - change 'blah' at the right time!!!
    }

    @Test
    void genDocumentWithNullSourceDataCausesNullPointerException() {
        DocumentGenerationService documentGenerationService = new DocumentGenerationService();
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> documentGenerationService.genPdfDocument(
                "EM-TRB-ET-00001.docx",
                "output.pdf",
                null));
        assertThat(exception.getMessage()).isEqualTo("blah");
    }

    @Test
    void genDocumentWithUnknownTemplateNameCausesDocumentGenerationException() {
        DocumentGenerationService documentGenerationService = new DocumentGenerationService();
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "a.docx",
                "output.pdf",
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("Unknown Template: a.docx");
    }

    @Test
    void genDocumentWithUnknownTemplateFileFormatCausesDocumentGenerationException() {
        DocumentGenerationService documentGenerationService = new DocumentGenerationService();
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "EM-TRB-ET-00001.xlsx",
                "output.pdf",
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("Unknown Template: EM-TRB-ET-00001.xlsx");
    }

    @Test
    void genDocumentWithBlankTemplateNameCausesDocumentGenerationException() {
        DocumentGenerationService documentGenerationService = new DocumentGenerationService();
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "",
                "output.pdf",
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("Unknown Template: ");
    }

    @Test
    void genDocumentWithBlankOutputFileNameCausesDocumentGenerationException() {
        DocumentGenerationService documentGenerationService = new DocumentGenerationService();
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "EM-TRB-ET-00001.docx",
                "",
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("Invalid output file name");
    }

    @Test
    void genDocumentWithUnknownOutputFileFormatCausesDocumentGenerationException() {
        DocumentGenerationService documentGenerationService = new DocumentGenerationService();
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "EM-TRB-ET-00001.docx",
                "output.xlsx",
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("Invalid output file name");
    }

    @Test
    void genDocumentWithNullTemplateNameCausesNullPointerException() {
        DocumentGenerationService documentGenerationService = new DocumentGenerationService();
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> documentGenerationService.genPdfDocument(
                null,
                "output.pdf",
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("blah"); // todo - populate once we've seen what really comes back
    }

    @Test
    void genDocumentWithNullOutputFileNameCausesNullPointerException() {
        DocumentGenerationService documentGenerationService = new DocumentGenerationService();
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> documentGenerationService.genPdfDocument(
                "EM-TRB-ET-00001.docx",
                null,
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("blah"); // todo - populate once we've seen what really comes back
    }

    @Test
    void genDocumentFailsToConnectToDocmosisCausesDocumentGenerationException() {
        DocumentGenerationService documentGenerationService = new DocumentGenerationService();
        DocumentGenerationException exception = assertThrows(
            DocumentGenerationException.class,
            () -> documentGenerationService.genPdfDocument(
                "EM-TRB-ET-00001.docx",
                "somefile.pdf",
                new ClaimCaseDocument()));
        assertThat(exception.getMessage()).isEqualTo("Failed to connect with Docmosis");
        assertThat(exception.getCause()).isInstanceOf(IOException.class);
        assertThat(exception.getCause().getMessage()).isEqualTo("blah"); // todo - populate once we've seen what really comes back
    }
}
