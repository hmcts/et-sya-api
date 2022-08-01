package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

/**
 * Uses {@link PdfMapperService} to convert a given case into a Pdf Document.
 */
@Service
@RequiredArgsConstructor()
public class PdfService {

    private final PdfMapperService pdfMapperService;
    @Value("${pdf.source}")
    private String pdfTemplateSource;

    /**
     * Converts a {@link CaseData} class object into a pdf document
     * using template (ver. ET1_0722)
     * @param caseData      The data that is to be converted into pdf
     * @return              a byte array that contains the pdf document.
     */
    public byte[] convertCaseToPdf(CaseData caseData) throws PdfServiceException {
        try (PDDocument pdfDocument = Loader.loadPDF(ResourceUtils.getFile(this.pdfTemplateSource))) {
            PDDocumentCatalog pdDocumentCatalog = pdfDocument.getDocumentCatalog();
            PDAcroForm pdfForm = pdDocumentCatalog.getAcroForm();
            this.pdfMapperService.mapHeadersToPdf(caseData).forEach((k, v) -> {
                PDField pdfField = pdfForm.getField(k);
                try {
                    pdfField.setValue(v.get());
                } catch (Exception e) {
                    //continue if field mapping error
                    e.printStackTrace();
                    return;
                }
            });
            pdfForm.flatten();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            pdfDocument.save(byteArrayOutputStream);
            pdfDocument.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ex) {
            throw new PdfServiceException("", ex);
        }
    }
}
