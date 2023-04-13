package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificate;
import uk.gov.hmcts.reform.et.syaapi.service.util.ServiceUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;

/**
 * Uses {@link PdfMapperService} to convert a given case into a Pdf Document.
 */
@Slf4j
@Service
@RequiredArgsConstructor()
public class PdfService {

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
     *
     * @param caseData  The data that is to be converted into pdf
     * @param pdfSource The source location of the PDF file to be used as the template
     * @return A byte array that contains the pdf document.
     */
    public byte[] convertCaseToPdf(CaseData caseData, String pdfSource) throws PdfServiceException {
        byte[] pdfDocumentBytes;
        try {
            pdfDocumentBytes = createPdf(caseData, pdfSource);
        } catch (IOException ioe) {
            throw new PdfServiceException("Failed to convert to PDF", ioe);
        }
        return pdfDocumentBytes;
    }

    /**
     * Populates a pdf document with data stored in the case data parameter.
     *
     * @param caseData  {@link CaseData} object with information in which to populate the pdf with
     * @param pdfSource file name of the pdf template used to create the pdf
     * @return a byte array of the generated pdf file.
     * @throws IOException if there is an issue reading the pdf template
     */
    public byte[] createPdf(CaseData caseData, String pdfSource) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream stream = ObjectUtils.isEmpty(cl) || StringUtils.isBlank(pdfSource) ? null
            : cl.getResourceAsStream(pdfSource);
        if (!ObjectUtils.isEmpty(stream)) {
            try (PDDocument pdfDocument = Loader.loadPDF(
                Objects.requireNonNull(stream))) {
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
                            ServiceUtil.logException("Error while parsing PDF file for entry key \""
                                                         + entryKey
                                                         + "\", entry value \""
                                                         + getEntryValueFromOptionalString(entryValue)
                                                         + "\"", caseData.getEthosCaseReference(), e.getMessage(),
                                                            this.getClass().getName(), "createPdf");
                        }
                    }
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                pdfDocument.save(byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            } finally {
                safeClose(stream, caseData);
            }
        }
        safeClose(stream, caseData);
        return new byte[0];
    }

    private String getEntryValueFromOptionalString(Optional<String> entryValue) {
        return ObjectUtils.isEmpty(entryValue) || entryValue.isEmpty() ? "" : entryValue.get();
    }

    public static void safeClose(InputStream is, CaseData caseData) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                ServiceUtil.logException("Input stream for the template PDF file was not closed: ",
                                         caseData.getEthosCaseReference(), e.getMessage(),
                                         "PDFServiceUtil", "safeClose");
            }
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
            + (ENGLISH_LANGUAGE.equals(documentLanguage) ? "" : "_" + documentLanguage)
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


    /**
     * Converts case data to a pdf byte array wrapped in a {@link PdfDecodedMultipartFile} Object.
     *
     * @param caseData The case data to be converted into a pdf file wrapped in a {@link CaseData}
     * @param userInfo a {@link UserInfo} used username as a backup if no name in case
     * @return a list of {@link PdfDecodedMultipartFile} which contains the pdf values
     */
    public List<PdfDecodedMultipartFile> convertCaseDataToPdfDecodedMultipartFile(CaseData caseData, UserInfo
        userInfo) {
        List<PdfDecodedMultipartFile> files = new ArrayList<>();
        try {
            byte[] pdfData = convertCaseToPdf(caseData, this.englishPdfTemplateSource);
            if (ObjectUtils.isEmpty(pdfData)) {
                throw new PdfServiceException(
                    "Failed to convert to PDF. English Template Not Found",
                    new NullPointerException()
                );
            }
            files.add(new PdfDecodedMultipartFile(
                pdfData,
                createPdfDocumentNameFromCaseData(caseData, ENGLISH_LANGUAGE, userInfo),
                PDF_FILE_TIKA_CONTENT_TYPE,
                createPdfDocumentDescriptionFromCaseData(caseData)
            ));
        } catch (PdfServiceException e) {
            ServiceUtil.logException("Case English PDF file could not be created for case: ",
                                     caseData.getEthosCaseReference(), e.getMessage(),
                                     this.getClass().getName(), "convertCaseDataToPdfDecodedMultipartFile");
        }
        try {
            if (WELSH_LANGUAGE.equals(ServiceUtil.findClaimantLanguage(caseData))) {
                byte[] pdfData = convertCaseToPdf(caseData, this.welshPdfTemplateSource);
                if (ObjectUtils.isEmpty(pdfData)) {
                    throw new PdfServiceException("Failed to convert to PDF. Welsh Template Not Found",
                                                  new NullPointerException());
                }
                files.add(new PdfDecodedMultipartFile(
                    pdfData,
                    createPdfDocumentNameFromCaseData(caseData, WELSH_LANGUAGE, userInfo),
                    PDF_FILE_TIKA_CONTENT_TYPE,
                    createPdfDocumentDescriptionFromCaseData(caseData)
                ));
            }
        } catch (PdfServiceException e) {
            ServiceUtil.logException("Case Welsh PDF file could not be created for case: ",
                                        caseData.getEthosCaseReference(), e.getMessage(),
                                        this.getClass().getName(), "convertCaseDataToPdfDecodedMultipartFile");
        }
        return files;
    }

    /**
     * Converts a list of {@link AcasCertificate} into a list of pdf files.
     *
     * @param caseData         case data wrapped in {@link CaseData} used when creating pdf name
     * @param acasCertificates certificates as a {@link AcasCertificate} to be converted into pdf files
     * @return a list of pdf files wrapped in {@link PdfDecodedMultipartFile}
     */
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
