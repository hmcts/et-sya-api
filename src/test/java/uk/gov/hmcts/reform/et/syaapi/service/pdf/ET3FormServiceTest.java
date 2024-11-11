package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ecm.common.service.pdf.PdfService;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.DocumentCategoryConstants.ET3_PDF_DOC_CATEGORY;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.ET3_FORM_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ET3FormServiceTest {

    private ET3FormService et3FormService;
    private CaseTestData caseTestData;

    @Mock
    private PdfService pdfService;
    @Mock
    private CaseDocumentService caseDocumentService;
    @Mock
    private IdamClient idamClient;

    @BeforeEach
    @SneakyThrows
    void beforeEach() {
        et3FormService = new ET3FormService(pdfService, caseDocumentService, idamClient);
        caseTestData = new CaseTestData();
    }


    @Test
    @SneakyThrows
    void theGenerateET3WelshAndEnglishForms() {
        CaseData caseData =  caseTestData.getCaseData();
        when(caseDocumentService.createDocumentTypeItem(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(caseData.getEcmCaseType()),
            eq(ET3_FORM_DOCUMENT_TYPE),
            eq(ET3_PDF_DOC_CATEGORY),
            any())).thenReturn(caseTestData.getUploadDocumentResponse().get(0));

        et3FormService.generateET3WelshAndEnglishForms(TEST_SERVICE_AUTH_TOKEN,
                                                       caseData,
                                                       caseData.getRespondentCollection().get(0));
        assertThat(caseData.getDocumentCollection()).contains(caseTestData.getUploadDocumentResponse().get(0));
    }

}
