package uk.gov.hmcts.reform.et.syaapi.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.models.EmploymentCaseData;
import uk.gov.hmcts.reform.et.syaapi.search.Query;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ZERO_INTEGER;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.submitCaseDraft;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ManageCaseController {

    private final CaseDetailsConverter caseDetailsConverter;

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
        Query query = new Query(QueryBuilders.wrapperQuery(searchString), ZERO_INTEGER);
        return ok(caseService.getCaseDataByUser(authorization, caseType, query.toString()));
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

    @PostMapping("/case-type/{caseType}/event-type/{eventType}/updateCase/{caseId}")
    @Operation(summary = "Update draft case API method")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "403", description = "Calling service is not authorised to use the endpoint"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public CaseData updateCase (
        @RequestHeader("Authorization") String authorization,
        @PathVariable @NotNull String caseType,
        @PathVariable @NotNull String eventType,
        @PathVariable @NotNull String caseId,
        @RequestBody String caseData
    ) {
        EmploymentCaseData employmentCaseData = getEmploymentCaseData(caseData);
        StartEventResponse startEventResponse = caseService.startUpdate(authorization,
                                                                        caseId, caseType, submitCaseDraft);
        return caseService.submitUpdate(authorization, caseId,
                                        caseDetailsConverter.caseDataContent(startEventResponse, employmentCaseData),
                                        caseType);
    }

    private EmploymentCaseData getEmploymentCaseData(String caseData) {
        ObjectMapper mapper = new ObjectMapper();
        EmploymentCaseData data = null;
        try {
           data = mapper.readValue(caseData, EmploymentCaseData.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return data;
    }
}
