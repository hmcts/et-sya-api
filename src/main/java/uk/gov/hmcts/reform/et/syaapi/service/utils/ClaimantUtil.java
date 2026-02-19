package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;

import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_IDAM_ID_ALREADY_EXISTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_ASSIGNMENT;

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
                                            String idamId,
                                            String modificationType) {
        Map<String, Object> existingCaseData = caseDetails.getData();
        if (MapUtils.isEmpty(existingCaseData)) {
            throw new RuntimeException(String.format("Case details %s does not have case data", caseDetails.getId()));
        }
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(existingCaseData);

        if (MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)) {
            String existingClaimantIdamId = caseData.getClaimantId();
            if (StringUtils.isNotBlank(existingClaimantIdamId)) {
                if (idamId.equals(existingClaimantIdamId)) {
                    log.info("User already assigned to case as claimant. UserId: {}, CaseId: {}",
                             idamId, caseDetails.getId());
                    return true;
                }
                throw new RuntimeException(String.format(EXCEPTION_IDAM_ID_ALREADY_EXISTS, caseDetails.getId()));
            }
            caseData.setClaimantId(idamId);
        } else {
            caseData.setClaimantId(StringUtils.EMPTY);
        }

        Map<String, Object> updatedCaseData = EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData);
        caseDetails.setData(updatedCaseData);
        return false;
    }
}
