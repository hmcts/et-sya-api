package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimCaseDocument;
import uk.gov.hmcts.reform.et.syaapi.models.TornadoDocument;
import uk.gov.hmcts.reform.et.syaapi.service.DocumentGenerationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

/**
 * Rest Controller will use {@link DocumentGenerationService} for document retrieval
 * from Doc Store API / Docmosis.
 * Docmosis (Tornado) exposes an API to create PDF, Doc, DocX, and HTML documents and reports based
 * on the templates and data that we provide.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class DocumentGenerationController {

    /**
     * Method returns a HelloWorld pdf byte array.
     * Will be updated in the future to use {@link DocumentGenerationService}
     * for the retrieval of generated PDFs from the Doc Store API.
     * @param authorization jwt token for authentication
     * @param caseId id for the submitted case
     * @return byte[] pdf binary of the submitted case
     * @throws IOException if there is an issue reading the pdf from source
     */
    @PostMapping(value = "/generate-pdf", produces = "application/pdf")
    @Operation(summary = "Generate submitted case pdf")
    @ApiResponseGroup
    public ResponseEntity<?> generatePdf(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestBody String caseId
    ) {
        ClaimCaseDocument tornadoDoc = new ClaimCaseDocument();
        byte[] pdfDocument;
        try {
            pdfDocument =
                genPdfDocumentStub("add-template-name", "outputFileName.pdf", tornadoDoc);
        } catch (IOException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(pdfDocument);
    }

    /**
     * Stub returns a HelloWorld.pdf byte array.
     * The unused parameters match the signature of genPdfDocument method from {@link DocumentGenerationService}.
     * @param templateName The name of the template used to create the pdf
     * @param outputFileName filename for the generated pdf
     * @param sourceData The JSON format case data with which to populate the template
     * @return byte[] pdf binary
     * @throws IOException if there is an issue reading the pdf from the source folder
     */
    protected byte[] genPdfDocumentStub(String templateName,
                                      String outputFileName,
                                      TornadoDocument sourceData) throws IOException {
        return Files.readAllBytes(Paths.get("src/main/resources/HelloWorld.pdf"));
    }
}
