package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimCaseDocument;
import uk.gov.hmcts.reform.et.syaapi.models.TornadoDocument;

import java.io.IOException;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DocumentGenerationController {

    @PostMapping(value = "/generatePDF", produces = "application/pdf")
    @Operation(summary = "Generate submitted case pdf")
    @ApiResponseGroup
    public ResponseEntity<byte[]> generatePdf(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestBody String caseId
    ) throws IOException {
        ClaimCaseDocument tornadoDoc = new ClaimCaseDocument();
        byte[] pdfDocument =
            genPdfDocumentStub("add-template-name", "outputFileName.pdf", tornadoDoc);
        log.info("Generated document");
        return ResponseEntity.ok(pdfDocument);
    }

    /**
     * Stub returns a HelloWorld.pdf byte array.
     * @param templateName The name of the template
     * @param outputFileName filename for the generated pdf
     * @param sourceData The case data being used
     * @return byte[] pdf binary
     * @throws IOException if there is an issue reading the pdf from the sorurce folder
     */
    protected byte[] genPdfDocumentStub(String templateName,
                                      String outputFileName,
                                      TornadoDocument sourceData) throws IOException {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("HelloWorld.pdf").readAllBytes();
    }
}
