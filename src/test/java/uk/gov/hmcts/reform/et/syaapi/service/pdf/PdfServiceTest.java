package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.apache.tika.Tika;
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
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificate;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PdfServiceTest {
    private static final Map<String, Optional<String>> PDF_VALUES = Map.of(
        PdfMapperConstants.TRIBUNAL_OFFICE, Optional.of("Manchester"),
        PdfMapperConstants.CASE_NUMBER, Optional.of("001"),
        PdfMapperConstants.DATE_RECEIVED, Optional.of("21-07-2022")
    );
    private static final Map<String, Optional<String>> PDF_VALUES_WITH_NULL = Map.of(
        PdfMapperConstants.TRIBUNAL_OFFICE, Optional.of("Manchester"),
        PdfMapperConstants.CASE_NUMBER, Optional.of("001"),
        PdfMapperConstants.DATE_RECEIVED, Optional.of("")
    );

    private TestData testData;
    private static final String PDF_TEMPLATE_SOURCE_ATTRIBUTE_NAME = "pdfTemplateSource";
    private static final String PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE = "ET1_1122.pdf";
    private static final String PDF_FILE_TIKA_CONTENT_TYPE = "application/pdf";

    private final AcasCertificate acasCertificate = ResourceLoader.fromString(
        "requests/acasCertificate.json",
        AcasCertificate.class
    );

    @Mock
    private PdfMapperService pdfMapperService;
    @InjectMocks
    private PdfService pdfService;

    @BeforeEach
    void beforeEach() {
        testData = new TestData();
        ReflectionTestUtils.setField(pdfService,
                                     PDF_TEMPLATE_SOURCE_ATTRIBUTE_NAME,
                                     PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE);
    }

    @Test
    void givenPdfValuesProducesAPdfDocument() throws PdfServiceException, IOException {
        when(pdfMapperService.mapHeadersToPdf(testData.getCaseData())).thenReturn(PDF_VALUES);
        byte[] pdfBytes = pdfService.convertCaseToPdf(testData.getCaseData());
        try (PDDocument actualPdf = Loader.loadPDF(pdfBytes)) {
            Map<String, Optional<String>> actualPdfValues = processPdf(actualPdf);
            PDF_VALUES.forEach((k, v) -> assertThat(actualPdfValues).containsEntry(k, v));
        }
    }

    @Test
    void givenInvalidPdfTemplateProducesException() {
        ReflectionTestUtils.setField(pdfService,
                                     PDF_TEMPLATE_SOURCE_ATTRIBUTE_NAME,
                                     "dummy_source");
        assertThrows(
            NullPointerException.class,
            () -> pdfService.convertCaseToPdf(testData.getCaseData()));
        ReflectionTestUtils.setField(pdfService,
                                     PDF_TEMPLATE_SOURCE_ATTRIBUTE_NAME,
                                     PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE);
    }

    @Test
    void givenNullValuesProducesDocumentWithoutGivenValues() throws PdfServiceException, IOException {
        when(pdfMapperService.mapHeadersToPdf(testData.getCaseData())).thenReturn(PDF_VALUES_WITH_NULL);
        byte[] pdfBytes = pdfService.convertCaseToPdf(testData.getCaseData());
        try (PDDocument actualPdf = Loader.loadPDF(pdfBytes)) {
            Map<String, Optional<String>> actualPdfValues = processPdf(actualPdf);
            PDF_VALUES_WITH_NULL.forEach((k, v) -> assertThat(actualPdfValues).containsEntry(k, v));
        }
    }

    private Map<String, Optional<String>> processPdf(PDDocument pdDocument) {
        PDDocumentCatalog pdDocumentCatalog = pdDocument.getDocumentCatalog();
        PDAcroForm pdfForm = pdDocumentCatalog.getAcroForm();
        Map<String, Optional<String>> returnFields = new ConcurrentHashMap<>();
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

    @Test
    void shouldCreatePdfFile() throws IOException {
        PdfService pdfService1 = new PdfService(new PdfMapperService());
        pdfService1.pdfTemplateSource = PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE;
        byte[] pdfData = pdfService1.createPdf(testData.getCaseData());
        assertThat(pdfData).isNotEmpty();
        assertThat(new Tika().detect(pdfData)).isEqualTo(PDF_FILE_TIKA_CONTENT_TYPE);
    }

    @Test
    void shouldCreatePdfDecodedMultipartFileFromCaseData() throws PdfServiceException {
        PdfDecodedMultipartFile pdfDecodedMultipartFile =
            pdfService.convertCaseDataToPdfDecodedMultipartFile(testData.getCaseData(), null);
        assertThat(pdfDecodedMultipartFile).isNotNull();
    }

    @Test
    void shouldCreatePdfDecodedMultipartFileFromCaseDataWithNullFirstAndLastName() throws PdfServiceException {
        testData.getCaseData().getClaimantIndType().setClaimantFirstNames("");
        testData.getCaseData().getClaimantIndType().setClaimantLastName("");
        UserInfo userInfo = new UserInfo("", "", "", "GivenName",
                                         "FamilyName", new ArrayList<>());
        PdfDecodedMultipartFile pdfDecodedMultipartFile =
            pdfService.convertCaseDataToPdfDecodedMultipartFile(testData.getCaseData(), userInfo);

        assertThat(pdfDecodedMultipartFile).isNotNull();
    }

    @Test
    void shouldCreatePdfDecodedMultipartFileFromCaseDataAndAcasCertificate() {
        List<AcasCertificate> acasCertificates = new ArrayList<>();
        acasCertificates.add(acasCertificate);
        List<PdfDecodedMultipartFile> pdfDecodedMultipartFiles =
            pdfService.convertAcasCertificatesToPdfDecodedMultipartFiles(testData.getCaseData(), acasCertificates);
        assertThat(pdfDecodedMultipartFiles).hasSize(1);
    }
}
