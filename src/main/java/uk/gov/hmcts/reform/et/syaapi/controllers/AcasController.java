package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocumentAcasResponse;
import uk.gov.hmcts.reform.et.syaapi.service.AcasCaseService;
import uk.gov.hmcts.reform.et.syaapi.service.AdminUserService;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;
import uk.gov.hmcts.reform.et.syaapi.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

/**
 * REST Controller for ACAS to communicate with CCD through ET using Azure API Management.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class AcasController {

    private final AcasCaseService acasCaseService;
    private final CaseDocumentService caseDocumentService;
    private final AdminUserService adminUserService;
    private final FeatureToggleService featureToggleService;

    /**
     * Given a datetime, this method will return a list of caseIds which have been modified since the datetime
     * provided.
     *
     * @param userToken       used for IDAM Authentication
     * @param requestDateTime used for querying when a case was last updated
     * @return a list of case ids
     */
    @GetMapping(value = "/getLastModifiedCaseList")
    @Operation(summary = "Return a list of CCD case IDs from a provided date")
    @ApiResponseGroup
    public ResponseEntity<Object> getLastModifiedCaseList(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String userToken,
        @RequestParam(name = "datetime")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime requestDateTime) {
        return ok(acasCaseService.getLastModifiedCasesId(userToken, requestDateTime));
    }

    /**
     * This method is used to fetch the raw case data from CCD from a list of CaseIds.
     *
     * @param authorisation used for IDAM authentication
     * @param caseIds       a list of CCD ids
     * @return a list of case data
     */
    @GetMapping(value = "/getCaseData")
    @Operation(summary = "Provide a JSON format of the case data for a specific CCD case")
    @ApiResponseGroup
    public ResponseEntity<Object> getCaseData(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestParam(name = "caseIds") List<String> caseIds) {
        return ok(acasCaseService.getCaseData(authorisation, caseIds));
    }

    /**
     * This method is used to retrieve a list of documents which are available to ACAS.
     *
     * @param authorisation used for IDAM authentication
     * @param caseId        ccd case id
     * @return a multi valued map containing a list of documents for ACAS
     */
    @GetMapping(value = "/getAcasDocuments")
    @Operation(summary = "Return a list of documents on a case")
    @ApiResponseGroup
    public ResponseEntity<Object> getAcasDocuments(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestParam(name = "caseId") String caseId) {
        List<CaseDocumentAcasResponse> body = acasCaseService.retrieveAcasDocuments(caseId);
        return ok(body);
    }

    /**
     * This method downloads documents for ACAS. Due to permissions, we retrieve a new token which can view the document
     * and use that to retrieve the document
     *
     * @param documentId UUID for the document in DM Store
     * @param authToken  idam token of ACAS to initially verify access to the API
     * @return document
     */
    @GetMapping("/downloadAcasDocuments")
    @Operation(summary = "Get a document from CDAM in binary format")
    @ApiResponseGroup
    public ResponseEntity<ByteArrayResource> getDocumentBinaryContent(
        @RequestParam(name = "documentId") final UUID documentId,
        @RequestHeader(AUTHORIZATION) String authToken) {
        String accessToken = adminUserService.getAdminUserToken();
        return caseDocumentService.downloadDocument(accessToken, documentId);
    }

    /**
     * THIS API SHOULD ONLY BE USED IN NON-PRODUCTION ENVIRONMENTS.
     * FEATURE TOGGLE IN PLACE TO DISABLE ACCESS IN PRODUCTION
     * Perform ET1 Vetting and Accept the case
     * @param caseId caseId
     * @param authToken authToken
     * @return CaseDetails after vetting and accepting
     */
    @PostMapping("/vetAndAcceptCase")
    @Operation(summary = "Endpoint to vet and accept a case")
    @ApiResponseGroup
    public ResponseEntity<Object> vetAndAcceptCase(
        @RequestParam(name = "caseId") final String caseId,
        @RequestHeader(AUTHORIZATION) String authToken) {
        // Feature flag in place to disable access in Production
        if (featureToggleService.isAcasVetAndAcceptEnabled()) {
            return 16 == caseId.length()
                ? ok(acasCaseService.vetAndAcceptCase(caseId))
                : ResponseEntity.badRequest().body("Invalid caseId");
        } else {
            return ResponseEntity.status(403).body("Feature is disabled");
        }
    }
}
