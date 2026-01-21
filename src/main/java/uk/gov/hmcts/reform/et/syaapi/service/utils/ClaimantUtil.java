package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

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
}
