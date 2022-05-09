package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;

import java.io.IOException;
import java.util.List;
import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ManageCaseController {

    @Autowired
    private CaseService caseService;

    @GetMapping("/caseDetails/{caseId}")
    @Operation(summary = "Return case details")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> getCaseDetails(
        @RequestHeader(AUTHORIZATION) String authorization,
        @PathVariable String caseId
    ) {
        var caseDetails = caseService.getCaseData(authorization, caseId);
        return ok(caseDetails);
    }

    @GetMapping("/caseTypes/{caseType}/cases")
    @Operation(summary = "Return all case details for User")
    @ApiResponseGroup
    public ResponseEntity<List<CaseDetails>> getCaseDataByUser(
        @RequestHeader(AUTHORIZATION) String authorization,
        @PathVariable String caseType,
        @RequestBody String searchString
    ) {
        return ok(caseService.getCaseDataByUser(authorization, caseType));
    }

    @PostMapping("/case-type/{caseType}/event-type/{eventType}/case")
    @Operation(summary = "Create a new default case")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> createCase(
        @RequestHeader(AUTHORIZATION) String authorization,
        @PathVariable @NotNull String caseType,
        @PathVariable @NotNull String eventType,
        @RequestBody String caseData
    ) {
        var caseDetails = caseService.createCase(authorization, caseType, eventType, caseData);
        return ok(caseDetails);
    }

    @PutMapping("/case-type/{caseType}/event-type/{eventType}/{caseId}")
    @Operation(summary = "Update draft case API method")
    @ApiResponseGroup
    public CaseData updateCase(
        @RequestHeader(AUTHORIZATION) String authorization,
        @PathVariable @NotNull String caseType,
        @PathVariable @NotNull String eventType,
        @PathVariable @NotNull String caseId,
        @RequestBody String caseData
    ) {
        return caseService.triggerEvent(authorization, caseId, caseType, CaseEvent.valueOf(eventType), caseData);
    }

    @GetMapping("/generate-pdf/{caseId}")
    @Operation(summary = "Generate submitted case pdf")
    @ApiResponseGroup
    public ResponseEntity<InputStreamResource> generatePdf(
        @RequestHeader(AUTHORIZATION) String authorization,
        @PathVariable String caseId
    )  throws IOException {

        /*
         generate a submitted case PDF
         This is a stub which returns a blank pdf.
         It should use the documentGenerationService in the future
        */
        ClassPathResource pdfFile = new ClassPathResource("HelloWorld.pdf");
        return ResponseEntity
            .ok()
            .contentLength(pdfFile.contentLength())
            .contentType(
                MediaType.parseMediaType("application/octet-stream"))
            .body(new InputStreamResource(pdfFile.getInputStream()));

    }
}
