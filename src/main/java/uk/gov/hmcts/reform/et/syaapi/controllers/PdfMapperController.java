package uk.gov.hmcts.reform.et.syaapi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfService;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfServiceException;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

/**
 * Rest Controller for {@link PdfService} to convert CaseData into a PDF document
 */
@RequiredArgsConstructor
@RestController
public class PdfMapperController {

    private PdfService pdfService;

    @PostMapping(value = "case-to-pdf")
    public ResponseEntity<byte[]> convertCaseToPdf(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestBody CaseData caseData
    ) {
        byte[] pdfDocument;
        try {
            pdfDocument = pdfService.convertCaseToPdf(caseData);
        } catch (PdfServiceException ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(pdfDocument);
    }
}
