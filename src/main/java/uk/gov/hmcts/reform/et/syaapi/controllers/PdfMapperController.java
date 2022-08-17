package uk.gov.hmcts.reform.et.syaapi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfService;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfServiceException;

/**
 * Rest Controller for {@link PdfService} to convert CaseData into a PDF document
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/cases")
public class PdfMapperController {

    private final PdfService pdfService;


    @PostMapping(value = "/convert-to-pdf")
    public ResponseEntity<byte[]> convertCaseToPdf(
        @RequestBody CaseRequest caseRequest
    ) {
        byte[] pdfDocument;
        try {
            pdfDocument = pdfService.convertCaseToPdf(
                new EmployeeObjectMapper().getEmploymentCaseDataFull(
                    caseRequest.getCaseData()));
        } catch (PdfServiceException ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(pdfDocument);
    }
}
