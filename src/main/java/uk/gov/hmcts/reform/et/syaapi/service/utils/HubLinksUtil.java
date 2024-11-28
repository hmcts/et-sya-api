package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.et3links.ET3HubLinksStatuses;
import uk.gov.hmcts.reform.et.syaapi.constants.CaseDetailsLinks;
import uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants;
import uk.gov.hmcts.reform.et.syaapi.constants.ResponseHubLinks;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_CANNOT_START_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_NOT_STARTED_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.SECTION_STATUS_COMPLETED;

public final class HubLinksUtil {

    private HubLinksUtil() {
        // restrict instantiation
    }

    public static String getResponseHubCheckYourAnswersStatus(CaseData caseData,
                                                              ET3HubLinksStatuses et3HubLinksStatuses) {
        if (ObjectUtils.isEmpty(et3HubLinksStatuses)) {
            return LINK_STATUS_CANNOT_START_YET;
        }
        if (isET3HubLinkStatusCompleted(et3HubLinksStatuses.getContactDetails())
            && isET3HubLinkStatusCompleted(et3HubLinksStatuses.getContestClaim())
            && isET3HubLinkStatusCompleted(et3HubLinksStatuses.getEmployerDetails())
            && isET3HubLinkStatusCompleted(et3HubLinksStatuses.getPayPensionBenefitDetails())
            && isET3HubLinkStatusCompleted(et3HubLinksStatuses.getConciliationAndEmployeeDetails())
            && isEmployersContractClaimEntered(caseData, et3HubLinksStatuses)) {
            return LINK_STATUS_NOT_STARTED_YET;
        }
        return LINK_STATUS_CANNOT_START_YET;
    }

    private static boolean isET3HubLinkStatusCompleted(String status) {
        return StringUtils.isNotBlank(status) && SECTION_STATUS_COMPLETED.equals(status);
    }

    public static boolean isEmployersContractClaimEntered(CaseData caseData, ET3HubLinksStatuses et3HubLinksStatuses) {
        if (ObjectUtils.isEmpty(caseData) || CollectionUtils.isEmpty(caseData.getTypeOfClaim())) {
            return true;
        }
        if (!caseData.getTypeOfClaim().contains(ClaimTypesConstants.BREACH_OF_CONTRACT)) {
            return true;
        }
        return isET3HubLinkStatusCompleted(et3HubLinksStatuses.getEmployersContractClaim());
    }

    public static void setLinkStatuses(CaseData caseData, RespondentSumType respondentSumType, Et3Request et3Request) {
        ResponseHubLinks.setResponseHubLinkStatus(
            respondentSumType,
            et3Request.getResponseHubLinksSectionId(),
            et3Request.getResponseHubLinksSectionStatus());
        CaseDetailsLinks.setCaseDetailsLinkStatus(
            respondentSumType,
            et3Request.getCaseDetailsLinksSectionId(),
            et3Request.getCaseDetailsLinksSectionStatus());
        respondentSumType.getEt3HubLinksStatuses()
            .setCheckYorAnswers(getResponseHubCheckYourAnswersStatus(caseData,
                                                                     respondentSumType.getEt3HubLinksStatuses()));
    }
}
