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

/*
 * Prints out provided case as a PDF.
 */
@Service
@RequiredArgsConstructor()
public class PdfService {

    private final PdfMapperService pdfMapperService;
    @Value("${pdf.source}")
    private String pdfTemplateSource;

    public byte[] convertCaseToPdf(CaseData caseData) {
        byte[] generatedPDF = {};
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
            ex.printStackTrace();
        }
        return generatedPDF;
    }
}
