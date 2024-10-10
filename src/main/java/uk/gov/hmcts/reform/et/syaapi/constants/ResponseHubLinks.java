package uk.gov.hmcts.reform.et.syaapi.constants;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.INVALID_RESPONSE_HUB_SECTION_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_CHECK_YOR_ANSWERS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_CONCILIATION_AND_EMPLOYEE_DETAILS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_CONTEST_CLAIM;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_EMPLOYER_DETAILS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.RESPONSE_HUB_SECTION_PAY_PENSION_BENEFIT_DETAILS;

@Slf4j
public enum ResponseHubLinks {
    CONTACT_DETAILS(RESPONSE_HUB_SECTION_CONTACT_DETAILS),
    EMPLOYER_DETAILS(RESPONSE_HUB_SECTION_EMPLOYER_DETAILS),
    CONCILIATION_AND_EMPLOYEE_DETAILS(RESPONSE_HUB_SECTION_CONCILIATION_AND_EMPLOYEE_DETAILS),
    PAY_PENSION_BENEFIT_DETAILS(RESPONSE_HUB_SECTION_PAY_PENSION_BENEFIT_DETAILS),
    CONTEST_CLAIM(RESPONSE_HUB_SECTION_CONTEST_CLAIM),
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
        switch (sectionId) {
            case RESPONSE_HUB_SECTION_CONTACT_DETAILS:
                respondent.getEt3HubLinksStatuses().setContactDetails(sectionStatus);
                break;
            case RESPONSE_HUB_SECTION_EMPLOYER_DETAILS:
                respondent.getEt3HubLinksStatuses().setEmployerDetails(sectionStatus);
                break;
            case RESPONSE_HUB_SECTION_CONCILIATION_AND_EMPLOYEE_DETAILS:
                respondent.getEt3HubLinksStatuses().setConciliationAndEmployeeDetails(sectionStatus);
                break;
            case RESPONSE_HUB_SECTION_PAY_PENSION_BENEFIT_DETAILS:
                respondent.getEt3HubLinksStatuses().setPayPensionBenefitDetails(sectionStatus);
                break;
            case RESPONSE_HUB_SECTION_CONTEST_CLAIM:
                respondent.getEt3HubLinksStatuses().setContestClaim(sectionStatus);
                break;
            case RESPONSE_HUB_SECTION_CHECK_YOR_ANSWERS:
                respondent.getEt3HubLinksStatuses().setCheckYorAnswers(sectionStatus);
                break;
            default: log.info(INVALID_RESPONSE_HUB_SECTION_ID);
        }
    }
}
