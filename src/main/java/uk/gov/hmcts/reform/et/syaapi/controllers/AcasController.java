package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

/**
 * REST Controller for ACAS to communicate with CCD through ET using Azure API Management.
*/
@Slf4j
@RequiredArgsConstructor
@RestController
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class AcasController {

    private final CaseService caseService;

    /**
     * This method is used to fetch the raw case data from CCD from a list of CaseIds.
     * @param authorisation used for IDAM authentication
     * @param caseIds a list of CCD ids
     * @return a list of case data
     */
    @GetMapping(value = "/getCaseData")
    @Operation(summary = "Provide a JSON format of the case data for a specific CCD case")
    @ApiResponseGroup
    public ResponseEntity<Object> getCaseData(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestParam(name = "caseIds") List<String> caseIds) {
        List<CaseDetails> caseDetailsList = caseService.getCaseData(authorisation, caseIds);
        if (CollectionUtils.isEmpty(caseDetailsList)) {
            return ok("No cases found");
        }
        return ok(caseDetailsList);
    }
}
