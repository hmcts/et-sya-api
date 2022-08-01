package uk.gov.hmcts.reform.et.syaapi.service.pdf;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.elasticsearch.core.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

@SuppressWarnings({"PMD.TooManyMethods"})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PdfServiceTest {

    private static final Map<String, Optional<String>> PDF_VALUES = Map.of(
        PdfMapperConstants.TRIBUNAL_OFFICE, ofNullable("Manchester"),
        PdfMapperConstants.CASE_NUMBER, ofNullable("001"),
        PdfMapperConstants.DATE_RECEIVED, ofNullable("21-07-2022"),
        PdfMapperConstants.Q1_FIRST_NAME, ofNullable("TEST NAME"),
        PdfMapperConstants.Q1_SURNAME, ofNullable("TEST SURNAME")
    );

    @Mock
    private CaseData caseData;

    @Mock
    private PdfMapperService pdfMapperService;

    @InjectMocks
    private PdfService pdfService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(pdfService, "pdfTemplateSource", "classpath:ET1_0722_mod.pdf");
    }

    @Test
    void givenPdfValuesProducesAPdfDocument() throws PdfServiceException {
        when(pdfMapperService.mapHeadersToPdf(caseData)).thenReturn(PDF_VALUES);
        byte[] pdfBytes = pdfService.convertCaseToPdf(caseData);

        assertThat(pdfBytes).isNotNull();
    }

    @Test
    void givenPdf() throws PdfServiceException, FileNotFoundException {
        when(pdfMapperService.mapHeadersToPdf(caseData)).thenReturn(PDF_VALUES);
        byte[] pdfBytes = pdfService.convertCaseToPdf(caseData);

        try (PDDocument templatePdf = Loader.loadPDF(ResourceUtils.getFile("classpath:ET1_0722_mod.pdf"))) {
            Map<String, Optional<String>> templatePdfValues = processPdf(templatePdf);

            try(PDDocument actualPdf = Loader.loadPDF(pdfBytes)) {
                Map<String, Optional<String>> actualPdfValues = processPdf(actualPdf);

                for(String label : actualPdfValues.keySet()) {
                    assertThat(templatePdfValues.containsKey(label)).isTrue();
                    assertThat(templatePdfValues.get(label)).isEqualTo(actualPdfValues.get(label));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Optional<String>> processPdf(PDDocument pdDocument) throws IOException {
        PDDocumentCatalog pdDocumentCatalog = pdDocument.getDocumentCatalog();
        PDAcroForm pdfForm = pdDocumentCatalog.getAcroForm();
        Map<String, Optional<String>> returnFields = new HashMap<>();
        pdfForm.getFields().forEach(
            field -> {
                Tuple fieldTuple = processField(field);
                returnFields.put(fieldTuple.v1().toString(), Optional.ofNullable(fieldTuple.v2().toString()));
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
