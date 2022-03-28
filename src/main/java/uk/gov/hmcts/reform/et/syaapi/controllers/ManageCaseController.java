package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.search.Query;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;

import java.util.List;
import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ManageCaseController {

    @Autowired
    private CaseService caseService;

    @GetMapping("/caseDetails/{caseId}")
    @Operation(summary = "Return case details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "403", description = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CaseDetails> getCaseDetails(
        @RequestHeader("Authorization") String authorization,
        @PathVariable String caseId
    ) {
        var caseDetails = caseService.getCaseData(authorization, caseId);
        return ok(caseDetails);
    }

    /*
    Service would talk to CCD ElasticSearch API and with one implemented method -
    * dev shall play around DSL query to get output (couple of examples of request body as below)
    1. Pull all cases
        {"match_all": {}}
    2. Pull specific cases
        {"terms": {"reference": ["XXX", "YYY"]}}
    3, Pull specific case
        {"match": {"reference": "XXX"}}
    */
    @GetMapping("/caseTypes/{caseType}/cases")
    @Operation(summary = "Return all case details for User")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "403", description = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<List<CaseDetails>> getCaseDataByUser(
        @RequestHeader("Authorization") String authorization,
        @PathVariable String caseType,
        @RequestBody String searchString
    ) {
        Query query = new Query(QueryBuilders.wrapperQuery(searchString), 0);
        List<CaseDetails> casesByUser = caseService.getCaseDataByUser(authorization, caseType, query.toString());

        return ok(casesByUser);
    }

    @PostMapping("/case-type/{caseType}/event-type/{eventType}/case")
    @Operation(summary = "Create a new default case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "403", description = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CaseDetails> createCase(
        @RequestHeader("Authorization") String authorization,
        @PathVariable @NotNull String caseType,
        @PathVariable @NotNull String eventType,
        @RequestBody String caseData
    ) {
        var caseDetails = caseService.createCase(authorization, caseType, eventType, caseData);
        return ok(caseDetails);
    }
}
