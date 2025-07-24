package uk.gov.hmcts.reform.et.syaapi.controllers;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentException;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;

/**
 * Rest Controller for {@link CaseDocumentService} for uploading a document to CCD.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/documents")
public class DocumentUploadController {

    private final CaseDocumentService caseDocumentService;

    /**
     * uploads the document to Case Document API and adds linked metadata to the case data within CCD.
     * @param authorization     Required to authenticate caller
     * @param caseTypeId        Which area this case document belongs to e.g. ET_EnglandWales
     * @param multipartFile     File to be uploaded
     * @return                  type {@link CaseDocument} which provides information on the uploaded document
     */
    @PostMapping("/upload/{caseTypeId}")
    public ResponseEntity<CaseDocument> uploadDocument(
        @RequestHeader(AUTHORIZATION) String authorization,
        @PathVariable @NotNull String caseTypeId,
        @RequestParam("document_upload") MultipartFile multipartFile
    ) {
        if (!validateRequest(caseTypeId, multipartFile)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            return ResponseEntity.ok(caseDocumentService.uploadDocument(authorization, caseTypeId,
                multipartFile));
        } catch (CaseDocumentException ex) {
            log.warn("documentUploadController - error occured whilst making call", ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validateRequest(String caseTypeId, MultipartFile multipartFile) {
        if (!caseTypeId.equals(ENGLAND_CASE_TYPE) && !caseTypeId.equals(SCOTLAND_CASE_TYPE)) {
            return false;
        }
        return !multipartFile.isEmpty();
    }
}
