package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = {ManageCaseDocumentController.class}
)
@Import(ManageCaseDocumentController.class)
public class ManageCaseDocumentControllerTest {

}
