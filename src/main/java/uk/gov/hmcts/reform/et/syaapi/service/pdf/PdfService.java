package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificate;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantResponseCya;
import uk.gov.hmcts.reform.et.syaapi.models.GenericTseApplication;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.DocumentGenerationException;
import uk.gov.hmcts.reform.et.syaapi.service.DocumentGenerationService;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ClaimantTseUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.GenericServiceUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.APP_TYPE_MAP;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.HELVETICA_PDFBOX_CHARACTER_CODE_1;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.HELVETICA_PDFBOX_CHARACTER_CODE_2;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.TIMES_NEW_ROMAN_PDFBOX_CHARACTER_CODE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;

/**
 * Uses {@link PdfMapperService} to convert a given case into a Pdf Document.
 */
@Slf4j
@Service
@RequiredArgsConstructor()
@SuppressWarnings({"PMD.TooManyMethods"})
public class PdfService {

    private final PdfMapperService pdfMapperService;
    private final DocumentGenerationService documentGenerationService;
    @Value("${pdf.english}")
    public String englishPdfTemplateSource;
    @Value("${pdf.welsh}")
    public String welshPdfTemplateSource;
    @Value("${pdf.contact_tribunal_template}")
    public String contactTheTribunalPdfTemplate;
    @Value("${pdf.claimant_response_template}")
    public String claimantResponsePdfTemplate;

    private static final String TSE_FILENAME = "Contact the tribunal - ";
    private static final String CLAIMANT_TITLE = "Claimant";
    private static final String CLAIMANT_RESPONSE = "ClaimantResponse.pdf";
    private static final String PDF_FILE_TIKA_CONTENT_TYPE = "application/pdf";
    private static final String NOT_FOUND = "not found";
    private static final List<String> DOCUMENT_CHARS_TO_REPLACE = List.of("@", "/", "\\", "'");


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
                PDResources resources = new PDResources();
                resources.put(COSName.getPDFName(TIMES_NEW_ROMAN_PDFBOX_CHARACTER_CODE), PDType1Font.TIMES_ROMAN);
                resources.put(COSName.getPDFName(HELVETICA_PDFBOX_CHARACTER_CODE_1), PDType1Font.HELVETICA);
                resources.put(COSName.getPDFName(HELVETICA_PDFBOX_CHARACTER_CODE_2), PDType1Font.HELVETICA);
                PDDocumentCatalog pdDocumentCatalog = pdfDocument.getDocumentCatalog();
                PDAcroForm pdfForm = pdDocumentCatalog.getAcroForm();
                pdfForm.setDefaultResources(resources);
                for (Map.Entry<String, Optional<String>> entry : this.pdfMapperService.mapHeadersToPdf(caseData)
                    .entrySet()) {
                    String entryKey = entry.getKey();
                    Optional<String> entryValue = entry.getValue();
                    if (entryValue.isPresent()) {
                        try {
                            PDField pdfField = pdfForm.getField(entryKey);
                            pdfField.setValue(entryValue.get());
                        } catch (Exception e) {
                            GenericServiceUtil.logException("Error while parsing PDF file for entry key \""
                                                         + entryKey, caseData.getEthosCaseReference(), e.getMessage(),
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

    public static void safeClose(InputStream is, CaseData caseData) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                GenericServiceUtil.logException("Input stream for the template PDF file was not closed: ",
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
        if (isNullOrEmpty(claimantFirstName)) {
            claimantFirstName = userInfo.getGivenName();
        }
        if (isNullOrEmpty(claimantLastName)) {
            claimantLastName = userInfo.getFamilyName();
        }
        return "ET1 - "
            + sanitizePartyName(claimantFirstName)
            + " "
            + sanitizePartyName(claimantLastName)
            + (ENGLISH_LANGUAGE.equals(documentLanguage) ? "" : " " + documentLanguage)
            + ".pdf";
    }

    private static String createPdfDocumentNameFromCaseDataAndAcasCertificate(CaseData caseData,
                                                                              AcasCertificate acasCertificate) {
        Optional<RespondentSumTypeItem> respondent = caseData.getRespondentCollection().stream()
            .filter(r -> acasCertificate.getCertificateNumber().equals(
                defaultIfEmpty(r.getValue().getRespondentAcas(), "")))
            .findFirst();
        String acasName = "";
        if (respondent.isPresent()) {
            acasName = sanitizePartyName(respondent.get().getValue().getRespondentName()) + " - ";
        }

        return "ACAS Certificate - "
            + acasName
            + acasCertificate.getCertificateNumber().replace("/", "_");
    }

    private static String createPdfDocumentDescriptionFromCaseData(CaseData caseData) {
        return "ET1 - "
            + caseData.getClaimantIndType().getClaimantFirstNames()
            + " " + caseData.getClaimantIndType().getClaimantLastName();
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
            GenericServiceUtil.logException("Case English PDF file could not be created for case: ",
                                            caseData.getEthosCaseReference(), e.getMessage(),
                                            this.getClass().getName(), "convertCaseDataToPdfDecodedMultipartFile");
        }
        try {
            if (WELSH_LANGUAGE.equals(GenericServiceUtil.findClaimantLanguage(caseData))) {
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
            GenericServiceUtil.logException("Case Welsh PDF file could not be created for case: ",
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
                String docName = createPdfDocumentNameFromCaseDataAndAcasCertificate(
                    caseData,
                    acasCertificate
                );
                pdfDecodedMultipartFiles.add(new PdfDecodedMultipartFile(
                    pdfData,
                    docName + ".pdf",
                    PDF_FILE_TIKA_CONTENT_TYPE,
                    docName
                ));
            }
        }
        return pdfDecodedMultipartFiles;
    }

    /**
     * Converts a given object of type {@link ClaimantTse} to a {@link PdfDecodedMultipartFile}.
     * Firstly by converting to a pdf byte array and then wrapping within the return object.
     *
     * @param claimantTse {@link CaseData} object that contains the {@link ClaimantTse} object to be converted.
     * @return {@link PdfDecodedMultipartFile} with the claimant tse CYA page in pdf format.
     * @throws DocumentGenerationException if there is an error generating the PDF.
     */
    public PdfDecodedMultipartFile convertClaimantTseIntoMultipartFile(
        ClaimantTse claimantTse,
        String caseReference,
        String docName)

        throws DocumentGenerationException {

        GenericTseApplication genericTseApplication =
            ClaimantTseUtil.getGenericTseApplicationFromClaimantTse(claimantTse, caseReference);

        byte[] tseApplicationPdf = documentGenerationService.genPdfDocument(
            contactTheTribunalPdfTemplate,
            docName,
            genericTseApplication
        );

        return new PdfDecodedMultipartFile(
            tseApplicationPdf,
            docName,
            PDF_FILE_TIKA_CONTENT_TYPE,
            TSE_FILENAME + APP_TYPE_MAP.get(claimantTse.getContactApplicationType())
        );
    }

    /**
     * Converts a given object of type {@link RespondToApplicationRequest} to a {@link PdfDecodedMultipartFile}.
     * Firstly by converting to a pdf byte array and then wrapping within the return object.
     *
     * @param request {@link RespondToApplicationRequest} object that contains the data to be converted
     * @return {@link PdfDecodedMultipartFile} with the claimant response CYA page in pdf format.
     * @throws DocumentGenerationException if there is an error generating the PDF.
     */
    public PdfDecodedMultipartFile convertClaimantResponseIntoMultipartFile(RespondToApplicationRequest request,
                                                                            String description,
                                                                            String ethosCaseReference,
                                                                            GenericTseApplicationType application)
        throws DocumentGenerationException {

        String documentName = "Application %s  - %s - Claimant Response.pdf".formatted(
            application.getNumber(),
            application.getType()
        );

        return new PdfDecodedMultipartFile(
            convertClaimantResponseToPdf(request, ethosCaseReference, description, documentName),
            documentName,
            PDF_FILE_TIKA_CONTENT_TYPE,
            description
        );
    }

    private byte[] convertClaimantResponseToPdf(RespondToApplicationRequest request, String ethosCaseReference,
                                                String appTypeDescription, String documentName)
        throws DocumentGenerationException {
        TseRespondType claimantResponse = request.getResponse();
        String fileName = claimantResponse.getSupportingMaterial() != null
            ? request.getSupportingMaterialFile().getDocumentFilename() : null;

        ClaimantResponseCya claimantResponseCya = ClaimantResponseCya.builder()
            .caseNumber(ethosCaseReference)
            .applicant(CLAIMANT_TITLE)
            .applicationType(appTypeDescription)
            .applicationDate(UtilHelper.formatCurrentDate(LocalDate.now()))
            .response(claimantResponse.getResponse())
            .fileName(fileName)
            .copyToOtherPartyYesOrNo(claimantResponse.getCopyToOtherParty())
            .build();

        return documentGenerationService.genPdfDocument(
            claimantResponsePdfTemplate,
            documentName,
            claimantResponseCya
        );
    }

    private static String sanitizePartyName(String partyName) {
        if (isNullOrEmpty(partyName)) {
            return "";
        }

        String sanitizedName = partyName;
        for (String charToReplace : DOCUMENT_CHARS_TO_REPLACE) {
            sanitizedName = sanitizedName.replace(charToReplace, " ");
        }
        return sanitizedName;
    }
}
