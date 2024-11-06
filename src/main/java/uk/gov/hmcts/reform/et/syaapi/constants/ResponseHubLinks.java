package uk.gov.hmcts.reform.et.syaapi.constants;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResponseUtil;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.SECTION_STATUS_COMPLETED;
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
            case RESPONSE_HUB_SECTION_CHECK_YOR_ANSWERS:
                respondent.getEt3HubLinksStatuses().setCheckYorAnswers(
                    getSectionValue(respondent.getEt3HubLinksStatuses().getCheckYorAnswers(),
                                    ResponseUtil
                                        .getResponseHubCheckYourAnswersStatus(respondent.getEt3HubLinksStatuses())));
                break;
            default: log.info(INVALID_RESPONSE_HUB_SECTION_ID);
        }
    }

    private static String getSectionValue(String existingValue, String newValue) {
        if (!SECTION_STATUS_COMPLETED.equals(existingValue)) {
            return newValue;
        }
        return existingValue;
    }
}
