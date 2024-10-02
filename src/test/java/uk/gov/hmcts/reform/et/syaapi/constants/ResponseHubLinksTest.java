package uk.gov.hmcts.reform.et.syaapi.constants;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseHubLinksTest {

    private static final String SECTION_STATUS_IN_PROGRESS = "inProgress";

    @ParameterizedTest
    @MethodSource("provideSetResponseHubLinkStatusTestData")
    void theSetResponseHubLinkStatus(Et3Request et3Request) {
        RespondentSumType respondent = et3Request.getRespondent();
        ResponseHubLinks.setResponseHubLinkStatus(respondent,
                                                  et3Request.getResponseHubLinksSectionId(),
                                                  et3Request.getResponseHubLinksSectionStatus());
        if (ResponseHubLinks.CONTACT_DETAILS.toString().equals(et3Request.getResponseHubLinksSectionId())) {
            assertThat(respondent.getEt3HubLinksStatuses().getContactDetails()).isEqualTo(SECTION_STATUS_IN_PROGRESS);
        }
        if (ResponseHubLinks.CHECK_YOR_ANSWERS.toString().equals(et3Request.getResponseHubLinksSectionId())) {
            assertThat(respondent.getEt3HubLinksStatuses().getCheckYorAnswers()).isEqualTo(SECTION_STATUS_IN_PROGRESS);
        }
        if (ResponseHubLinks.CONCILIATION_AND_EMPLOYEE_DETAILS.toString()
            .equals(et3Request.getResponseHubLinksSectionId())) {
            assertThat(respondent.getEt3HubLinksStatuses().getConciliationAndEmployeeDetails())
                .isEqualTo(SECTION_STATUS_IN_PROGRESS);
        }
        if (ResponseHubLinks.CONTEST_CLAIM.toString().equals(et3Request.getResponseHubLinksSectionId())) {
            assertThat(respondent.getEt3HubLinksStatuses().getContestClaim()).isEqualTo(SECTION_STATUS_IN_PROGRESS);
        }
        if (ResponseHubLinks.EMPLOYER_DETAILS.toString().equals(et3Request.getResponseHubLinksSectionId())) {
            assertThat(respondent.getEt3HubLinksStatuses().getEmployerDetails()).isEqualTo(SECTION_STATUS_IN_PROGRESS);
        }
        if (ResponseHubLinks.PAY_PENSION_BENEFIT_DETAILS.toString().equals(et3Request.getResponseHubLinksSectionId())) {
            assertThat(respondent.getEt3HubLinksStatuses().getPayPensionBenefitDetails())
                .isEqualTo(SECTION_STATUS_IN_PROGRESS);
        }
    }

    private static Stream<Et3Request> provideSetResponseHubLinkStatusTestData() {
        Et3Request et3RequestUpdateHubLinkStatusesContestClaim = new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesContestClaim.setResponseHubLinksSectionId(
            ResponseHubLinks.CONTEST_CLAIM.toString()
        );
        Et3Request et3RequestUpdateHubLinkStatusesContactDetails = new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesContactDetails.setResponseHubLinksSectionId(
            ResponseHubLinks.CONTACT_DETAILS.toString()
        );
        Et3Request et3RequestUpdateHubLinkStatusesCheckYourAnswers = new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesCheckYourAnswers.setResponseHubLinksSectionId(
            ResponseHubLinks.CHECK_YOR_ANSWERS.toString()
        );
        Et3Request et3RequestUpdateHubLinkStatusesConciliationAndEmploymentDetails = new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesConciliationAndEmploymentDetails.setResponseHubLinksSectionId(
            ResponseHubLinks.CONCILIATION_AND_EMPLOYEE_DETAILS.toString()
        );
        Et3Request et3RequestUpdateHubLinkStatusesEmployerDetails = new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesEmployerDetails.setResponseHubLinksSectionId(
            ResponseHubLinks.EMPLOYER_DETAILS.toString()
        );
        Et3Request et3RequestUpdateHubLinkStatusesPayPensionBenefitDetails = new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesPayPensionBenefitDetails.setResponseHubLinksSectionId(
            ResponseHubLinks.PAY_PENSION_BENEFIT_DETAILS.toString()
        );
        return Stream.of(et3RequestUpdateHubLinkStatusesContestClaim,
                         et3RequestUpdateHubLinkStatusesContactDetails,
                         et3RequestUpdateHubLinkStatusesCheckYourAnswers,
                         et3RequestUpdateHubLinkStatusesConciliationAndEmploymentDetails,
                         et3RequestUpdateHubLinkStatusesEmployerDetails,
                         et3RequestUpdateHubLinkStatusesPayPensionBenefitDetails);
    }
}
