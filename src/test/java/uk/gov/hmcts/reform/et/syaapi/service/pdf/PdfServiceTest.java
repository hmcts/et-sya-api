package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import lombok.SneakyThrows;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificate;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.models.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.et.syaapi.service.utils.GenericServiceUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({"PMD.CloseResource"})
class PdfServiceTest {
    private static final Map<String, Optional<String>> PDF_VALUES = Map.of(
        PdfMapperConstants.TRIBUNAL_OFFICE, Optional.of("Manchester"),
        PdfMapperConstants.CASE_NUMBER, Optional.of("001"),
        PdfMapperConstants.DATE_RECEIVED, Optional.of("21-07-2022")
    );

    private CaseTestData caseTestData;

    private static final String PDF_TEMPLATE_SOURCE_ATTRIBUTE_NAME = "englishPdfTemplateSource";
    private static final String PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_ENGLISH = "ET1_1122.pdf";
    private static final String PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_ENGLISH_INVALID = "ET1_0722.pdf";
    private static final String PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_ENGLISH_NOT_EXISTS = "invalid_english.pdf";
    private static final String PDF_TEMPLATE_SOURCE_ATTRIBUTE_NAME_WELSH = "welshPdfTemplateSource";
    private static final String PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_WELSH = "CY_ET1_2222.pdf";
    private static final String PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_WELSH_INVALID = "CY_ET1_0922.pdf";
    private static final String PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_WELSH_NOT_EXISTS = "invalid_welsh.pdf";
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
        caseTestData = new CaseTestData();
        ReflectionTestUtils.setField(
            pdfService,
            PDF_TEMPLATE_SOURCE_ATTRIBUTE_NAME,
            PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_ENGLISH
        );
        ReflectionTestUtils.setField(
            pdfService,
            PDF_TEMPLATE_SOURCE_ATTRIBUTE_NAME_WELSH,
            PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_WELSH
        );
    }

    @SneakyThrows
    @Test
    void givenPdfValuesProducesAPdfDocument() {
        when(pdfMapperService.mapHeadersToPdf(caseTestData.getCaseData())).thenReturn(PDF_VALUES);
        byte[] pdfBytes = pdfService.convertCaseToPdf(
            caseTestData.getCaseData(),
            PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_ENGLISH
        );
        try (PDDocument actualPdf = Loader.loadPDF(pdfBytes)) {
            Map<String, Optional<String>> actualPdfValues = processPdf(actualPdf);
            PDF_VALUES.forEach((k, v) -> assertThat(actualPdfValues).containsEntry(k, v));
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

    @SneakyThrows
    @ParameterizedTest
    @CsvSource({
        ENGLISH_LANGUAGE + "," + PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_ENGLISH,
        ENGLISH_LANGUAGE + "," + PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_ENGLISH_INVALID,
        ENGLISH_LANGUAGE + "," + PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_ENGLISH_NOT_EXISTS,
        ENGLISH_LANGUAGE + ",",
        WELSH_LANGUAGE + "," + PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_WELSH,
        WELSH_LANGUAGE + "," + PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_WELSH_INVALID,
        WELSH_LANGUAGE + "," + PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_WELSH_NOT_EXISTS,
        WELSH_LANGUAGE + ","
    })
    void shouldCreatePdfFileAccordingToSelectedLanguageAndTemplateSource(String language, String templateSource) {

        try (MockedStatic<GenericServiceUtil> mockedServiceUtil = Mockito.mockStatic(GenericServiceUtil.class)) {
            if (ENGLISH_LANGUAGE.equals(language)) {
                mockedServiceUtil.when(() -> GenericServiceUtil.findClaimantLanguage(caseTestData.getCaseData()))
                    .thenReturn(ENGLISH_LANGUAGE);
            }
            if (WELSH_LANGUAGE.equals(language)) {
                mockedServiceUtil.when(() -> GenericServiceUtil.findClaimantLanguage(caseTestData.getCaseData()))
                    .thenReturn(WELSH_LANGUAGE);
            }
            PdfService pdfService1 = new PdfService(new PdfMapperService());
            byte[] pdfData = pdfService1.createPdf(caseTestData.getCaseData(), templateSource);
            if (PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_ENGLISH.equals(templateSource)
                || PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_WELSH.equals(templateSource)) {
                assertThat(pdfData).isNotEmpty();
                assertThat(new Tika().detect(pdfData)).isEqualTo(PDF_FILE_TIKA_CONTENT_TYPE);
            }
            if (templateSource == null || templateSource.contains("invalid")) {
                assertThat(pdfData).isEmpty();
            }
            if (PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_ENGLISH_INVALID.equals(templateSource)
                || PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_WELSH_INVALID.equals(templateSource)) {
                mockedServiceUtil.verify(
                    () -> GenericServiceUtil.logException(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()
                    ),
                    atLeast(1)
                );
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"userInfoExists", "userInfoNotExists"})
    void shouldCreatePdfDecodedMultipartFileListFromCaseDataAccordingToUserInfo(String userInfoFlag) {
        List<PdfDecodedMultipartFile> pdfDecodedMultipartFileList;
        if ("userInfoExists".equals(userInfoFlag)) {
            pdfDecodedMultipartFileList =
                pdfService.convertCaseDataToPdfDecodedMultipartFile(caseTestData.getCaseData(),
                                                                    caseTestData.getUserInfo());
        } else {
            pdfDecodedMultipartFileList =
                pdfService.convertCaseDataToPdfDecodedMultipartFile(caseTestData.getCaseData(), null);
        }
        assertThat(pdfDecodedMultipartFileList).hasSize(1);
    }

    @Test
    void shouldNotCreatePdfDecodedMultipartFileFromCaseDataWhenBothWelshAndEnglishTemplateSourcesNotExist() {
        caseTestData.getCaseData().getClaimantHearingPreference().setContactLanguage(WELSH_LANGUAGE);
        PdfService pdfService1 = new PdfService(new PdfMapperService());
        pdfService1.welshPdfTemplateSource = PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_WELSH_NOT_EXISTS;
        pdfService1.englishPdfTemplateSource = PDF_TEMPLATE_SOURCE_ATTRIBUTE_VALUE_WELSH_NOT_EXISTS;
        try (MockedStatic<GenericServiceUtil> mockedServiceUtil = Mockito.mockStatic(GenericServiceUtil.class)) {
            mockedServiceUtil.when(() -> GenericServiceUtil.findClaimantLanguage(caseTestData.getCaseData()))
                .thenReturn(WELSH_LANGUAGE);
            List<PdfDecodedMultipartFile> pdfDecodedMultipartFileList =
                pdfService1.convertCaseDataToPdfDecodedMultipartFile(caseTestData.getCaseData(), null);
            assertThat(pdfDecodedMultipartFileList).isEmpty();
            mockedServiceUtil.verify(
                () -> GenericServiceUtil.logException(anyString(), anyString(), anyString(), anyString(), anyString()),
                times(2)
            );
        }
    }

    @Test
    void shouldCreatePdfDecodedMultipartFileFromCaseDataAndAcasCertificate() {
        List<AcasCertificate> acasCertificates = new ArrayList<>();
        acasCertificates.add(acasCertificate);
        List<PdfDecodedMultipartFile> pdfDecodedMultipartFiles =
            pdfService.convertAcasCertificatesToPdfDecodedMultipartFiles(caseTestData.getCaseData(), acasCertificates);
        assertThat(pdfDecodedMultipartFiles).hasSize(1);
    }

    @Test
    void shouldNotCreateWhenCertificateDocumentNotFound() {
        List<AcasCertificate> acasCertificates = new ArrayList<>();
        AcasCertificate acasCert = new AcasCertificate();
        acasCert.setCertificateDocument("not found");
        acasCertificates.add(acasCert);
        List<PdfDecodedMultipartFile> pdfDecodedMultipartFiles =
            pdfService.convertAcasCertificatesToPdfDecodedMultipartFiles(caseTestData.getCaseData(), acasCertificates);
        assertThat(pdfDecodedMultipartFiles).isEmpty();
    }

    @SneakyThrows
    @Test
    void shouldThrowExceptionWhenInputStreamNotClosed() {
        try (MockedStatic<GenericServiceUtil> mockedServiceUtil = Mockito.mockStatic(GenericServiceUtil.class)) {
            InputStream is = Mockito.mock(InputStream.class);
            doThrow(new IOException("Test IOException")).when(is).close();
            PdfService.safeClose(is, caseTestData.getCaseData());
            mockedServiceUtil.verify(
                () -> GenericServiceUtil.logException(anyString(), anyString(), anyString(), anyString(), anyString()),
                times(1)
            );
        }
    }
}
