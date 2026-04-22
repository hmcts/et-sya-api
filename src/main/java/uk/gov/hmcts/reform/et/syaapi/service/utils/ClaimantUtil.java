package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseUserRoleConflictException;

import java.util.List;

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

    public static boolean validateClaimantAssignment(CaseDetails caseDetails,
                                                     CaseUserAssignmentData caseUserAssignmentData,
                                                     String idamId) {
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
        return false;
    }
}
