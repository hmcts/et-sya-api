package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseUserRoleConflictException;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseUserRoleNotFoundException;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_IDAM_ID_ALREADY_EXISTS;

@Slf4j
public final class ClaimantUtil {

    private ClaimantUtil() {
    }

    /**
     * Validate if the other party (Claimant/Citizen) is a system user.
     * Non system user Claimant refers to the cases that have been transferred from legacy ECM or a paper based claim
     * which a caseworker would manually create in ExUI.
     * @param caseData in which the case details are extracted from
     * @return errors Error message
     */
    public static boolean isClaimantNonSystemUser(CaseData caseData) {
        if (caseData != null) {
            boolean isNotaSystemUser = caseData.getEt1OnlineSubmission() == null
                && caseData.getHubLinksStatuses() == null;

            return (isNotaSystemUser
                || YES.equals(defaultIfNull(caseData.getMigratedFromEcm(), NO)))
                && !isRepresentedClaimantWithMyHmctsCase(caseData);
        }
        return true;
    }

    private static boolean isRepresentedClaimantWithMyHmctsCase(CaseData caseData) {
        return YES.equals(caseData.getClaimantRepresentedQuestion())
            && ObjectUtils.isNotEmpty(caseData.getRepresentativeClaimantType())
            && ObjectUtils.isNotEmpty(caseData.getRepresentativeClaimantType().getMyHmctsOrganisation());
    }

    public static boolean setClaimantIdamId(CaseDetails caseDetails,
                                            CaseUserAssignmentData caseUserAssignmentData,
                                            String idamId,
                                            String userEmailAddress) {
        Map<String, Object> existingCaseData = caseDetails.getData();
        if (MapUtils.isEmpty(existingCaseData)) {
            throw new CaseUserRoleNotFoundException(
                String.format("Case details %s does not have case data", caseDetails.getId()));
        }

        List<CaseUserAssignment> assignments =
            caseUserAssignmentData == null ? null : caseUserAssignmentData.getCaseUserAssignments();

        if (CollectionUtils.isNotEmpty(assignments)) {
            // Check if user is already assigned
            boolean exists = assignments.stream()
                .anyMatch(a -> idamId.equals(a.getUserId()) && CASE_USER_ROLE_CREATOR.equals(a.getCaseRole()));

            if (exists) {
                log.info("User {} already assigned as claimant for Case {}", idamId, caseDetails.getId());
                return true;
            }

            // Conflict check: If someone else is already the creator
            if (assignments.stream().anyMatch(a -> CASE_USER_ROLE_CREATOR.equals(a.getCaseRole()))) {
                throw new CaseUserRoleConflictException(
                    String.format(EXCEPTION_IDAM_ID_ALREADY_EXISTS, caseDetails.getId()));
            }
        }

        // Only mutate and map data if the user wasn't already assigned
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(existingCaseData);
        caseData.setClaimantId(idamId);
        if (caseData.getClaimantType() != null) {
            caseData.getClaimantType().setClaimantEmailAddress(userEmailAddress);
        } else {
            ClaimantType claimantType = new ClaimantType();
            claimantType.setClaimantEmailAddress(userEmailAddress);
            caseData.setClaimantType(claimantType);
        }
        caseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        return false;
    }
}
