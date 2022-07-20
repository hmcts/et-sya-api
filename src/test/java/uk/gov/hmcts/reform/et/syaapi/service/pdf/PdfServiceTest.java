package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

@SuppressWarnings({"PMD.TooManyMethods"})
public class PdfServiceTest {

    private PdfService pdfService;

    private CaseData caseData = new CaseData();

    @Mock
    private PdfMapperService pdfMapperService;

    @BeforeEach
    void setup() {


        String pdfSourceUrl = "classpath:ET1_0722.pdf";

        pdfService = new PdfService(pdfMapperService, pdfSourceUrl);

    }

    // TODO - create MOCK return values
    // TODO - test that PDF is created
    // TODO - Add a helper to read the pdf variables
    // TODO - use reader to check that attributes match
}
