package uk.gov.hmcts.reform.et.syaapi.controllers;

import java.io.File;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentException;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfService;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfServiceException;

/**
 * Rest Controller for {@link PdfService} to convert CaseData into a PDF document
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/document")
public class DocumentUploadController {

    private final CaseDocumentService caseDocumentService;


    @PostMapping(value = "/upload", produces = "application/pdf")
    public ResponseEntity<String> convertCaseToPdf(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(CASE_TYPE) String caseTypeId,
        @RequestBody File file
    ) {

        try {
            caseDocumentService.uploadDocument(authorization, caseTypeId)
        } catch (CaseDocumentException ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok("Test");
    }
}
