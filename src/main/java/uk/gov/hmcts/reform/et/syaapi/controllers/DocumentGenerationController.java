package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.models.TornadoDocument;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimCaseDocument;

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
        byte[] pdfDocument = genPdfDocumentStub("add-template-name", "outputFileName.pdf", tornadoDoc);
        log.info("Generated document");
        return ResponseEntity.ok(pdfDocument);
    }

    private byte[] genPdfDocumentStub(String templateName, String outputFileName, TornadoDocument sourceData) throws IOException {
        return this.getClass().getClassLoader().getResourceAsStream("HelloWorld.pdf").readAllBytes();
    }
}
