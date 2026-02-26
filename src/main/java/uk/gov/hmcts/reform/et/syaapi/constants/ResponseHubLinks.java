package uk.gov.hmcts.reform.et.syaapi.constants;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.service.utils.HubLinksUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.RespondentUtil;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.SECTION_STATUS_COMPLETED;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.SECTION_STATUS_IN_PROGRESS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.SECTION_STATUS_IN_PROGRESS_CYA;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.INVALID_RESPONSE_HUB_SECTION_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_CHECK_YOR_ANSWERS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_CONCILIATION_AND_EMPLOYEE_DETAILS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_CONTEST_CLAIM;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_EMPLOYERS_CONTRACT_CLAIM;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_EMPLOYER_DETAILS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_PAY_PENSION_BENEFIT_DETAILS;

@Slf4j
public enum ResponseHubLinks {
    CONTACT_DETAILS(RESPONSE_HUB_SECTION_CONTACT_DETAILS),
    EMPLOYER_DETAILS(RESPONSE_HUB_SECTION_EMPLOYER_DETAILS),
    CONCILIATION_AND_EMPLOYEE_DETAILS(RESPONSE_HUB_SECTION_CONCILIATION_AND_EMPLOYEE_DETAILS),
    PAY_PENSION_BENEFIT_DETAILS(RESPONSE_HUB_SECTION_PAY_PENSION_BENEFIT_DETAILS),
    CONTEST_CLAIM(RESPONSE_HUB_SECTION_CONTEST_CLAIM),
    EMPLOYERS_CONTRACT_CLAIM(RESPONSE_HUB_SECTION_EMPLOYERS_CONTRACT_CLAIM),
    CHECK_YOR_ANSWERS(RESPONSE_HUB_SECTION_CHECK_YOR_ANSWERS);

    private final String name;

    ResponseHubLinks(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static void setResponseHubLinkStatus(RespondentSumType respondent,
                                                String sectionId,
                                                String sectionStatus) {
        // Initialize et3HubLinksStatuses with defaults if null, following the pattern from RespondentUtil.java
        if (ObjectUtils.isEmpty(respondent.getEt3HubLinksStatuses())) {
            respondent.setEt3HubLinksStatuses(RespondentUtil.generateDefaultET3HubLinksStatuses());
        }
        switch (sectionId) {
            case RESPONSE_HUB_SECTION_CONTACT_DETAILS: {
                respondent.getEt3HubLinksStatuses().setContactDetails(
                    getSectionValue(respondent.getEt3HubLinksStatuses().getContactDetails(), sectionStatus));
                break;
            }
            case RESPONSE_HUB_SECTION_EMPLOYER_DETAILS:
                respondent.getEt3HubLinksStatuses().setEmployerDetails(
                    getSectionValue(respondent.getEt3HubLinksStatuses().getEmployerDetails(), sectionStatus));
                break;
            case RESPONSE_HUB_SECTION_CONCILIATION_AND_EMPLOYEE_DETAILS:
                respondent.getEt3HubLinksStatuses().setConciliationAndEmployeeDetails(
                    getSectionValue(respondent.getEt3HubLinksStatuses().getConciliationAndEmployeeDetails(),
                                    sectionStatus));
                break;
            case RESPONSE_HUB_SECTION_PAY_PENSION_BENEFIT_DETAILS:
                respondent.getEt3HubLinksStatuses().setPayPensionBenefitDetails(
                    getSectionValue(respondent.getEt3HubLinksStatuses().getPayPensionBenefitDetails(), sectionStatus));
                break;
            case RESPONSE_HUB_SECTION_CONTEST_CLAIM:
                respondent.getEt3HubLinksStatuses().setContestClaim(
                    getSectionValue(respondent.getEt3HubLinksStatuses().getContestClaim(), sectionStatus));
                break;
            case RESPONSE_HUB_SECTION_EMPLOYERS_CONTRACT_CLAIM:
                respondent.getEt3HubLinksStatuses().setEmployersContractClaim(
                    getSectionValue(respondent.getEt3HubLinksStatuses().getEmployersContractClaim(), sectionStatus));
                break;
            case RESPONSE_HUB_SECTION_CHECK_YOR_ANSWERS:
                respondent.getEt3HubLinksStatuses().setCheckYorAnswers(
                    getSectionValue(respondent.getEt3HubLinksStatuses().getCheckYorAnswers(),
                                    HubLinksUtil
                                        .getResponseHubCheckYourAnswersStatus(null,
                                                                              respondent.getEt3HubLinksStatuses())));
                break;
            default: log.info(INVALID_RESPONSE_HUB_SECTION_ID);
        }
    }

    public static String getSectionValue(String existingValue, String newValue) {
        if (SECTION_STATUS_IN_PROGRESS_CYA.equals(newValue)) {
            return SECTION_STATUS_IN_PROGRESS;
        }
        if (!SECTION_STATUS_COMPLETED.equals(existingValue)) {
            return newValue;
        }
        return existingValue;
    }
}
