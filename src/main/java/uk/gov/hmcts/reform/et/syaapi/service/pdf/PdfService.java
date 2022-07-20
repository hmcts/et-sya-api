package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

/*
 * Prints out provided case as a PDF.
 */
@Service
public class PdfService {

    private final PdfMapperService pdfMapperService;

    private final String pdfTemplateSource;

    public PdfService(PdfMapperService pdfMapperService,
                      @Value("${pdf.source}") String pdfTemplateSource) {
        this.pdfMapperService = pdfMapperService;
        this.pdfTemplateSource = pdfTemplateSource;
    }

    public byte[] convertCaseToPdf(CaseData caseData) {
        return "".getBytes();
    }

}
