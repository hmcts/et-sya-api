package uk.gov.hmcts.reform.et.syaapi.controllers;

import java.util.UUID;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.models.DocumentDetailsResponse;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/document")
public class DocumentController {

    private final CaseDocumentService caseDocumentService;

    @GetMapping("/download/{documentId}")
    @Operation(summary = "Get document binary content by id from case document api")
    @ApiResponseGroup
    public ResponseEntity<ByteArrayResource> getDocumentBinaryContent(
        @PathVariable("documentId") final UUID documentId,
        @RequestHeader(AUTHORIZATION) String authToken) {

        return caseDocumentService.downloadDocument(authToken, documentId);
    }

    @GetMapping("/details/{documentId}")
    @Operation(summary = "Get document details by id from case document api")
    @ApiResponseGroup
    public ResponseEntity<DocumentDetailsResponse> getDocumentDetails(
        @PathVariable("documentId") final UUID documentId,
        @RequestHeader(AUTHORIZATION) String authToken) {

        return caseDocumentService.getDocumentDetails(authToken, documentId);
    }
}
