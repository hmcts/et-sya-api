package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.et3links.ET3HubLinksStatuses;
import uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_CANNOT_START_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_NOT_STARTED_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.SECTION_STATUS_COMPLETED;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.SECTION_STATUS_IN_PROGRESS;

class HubLinksUtilTest {

    @Test
    void theSetCheckYourAnswersSection() {
        assertThat(HubLinksUtil.getResponseHubCheckYourAnswersStatus(null,null))
            .isEqualTo(LINK_STATUS_CANNOT_START_YET);
        ET3HubLinksStatuses et3HubLinksStatuses = new ET3HubLinksStatuses();
        CaseData caseData = new CaseData();
        caseData.setTypeOfClaim(List.of(ClaimTypesConstants.BREACH_OF_CONTRACT));
        assertThat(HubLinksUtil.getResponseHubCheckYourAnswersStatus(caseData, et3HubLinksStatuses))
            .isEqualTo(LINK_STATUS_CANNOT_START_YET);
        et3HubLinksStatuses.setContactDetails(SECTION_STATUS_COMPLETED);
        et3HubLinksStatuses.setPayPensionBenefitDetails(SECTION_STATUS_COMPLETED);
        et3HubLinksStatuses.setContestClaim(SECTION_STATUS_COMPLETED);
        et3HubLinksStatuses.setEmployerDetails(SECTION_STATUS_COMPLETED);
        et3HubLinksStatuses.setConciliationAndEmployeeDetails(SECTION_STATUS_COMPLETED);
        et3HubLinksStatuses.setEmployersContractClaim(SECTION_STATUS_IN_PROGRESS);
        assertThat(HubLinksUtil.getResponseHubCheckYourAnswersStatus(caseData, et3HubLinksStatuses))
            .isEqualTo(LINK_STATUS_CANNOT_START_YET);
        et3HubLinksStatuses.setContactDetails(SECTION_STATUS_COMPLETED);
        et3HubLinksStatuses.setPayPensionBenefitDetails(SECTION_STATUS_COMPLETED);
        et3HubLinksStatuses.setContestClaim(SECTION_STATUS_COMPLETED);
        et3HubLinksStatuses.setEmployerDetails(SECTION_STATUS_COMPLETED);
        et3HubLinksStatuses.setConciliationAndEmployeeDetails(SECTION_STATUS_COMPLETED);
        et3HubLinksStatuses.setEmployersContractClaim(SECTION_STATUS_COMPLETED);
        assertThat(HubLinksUtil.getResponseHubCheckYourAnswersStatus(caseData, et3HubLinksStatuses))
            .isEqualTo(LINK_STATUS_NOT_STARTED_YET);
    }

    @Test
    void theIsEmployersContractClaimEntered() {
        CaseData caseData = new CaseTestData().getCaseData();
        ET3HubLinksStatuses et3HubLinksStatuses = new ET3HubLinksStatuses();
        assertThat(HubLinksUtil.isEmployersContractClaimEntered(null, et3HubLinksStatuses))
            .isEqualTo(true);
        caseData.setTypeOfClaim(null);
        assertThat(HubLinksUtil.isEmployersContractClaimEntered(caseData, et3HubLinksStatuses))
            .isEqualTo(true);
        caseData.setTypeOfClaim(List.of(ClaimTypesConstants.DISCRIMINATION));
        assertThat(HubLinksUtil.isEmployersContractClaimEntered(caseData, et3HubLinksStatuses))
            .isEqualTo(true);
        caseData.setTypeOfClaim(List.of(ClaimTypesConstants.BREACH_OF_CONTRACT));
        et3HubLinksStatuses.setEmployersContractClaim(SECTION_STATUS_COMPLETED);
        assertThat(HubLinksUtil.isEmployersContractClaimEntered(caseData, et3HubLinksStatuses))
            .isEqualTo(true);
        et3HubLinksStatuses.setEmployersContractClaim(SECTION_STATUS_IN_PROGRESS);
        assertThat(HubLinksUtil.isEmployersContractClaimEntered(caseData, et3HubLinksStatuses))
            .isEqualTo(false);
    }
}
