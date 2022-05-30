package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;

import java.net.URI;
import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
public class ManageCaseDocumentController {
    private final CaseDocumentService caseDocumentService;

    @Autowired
    public ManageCaseDocumentController(CaseDocumentService caseDocumentService) {
        this.caseDocumentService = caseDocumentService;
    }

    @PostMapping("/uploadDocument/{caseTypeId}")
    @Operation(summary = "Return document url")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "403", description = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<URI> getCaseDetails(
        @RequestHeader("Authorization") String authorization,
        @PathVariable @NotNull String caseTypeId,
        @RequestParam("file") MultipartFile file
    ) {
        var documentLink = caseDocumentService.uploadDocument(authorization, caseTypeId, file);
        return ok(documentLink);
    }
}
