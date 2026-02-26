package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

class ClaimantUtilTest {
    CaseData caseData = new CaseData();

    @Test
    void isClaimantNonSystemUserTest_BothNull() {
        caseData.setEt1OnlineSubmission(null);
        caseData.setHubLinksStatuses(null);
        assertTrue(ClaimantUtil.isClaimantNonSystemUser(caseData));
    }

    @Test
    void isClaimantNonSystemUserTest_SubmissionYes() {
        caseData.setEt1OnlineSubmission("Yes");
        caseData.setHubLinksStatuses(null);
        assertFalse(ClaimantUtil.isClaimantNonSystemUser(caseData));
    }

    @Test
    void isClaimantNonSystemUserTest_HubLinksStatuses() {
        caseData.setEt1OnlineSubmission(null);
        caseData.setHubLinksStatuses(new HubLinksStatuses());
        assertFalse(ClaimantUtil.isClaimantNonSystemUser(caseData));
    }

    @Test
    void isClaimantNonSystemUserTest_AllYes() {
        // System user but migrated from ECM so not a system user
        caseData.setEt1OnlineSubmission(YES);
        caseData.setHubLinksStatuses(new HubLinksStatuses());
        caseData.setMigratedFromEcm(YES);
        assertTrue(ClaimantUtil.isClaimantNonSystemUser(caseData));
    }
}
