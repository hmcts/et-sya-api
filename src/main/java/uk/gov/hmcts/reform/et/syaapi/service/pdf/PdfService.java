package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Uses {@link PdfMapperService} to convert a given case into a Pdf Document.
 */
@Service
@RequiredArgsConstructor()
public class PdfService {

    private final PdfMapperService pdfMapperService;
    @Value("${pdf.source}")
    public String pdfTemplateSource;

    private static final String PDF_FILE_TIKA_CONTENT_TYPE = "application/pdf";

    /**
     * Converts a {@link CaseData} class object into a pdf document
     * using template (ver. ET1_0922)
     * @param caseData      The data that is to be converted into pdf
     * @return              a byte array that contains the pdf document.
     */
    public byte[] convertCaseToPdf(CaseData caseData) throws PdfServiceException {
        byte[] pdfDocumentBytes;
        try {
            pdfDocumentBytes = createPdf(caseData);
        } catch (IOException ex) {
            throw new PdfServiceException("Failed to convert to PDF", ex);
        }
        return pdfDocumentBytes;
    }

    protected byte[] createPdf(CaseData caseData) throws IOException {
        try (PDDocument pdfDocument = Loader.loadPDF(ResourceUtils.getFile(this.pdfTemplateSource))) {
            PDDocumentCatalog pdDocumentCatalog = pdfDocument.getDocumentCatalog();
            PDAcroForm pdfForm = pdDocumentCatalog.getAcroForm();
            for (Map.Entry<String, Optional<String>> entry : this.pdfMapperService.mapHeadersToPdf(caseData)
                .entrySet()) {
                String entryKey = entry.getKey();
                Optional<String> entryValue = entry.getValue();
                if (entryValue.isPresent()) {
                    PDField pdfField = pdfForm.getField(entryKey);
                    pdfField.setValue(entryValue.get());
                }
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            pdfDocument.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    private static String createPdfDocumentNameFromCaseData(CaseData caseData) {
        return "ET1_"
            + caseData.getClaimantIndType().getClaimantFirstNames()
            + "_"
            + caseData.getClaimantIndType().getClaimantLastName()
            + ".pdf";
    }

    private static String createPdfDocumentNameFromCaseDataAndAcasCertificate(
        CaseData caseData, AcasCertificate acasCertificate) {
        return "ET1_"
            + caseData.getClaimantIndType().getClaimantFirstNames()
            + "_"
            + caseData.getClaimantIndType().getClaimantLastName()
            + "_"
            + acasCertificate.getCertificateNumber()
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
        return "Case Details - "
            + caseData.getClaimantIndType().getClaimantFirstNames()
            + " "
            + caseData.getClaimantIndType().getClaimantLastName()
            + " - "
            + acasCertificate.getCertificateNumber();
    }

    public PdfDecodedMultipartFile convertCaseDataToPdfDecodedMultipartFile(CaseData caseData)
        throws PdfServiceException {
        byte[] pdfData = convertCaseToPdf(caseData);
        return new PdfDecodedMultipartFile(pdfData,
                                           createPdfDocumentNameFromCaseData(caseData),
                                           PDF_FILE_TIKA_CONTENT_TYPE,
                                           createPdfDocumentDescriptionFromCaseData(caseData));
    }

    public PdfDecodedMultipartFile convertAcasCertificateToPdfDecodedMultipartFile(
        CaseData caseData, AcasCertificate acasCertificate) {
        byte[] pdfData = Base64.getDecoder().decode(acasCertificate.getCertificateDocument());
        return new PdfDecodedMultipartFile(pdfData,
                                           createPdfDocumentNameFromCaseDataAndAcasCertificate(caseData,
                                                                                               acasCertificate),
                                           PDF_FILE_TIKA_CONTENT_TYPE,
                                           createPdfDocumentDescriptionFromCaseDataAndAcasCertificate(caseData,
                                                                                                      acasCertificate));
    }
}
