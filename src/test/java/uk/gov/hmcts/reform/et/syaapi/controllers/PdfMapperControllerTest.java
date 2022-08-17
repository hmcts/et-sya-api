package uk.gov.hmcts.reform.et.syaapi.controllers;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfService;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfServiceException;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

@WebMvcTest(
    controllers = {PdfMapperController.class}
)
@Import(PdfMapperController.class)
public class PdfMapperControllerTest {

    private static final String CASE_ID = "1646225213651590";

    private static final Map<String, Object> CASE_DATA = Map.of();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PdfService pdfService;

    @MockBean
    private VerifyTokenService verifyTokenService;

    PdfMapperControllerTest() {

    }

    @SneakyThrows
    @Test
    void givenCallWithCaseProducesPdfDocument() {
        CaseRequest caseRequest = CaseRequest.builder()
            .caseId(CASE_ID).caseData(CASE_DATA).build();

        when(pdfService.convertCaseToPdf(new CaseData())).thenReturn("".getBytes());
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);

        mockMvc.perform(post("/cases/convert-to-pdf")
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ResourceLoader.toJson(caseRequest)))
            .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void givenPdfServiceExpectionProducesServerError() {
        CaseRequest caseRequest = CaseRequest.builder()
            .caseId(CASE_ID).caseData(CASE_DATA).build();

        when(pdfService.convertCaseToPdf(new CaseData())).thenThrow(PdfServiceException.class);
        when(verifyTokenService.verifyTokenSignature(any())).thenReturn(true);

        mockMvc.perform(post("/cases/convert-to-pdf")
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ResourceLoader.toJson(caseRequest)))
            .andExpect(status().isInternalServerError());
    }

//
//    @SneakyThrows
//    @Test
//    void givenEmptyCaseProduces() {
//
//    }
}
