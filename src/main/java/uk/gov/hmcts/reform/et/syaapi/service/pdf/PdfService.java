package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificate;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


/**
 * Uses {@link PdfMapperService} to convert a given case into a Pdf Document.
 */
@Slf4j
@Service
@RequiredArgsConstructor()
public class PdfService {

    private static final String ENGLISH = "English";
    private static final String WELSH = "Welsh";

    private final PdfMapperService pdfMapperService;
    @Value("${pdf.english}")
    public String englishPdfTemplateSource;
    @Value("${pdf.welsh}")
    public String welshPdfTemplateSource;

    private static final String PDF_FILE_TIKA_CONTENT_TYPE = "application/pdf";
    private static final String NOT_FOUND = "not found";

    /**
     * Converts a {@link CaseData} class object into a pdf document
     * using template (ver. ET1_1122)
     * @param caseData      The data that is to be converted into pdf
     * @param pdfSource     The source location of the PDF file to be used as the template
     * @return              a byte array that contains the pdf document.
     */
    public byte[] convertCaseToPdf(CaseData caseData, String pdfSource) throws PdfServiceException {
        byte[] pdfDocumentBytes;
        try {
            pdfDocumentBytes = createPdf(caseData, pdfSource);
        } catch (IOException ex) {
            throw new PdfServiceException("Failed to convert to PDF", ex);
        }
        return pdfDocumentBytes;
    }

    protected byte[] createPdf(CaseData caseData, String pdfSource) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try (PDDocument pdfDocument = Loader.loadPDF(
            Objects.requireNonNull(cl.getResourceAsStream(pdfSource)))) {
            PDDocumentCatalog pdDocumentCatalog = pdfDocument.getDocumentCatalog();
            PDAcroForm pdfForm = pdDocumentCatalog.getAcroForm();
            for (Map.Entry<String, Optional<String>> entry : this.pdfMapperService.mapHeadersToPdf(caseData)
                .entrySet()) {
                String entryKey = entry.getKey();
                Optional<String> entryValue = entry.getValue();
                if (entryValue.isPresent()) {
                    try {
                        PDField pdfField = pdfForm.getField(entryKey);
                        pdfField.setValue(entryValue.get());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            pdfDocument.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    private static String createPdfDocumentNameFromCaseData(CaseData caseData,
                                                            String documentLanguage,
                                                            UserInfo userInfo) {

        String claimantFirstName = caseData.getClaimantIndType().getClaimantFirstNames();
        String claimantLastName = caseData.getClaimantIndType().getClaimantLastName();
        if (Strings.isNullOrEmpty(claimantFirstName)) {
            claimantFirstName = userInfo.getGivenName();
        }
        if (Strings.isNullOrEmpty(claimantLastName)) {
            claimantLastName = userInfo.getFamilyName();
        }
        return "ET1_CASE_DOCUMENT_"
            + claimantFirstName.replace(" ", "_")
            + "_"
            + claimantLastName.replace(" ", "_")
            +  (ENGLISH.equals(documentLanguage) ? "" : "_" + documentLanguage)
            + ".pdf";
    }

    private static String createPdfDocumentNameFromCaseDataAndAcasCertificate(
        CaseData caseData, AcasCertificate acasCertificate) {
        return "ET1_ACAS_CERTIFICATE_"
            + caseData.getClaimantIndType().getClaimantFirstNames().replace(" ", "_")
            + "_"
            + caseData.getClaimantIndType().getClaimantLastName().replace(" ", "_")
            + "_"
            + acasCertificate.getCertificateNumber().replace("/", "_")
            + ".pdf";
    }

    private static String createPdfDocumentDescriptionFromCaseData(CaseData caseData) {
        return "Case Details - "
            + caseData.getClaimantIndType().getClaimantFirstNames()
            + " " + caseData.getClaimantIndType().getClaimantLastName();
    }

    private static String createPdfDocumentDescriptionFromCaseDataAndAcasCertificate(
        CaseData caseData,
        AcasCertificate acasCertificate) {
        return "ACAS Certificate - "
            + caseData.getClaimantIndType().getClaimantFirstNames()
            + " "
            + caseData.getClaimantIndType().getClaimantLastName()
            + " - "
            + acasCertificate.getCertificateNumber();
    }

    public List<PdfDecodedMultipartFile> convertCaseDataToPdfDecodedMultipartFile(CaseData caseData, UserInfo userInfo)
        throws PdfServiceException {
        List<PdfDecodedMultipartFile> files = new ArrayList<>();
        files.add(new PdfDecodedMultipartFile(convertCaseToPdf(caseData, this.englishPdfTemplateSource),
                                              createPdfDocumentNameFromCaseData(caseData, ENGLISH, userInfo),
                                              PDF_FILE_TIKA_CONTENT_TYPE,
                                              createPdfDocumentDescriptionFromCaseData(caseData)));

        if (WELSH.equals(caseData.getClaimantType().getClaimantContactLanguage())) {
            files.add(new PdfDecodedMultipartFile(convertCaseToPdf(caseData, this.welshPdfTemplateSource),
                                                  createPdfDocumentNameFromCaseData(caseData, WELSH, userInfo),
                                                  PDF_FILE_TIKA_CONTENT_TYPE,
                                                  createPdfDocumentDescriptionFromCaseData(caseData)));
        }

        return files;
    }

    public List<PdfDecodedMultipartFile> convertAcasCertificatesToPdfDecodedMultipartFiles(
        CaseData caseData, List<AcasCertificate> acasCertificates) {
        List<PdfDecodedMultipartFile> pdfDecodedMultipartFiles = new ArrayList<>();
        for (AcasCertificate acasCertificate : acasCertificates) {
            if (!NOT_FOUND.equals(acasCertificate.getCertificateDocument())) {
                byte[] pdfData = Base64.getDecoder().decode(acasCertificate.getCertificateDocument());
                pdfDecodedMultipartFiles.add(new PdfDecodedMultipartFile(
                    pdfData,
                    createPdfDocumentNameFromCaseDataAndAcasCertificate(
                        caseData,
                        acasCertificate
                    ),
                    PDF_FILE_TIKA_CONTENT_TYPE,
                    createPdfDocumentDescriptionFromCaseDataAndAcasCertificate(
                        caseData,
                        acasCertificate
                    )
                ));
            }
        }
        return pdfDecodedMultipartFiles;
    }
}
