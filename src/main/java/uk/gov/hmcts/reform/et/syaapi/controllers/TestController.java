package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.et.syaapi.models.DocumentDetailsResponse;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;

import java.util.UUID;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {

    private final CaseDocumentService caseDocumentService;

    @GetMapping("/document/{documentId}")
    @Operation(summary = "Get document details by id from case document api")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "Case document not found")
    })
    public ResponseEntity<DocumentDetailsResponse> getDocumentDetails(
        @PathVariable("documentId") final UUID documentId,
        @RequestHeader(AUTHORIZATION) String authToken) {
        log.info("Called TestController getDocumentDetails");
        return caseDocumentService.getDocumentDetails(authToken, documentId);
    }
}
