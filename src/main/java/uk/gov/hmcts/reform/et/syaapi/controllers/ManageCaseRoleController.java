package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ecm.common.model.ccd.ModifyCaseUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.ManageCaseRoleService;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

/**
 * Rest Controller to modify user case user roles.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/manageCaseRole")
public class ManageCaseRoleController {

    private final ManageCaseRoleService manageCaseRoleService;

    @PostMapping("/findCaseForRoleModification")
    @Operation(summary = "Modifies user roles of the case")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> findCaseForRoleModification(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @NotNull @RequestBody FindCaseForRoleModificationRequest findCaseForRoleModificationRequest
    ) {
        CaseDetails caseDetails;
        try {
            caseDetails =
                manageCaseRoleService.findCaseForRoleModification(findCaseForRoleModificationRequest, authorisation);
        } catch (IOException e) {
            throw new ManageCaseRoleException(e);
        }
        return ok(caseDetails);
    }

    /**
     * Modifies user role(s) of the case. Modification Type Assignment for assigning a role and
     * modification type Revoke for revoking a role for users with the given user idam id.
     * It also assigns user idam id with the given user full name of the user in respondent collection.
     * @param modifyCaseUserRolesRequest request object which contains modify user case roles
     * @return the modification status of the case
     */
    @PostMapping("/modifyCaseUserRoles")
    @Operation(summary = "Modifies user roles of the case")
    @ApiResponseGroup
    public ResponseEntity<List<CaseDetails>> modifyCaseUserRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @NotNull @Parameter String modificationType,
        @NotNull @RequestBody ModifyCaseUserRolesRequest modifyCaseUserRolesRequest
    ) {
        List<CaseDetails> caseDetailsList;
        try {
            caseDetailsList = manageCaseRoleService.modifyUserCaseRoles(
                authorisation, manageCaseRoleService.generateModifyCaseUserRolesRequest(authorisation,
                                                                                        modifyCaseUserRolesRequest),
                modificationType);
        } catch (Exception e) {
            throw new ManageCaseRoleException(e);
        }
        return ok(caseDetailsList);
    }
}
