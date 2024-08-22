package uk.gov.hmcts.reform.et.syaapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.annotation.ApiResponseGroup;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseRoleManagementException;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseRoleManagementService;

import javax.validation.constraints.NotNull;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.AUTHORIZATION;

/**
 * Rest Controller to modify case user roles.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/caseRoleManagement")
public class CaseRoleManagementController {

    private static final String CASE_USER_ROLE_SUCCESSFULLY_MODIFIED = "Successfully modified case user role";
    private final CaseRoleManagementService caseRoleManagementService;

    @PostMapping("/findCaseForRoleModification")
    @Operation(summary = "Modifies user roles of the case")
    @ApiResponseGroup
    public ResponseEntity<CaseDetails> findCaseForRoleModification(
        @NotNull @RequestBody FindCaseForRoleModificationRequest findCaseForRoleModificationRequest
    ) {
        CaseDetails caseDetails =
            caseRoleManagementService.findCaseForRoleModification(findCaseForRoleModificationRequest);
        return ok(caseDetails);
    }

    /**
     * Modifies user role(s) of the case. Modification Type Assignment for assigning a role and
     * modification type Revoke for revoking a role for users.
     * @param caseAssignmentUserRolesRequest the request object which contains user case roles list
     * @return the modification status of the case
     */
    @PostMapping("/modifyCaseUserRoles")
    @Operation(summary = "Modifies user roles of the case")
    @ApiResponseGroup
    public ResponseEntity<String> modifyCaseUserRoles(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @NotNull @Parameter String modificationType,
        @NotNull @RequestBody CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest
    ) {
        try {
            caseRoleManagementService.modifyUserCaseRoles(
                caseRoleManagementService.generateCaseAssignmentUserRolesRequestWithUserIds(
                    authorisation, caseAssignmentUserRolesRequest),
                modificationType);
        } catch (Exception e) {
            throw new CaseRoleManagementException(e);
        }
        return ok(CASE_USER_ROLE_SUCCESSFULLY_MODIFIED);
    }
}
