package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;

import java.io.IOException;


import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
public class DocumentGenerationController {

    @PostMapping(value = "/generate-case-pdf", produces = "application/pdf")
    @Operation(summary = "Generate submitted case pdf")
    @ApiResponseGroup
    public ResponseEntity<byte[]> generatePdf(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestBody String caseId
    ) throws IOException {

        byte[] contents = pdfStub();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String fileName = "submitted-case.pdf";
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        ResponseEntity<byte[]> response = new ResponseEntity<>(contents, headers, HttpStatus.OK);
        return response;

    }

    private byte[] pdfStub() throws IOException {

        byte[] helloWorldPdf = this.getClass().getClassLoader().getResourceAsStream("HelloWorld.pdf").readAllBytes();
        return helloWorldPdf;
    }
}
