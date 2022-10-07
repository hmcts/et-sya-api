package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;

import java.util.UUID;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

/**
 * Rest Controller will use {@link CaseDocumentService} for getting document contents and
 *  details from the case management API.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/document")
public class DocumentController {

    private final CaseDocumentService caseDocumentService;

    /**
     * Returns content in binary stream of the given document id.
     * @param authToken jwt token for authentication
     * @param documentId id for the chosen document
     */
    @GetMapping("/download/{documentId}")
    @Operation(summary = "Get document binary content by id from case document api")
    @ApiResponses(
        {@ApiResponse(
            responseCode = "200",
            description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "Case document not found")
    })
    public ResponseEntity<ByteArrayResource> getDocumentBinaryContent(
        @PathVariable("documentId") final UUID documentId,
        @RequestHeader(AUTHORIZATION) String authToken) {

        return caseDocumentService.downloadDocument(authToken, documentId);
    }

    /**
     * Returns document details in JSON format of the given document id.
     * @param authToken jwt token for authentication
     * @param documentId id for the chosen document
     */
    @GetMapping("/details/{documentId}")
    @Operation(summary = "Get document details by id from case document api")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "Case document not found")
    })
    public ResponseEntity<CaseDocument> getDocumentDetails(
        @PathVariable("documentId") final UUID documentId,
        @RequestHeader(AUTHORIZATION) String authToken) {
        log.info("Called DocumentController getDocumentDetails");
        try {
            ResponseEntity<CaseDocument> response =
                caseDocumentService.getDocumentDetails(authToken, documentId);
            return response;
        } catch (Exception ex) {
            log.error("DocumentController exception message: " + ex.getMessage());
            log.error("DocumentController exception cause: " + ex.getCause());
            throw ex;
        }

    }
}
