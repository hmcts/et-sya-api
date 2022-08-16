package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfService;

@WebMvcTest(
    controllers = {PdfMapperController.class}
)
public class PdfMapperControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PdfService pdfService;

    PdfMapperControllerTest() {

    }

    @Test
    void givenCallWithCaseProduces() {

    }
}
