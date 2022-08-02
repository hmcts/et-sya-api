package uk.gov.hmcts.reform.et.syaapi.service.pdf;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.elasticsearch.core.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

@SuppressWarnings({"PMD.TooManyMethods"})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PdfServiceTest {

    private static final Map<String, Optional<String>> PDF_VALUES = Map.of(
        PdfMapperConstants.TRIBUNAL_OFFICE, Optional.of("Manchester"),
        PdfMapperConstants.CASE_NUMBER, Optional.of("001"),
        PdfMapperConstants.DATE_RECEIVED, Optional.of("21-07-2022"),
        PdfMapperConstants.Q1_FIRST_NAME, Optional.of("TEST NAME"),
        PdfMapperConstants.Q1_SURNAME, Optional.of("TEST SURNAME")
    );
    private static final Map<String, Optional<String>> PDF_VALUES_WITH_NULL = Map.of(
        PdfMapperConstants.TRIBUNAL_OFFICE, Optional.of("Manchester"),
        PdfMapperConstants.CASE_NUMBER, Optional.of("001"),
        PdfMapperConstants.DATE_RECEIVED, Optional.empty(),
        PdfMapperConstants.Q1_FIRST_NAME, Optional.of("TEST NAME"),
        PdfMapperConstants.Q1_SURNAME, Optional.empty()
    );
    @Mock
    private CaseData caseData;

    @Mock
    private PdfMapperService pdfMapperService;

    @InjectMocks
    private PdfService pdfService;

    @Test
    void givenPdfValuesProducesAPdfDocument() throws PdfServiceException, IOException {
        ReflectionTestUtils.setField(pdfService, "pdfTemplateSource", "classpath:ET1_0722_mod.pdf");
        when(pdfMapperService.mapHeadersToPdf(caseData)).thenReturn(PDF_VALUES);
        byte[] pdfBytes = pdfService.convertCaseToPdf(caseData);

        try(PDDocument actualPdf = Loader.loadPDF(pdfBytes)) {
            Map<String, Optional<String>> actualPdfValues = processPdf(actualPdf);

            assertThat(actualPdfValues).containsOnlyKeys(PDF_VALUES.keySet());
        }
    }

    @Test
    void givenNoPdfTemplateProducesException() {
        ReflectionTestUtils.setField(pdfService, "pdfTemplateSource", "classpath:none.pdf");

        PdfServiceException exception = assertThrows(
            PdfServiceException.class,
            () -> pdfService.convertCaseToPdf(caseData));

        assertThat(exception.getMessage()).isEqualTo("Failed to convert to PDF");
    }

    @Test
    void givenNullValuesProducesDocumentWithoutGivenValues() throws PdfServiceException, IOException {
        ReflectionTestUtils.setField(pdfService, "pdfTemplateSource", "classpath:ET1_0722_mod.pdf");
        when(pdfMapperService.mapHeadersToPdf(caseData)).thenReturn(PDF_VALUES_WITH_NULL);
        byte[] pdfBytes = pdfService.convertCaseToPdf(caseData);

        try(PDDocument actualPdf = Loader.loadPDF(pdfBytes)) {
            Map<String, Optional<String>> actualPdfValues = processPdf(actualPdf);

            assertThat(actualPdfValues).doesNotContainKey(PdfMapperConstants.Q1_SURNAME);
            assertThat(actualPdfValues).doesNotContainKey(PdfMapperConstants.DATE_RECEIVED);
        }
    }

    private Map<String, Optional<String>> processPdf(PDDocument pdDocument) {
        PDDocumentCatalog pdDocumentCatalog = pdDocument.getDocumentCatalog();
        PDAcroForm pdfForm = pdDocumentCatalog.getAcroForm();
        Map<String, Optional<String>> returnFields = new HashMap<>();
        pdfForm.getFields().forEach(
            field -> {
                Tuple<String, String> fieldTuple = processField(field);
                returnFields.put(fieldTuple.v1(), Optional.ofNullable(fieldTuple.v2()));
            }
        );
        return returnFields;
    }

    private Tuple<String, String> processField(PDField field) {

        if (field instanceof PDNonTerminalField) {
            for (PDField child : ((PDNonTerminalField) field).getChildren()) {
                processField(child);
            }
        }

        return new Tuple<>(field.getFullyQualifiedName(), field.getValueAsString());
    }
}
