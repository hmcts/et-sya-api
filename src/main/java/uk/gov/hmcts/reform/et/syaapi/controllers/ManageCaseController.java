package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.search.Query;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;

import java.util.List;
import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ZERO_INTEGER;

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
        Query query = new Query(QueryBuilders.wrapperQuery(searchString), ZERO_INTEGER);
        return ok(caseService.getCaseDataByUser(authorization, caseType, query.toString()));
    }

    @PostMapping("/initiate-case")
    @Operation(summary = "Create a new default case")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> createCase(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody CallbackRequest callback
    ) {
        log.info("Received initiate-case request - caseTypeId: {} eventId: {}",
                 callback.getCaseDetails().getCaseTypeId(), callback.getEventId());

        var caseDetails = caseService.createCase(authorization, callback.getCaseDetails().getCaseTypeId(),
                                                            callback.getEventId(),
                                                            callback.getCaseDetails().getData());
        return ok(caseDetails);
    }

    @PutMapping("/manage-case")
    @Operation(summary = "Update draft case API method")
    @ApiResponseGroup
    public CaseDetails updateCase(
        @RequestHeader(AUTHORIZATION) String authorization,
        @NotNull @RequestBody CallbackRequest callback
    ) {

        log.info("Received manage-case request - caseTypeId: {} eventId: {}",
                 callback.getCaseDetails().getCaseTypeId(), callback.getEventId());

        return caseService.triggerEvent(authorization, String.valueOf(callback.getCaseDetails().getId()),
                                        callback.getCaseDetails().getCaseTypeId(),
                                        CaseEvent.valueOf(callback.getEventId()),
                                        callback.getCaseDetails().getData());
    }
}
