package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.service.ET3Service;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.STRING_FALSE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.STRING_TRUE;

/**
 * Rest Controller to modify user case user roles.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/et3")
public class ET3Controller {

    private final ET3Service et3Service;

    @PostMapping("/modifyEt3Data")
    @Operation(summary = "Modifies et3 data of the selected respondent")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> modifyEt3Data(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @NotNull @RequestBody Et3Request et3Request
    ) {
        CaseDetails caseDetails = et3Service.modifyEt3Data(authorisation, et3Request);
        return ok(caseDetails);
    }

    @GetMapping("/findCaseByEthosCaseReference")
    @Operation(summary = "Modifies user roles of the case")
    @ApiResponseGroup
    public ResponseEntity<String> checkCaseByEthosCaseReference(
        @RequestParam(name = "ethosCaseReference") String ethosCaseReference) {
        CaseDetails caseDetails = et3Service.findCaseByEthosCaseReference(ethosCaseReference);
        if (ObjectUtils.isNotEmpty(caseDetails)) {
            return ok(STRING_TRUE);
        }
        return ok(STRING_FALSE);
    }

    @GetMapping("/findCaseById")
    @Operation(summary = "Find accepted case by id")
    @ApiResponseGroup
    public ResponseEntity<String> findCaseById(
        @RequestParam(name = "id") String id) {
        CaseDetails caseDetails = et3Service.findCaseByIdAndAcceptedState(id);
        if (ObjectUtils.isNotEmpty(caseDetails)) {
            return ok(STRING_TRUE);
        }
        return ok(STRING_FALSE);
    }
}
