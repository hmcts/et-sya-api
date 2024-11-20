package uk.gov.hmcts.reform.et.syaapi.constants;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_CANNOT_START_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.SECTION_STATUS_COMPLETED;

class ResponseHubLinksTest {

    private static final String SECTION_STATUS_IN_PROGRESS = "inProgress";

    @ParameterizedTest
    @MethodSource("provideSetResponseHubLinkStatusTestData")
    void theSetResponseHubLinkStatus(Et3Request et3Request) {
        RespondentSumType respondent = et3Request.getRespondent().getValue();
        ResponseHubLinks.setResponseHubLinkStatus(respondent,
                                                  et3Request.getResponseHubLinksSectionId(),
                                                  SECTION_STATUS_IN_PROGRESS);
        if (ResponseHubLinks.CONTACT_DETAILS.toString().equals(et3Request.getResponseHubLinksSectionId())) {
            if (!SECTION_STATUS_COMPLETED.equals(respondent.getEt3HubLinksStatuses().getContactDetails())) {
                assertThat(respondent.getEt3HubLinksStatuses().getContactDetails())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                return;
            }
            assertThat(respondent.getEt3HubLinksStatuses().getContactDetails()).isEqualTo(SECTION_STATUS_COMPLETED);
        }
        if (ResponseHubLinks.CHECK_YOR_ANSWERS.toString().equals(et3Request.getResponseHubLinksSectionId())) {
            if (!SECTION_STATUS_COMPLETED.equals(respondent.getEt3HubLinksStatuses().getCheckYorAnswers())) {
                assertThat(respondent.getEt3HubLinksStatuses().getCheckYorAnswers()).isEqualTo(
                    LINK_STATUS_CANNOT_START_YET);
                return;
            }
            assertThat(respondent.getEt3HubLinksStatuses().getCheckYorAnswers()).isEqualTo(SECTION_STATUS_COMPLETED);
        }
        if (ResponseHubLinks.PAY_PENSION_BENEFIT_DETAILS.toString().equals(et3Request.getResponseHubLinksSectionId())) {
            if (!SECTION_STATUS_COMPLETED.equals(respondent.getEt3HubLinksStatuses().getPayPensionBenefitDetails())) {
                assertThat(respondent.getEt3HubLinksStatuses().getPayPensionBenefitDetails())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                return;
            }
            assertThat(respondent.getEt3HubLinksStatuses().getPayPensionBenefitDetails())
                .isEqualTo(SECTION_STATUS_COMPLETED);
        }
        continueTestResponseHubLinks(et3Request);
    }

    private static void continueTestResponseHubLinks(Et3Request et3Request) {
        RespondentSumType respondent = et3Request.getRespondent().getValue();
        if (ResponseHubLinks.CONCILIATION_AND_EMPLOYEE_DETAILS.toString()
            .equals(et3Request.getResponseHubLinksSectionId())) {
            if (!SECTION_STATUS_COMPLETED.equals(
                respondent.getEt3HubLinksStatuses().getConciliationAndEmployeeDetails())) {
                assertThat(respondent.getEt3HubLinksStatuses().getConciliationAndEmployeeDetails())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                return;
            }
            assertThat(respondent.getEt3HubLinksStatuses().getConciliationAndEmployeeDetails())
                .isEqualTo(SECTION_STATUS_COMPLETED);
        }
        if (ResponseHubLinks.CONTEST_CLAIM.toString().equals(et3Request.getResponseHubLinksSectionId())) {
            if (!SECTION_STATUS_COMPLETED.equals(respondent.getEt3HubLinksStatuses().getContestClaim())) {
                assertThat(respondent.getEt3HubLinksStatuses().getContestClaim())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                return;
            }
            assertThat(respondent.getEt3HubLinksStatuses().getContestClaim()).isEqualTo(SECTION_STATUS_COMPLETED);
        }
        if (ResponseHubLinks.EMPLOYERS_CONTRACT_CLAIM.toString().equals(et3Request.getResponseHubLinksSectionId())) {
            if (!SECTION_STATUS_COMPLETED.equals(respondent.getEt3HubLinksStatuses().getEmployersContractClaim())) {
                assertThat(respondent.getEt3HubLinksStatuses().getEmployersContractClaim()).isEqualTo(
                    SECTION_STATUS_IN_PROGRESS);
                return;
            }
            assertThat(respondent.getEt3HubLinksStatuses().getEmployersContractClaim())
                .isEqualTo(SECTION_STATUS_COMPLETED);
        }
        if (ResponseHubLinks.EMPLOYER_DETAILS.toString().equals(et3Request.getResponseHubLinksSectionId())) {
            if (!SECTION_STATUS_COMPLETED.equals(respondent.getEt3HubLinksStatuses().getEmployerDetails())) {
                assertThat(respondent.getEt3HubLinksStatuses().getEmployerDetails()).isEqualTo(
                    SECTION_STATUS_IN_PROGRESS);
                return;
            }
            assertThat(respondent.getEt3HubLinksStatuses().getEmployerDetails()).isEqualTo(SECTION_STATUS_COMPLETED);
        }
    }

    private static Stream<Et3Request> provideSetResponseHubLinkStatusTestData() {
        Et3Request et3RequestUpdateHubLinkStatusesEmployersContractClaim = new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesEmployersContractClaim.setResponseHubLinksSectionId(
            ResponseHubLinks.EMPLOYERS_CONTRACT_CLAIM.toString()
        );
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
        // COMPLETED SECTION EMPLOYER'S CONTRACT CLAIM
        Et3Request et3RequestUpdateHubLinkStatusesEmployersContractClaimCompleted = new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesEmployersContractClaim.setResponseHubLinksSectionId(
            ResponseHubLinks.EMPLOYERS_CONTRACT_CLAIM.toString()
        );
        et3RequestUpdateHubLinkStatusesEmployersContractClaimCompleted.getRespondent().getValue()
            .getEt3HubLinksStatuses().setEmployersContractClaim(SECTION_STATUS_COMPLETED);
        // COMPLETED SECTION CONTEST CLAIM
        Et3Request et3RequestUpdateHubLinkStatusesContestClaimCompleted = new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesContestClaim.setResponseHubLinksSectionId(
            ResponseHubLinks.CONTEST_CLAIM.toString()
        );
        et3RequestUpdateHubLinkStatusesContestClaimCompleted.getRespondent().getValue()
            .getEt3HubLinksStatuses().setContestClaim(SECTION_STATUS_COMPLETED);
        // COMPLETED SECTION CONTACT DETAILS
        Et3Request et3RequestUpdateHubLinkStatusesContactDetailsCompleted = new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesContactDetailsCompleted.setResponseHubLinksSectionId(
            ResponseHubLinks.CONTACT_DETAILS.toString()
        );
        et3RequestUpdateHubLinkStatusesContactDetailsCompleted.getRespondent().getValue()
            .getEt3HubLinksStatuses().setContactDetails(SECTION_STATUS_COMPLETED);
        // COMPLETED SECTION CHECK YOUR ANSWERS
        Et3Request et3RequestUpdateHubLinkStatusesCheckYourAnswersCompleted = new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesCheckYourAnswersCompleted.setResponseHubLinksSectionId(
            ResponseHubLinks.CHECK_YOR_ANSWERS.toString()
        );
        et3RequestUpdateHubLinkStatusesCheckYourAnswersCompleted.getRespondent().getValue()
            .getEt3HubLinksStatuses().setCheckYorAnswers(SECTION_STATUS_COMPLETED);
        // COMPLETED SECTION CONCILIATION AND EMPLOYMENT DETAILS
        Et3Request et3RequestUpdateHubLinkStatusesConciliationAndEmploymentDetailsCompleted =
            new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesConciliationAndEmploymentDetailsCompleted.setResponseHubLinksSectionId(
            ResponseHubLinks.CONCILIATION_AND_EMPLOYEE_DETAILS.toString()
        );
        et3RequestUpdateHubLinkStatusesConciliationAndEmploymentDetailsCompleted.getRespondent().getValue()
            .getEt3HubLinksStatuses().setConciliationAndEmployeeDetails(SECTION_STATUS_COMPLETED);
        // COMPLETED SECTION EMPLOYER DETAILS
        Et3Request et3RequestUpdateHubLinkStatusesEmployerDetailsCompleted = new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesEmployerDetailsCompleted.setResponseHubLinksSectionId(
            ResponseHubLinks.EMPLOYER_DETAILS.toString()
        );
        et3RequestUpdateHubLinkStatusesEmployerDetailsCompleted.getRespondent().getValue()
            .getEt3HubLinksStatuses().setEmployerDetails(SECTION_STATUS_COMPLETED);
        // COMPLETED PAY PENSION BENEFIT DETAILS
        Et3Request et3RequestUpdateHubLinkStatusesPayPensionBenefitDetailsCompleted =
            new CaseTestData().getEt3Request();
        et3RequestUpdateHubLinkStatusesPayPensionBenefitDetailsCompleted.setResponseHubLinksSectionId(
            ResponseHubLinks.PAY_PENSION_BENEFIT_DETAILS.toString()
        );
        et3RequestUpdateHubLinkStatusesPayPensionBenefitDetailsCompleted.getRespondent().getValue()
            .getEt3HubLinksStatuses().setPayPensionBenefitDetails(SECTION_STATUS_COMPLETED);
        return Stream.of(et3RequestUpdateHubLinkStatusesEmployersContractClaim,
                         et3RequestUpdateHubLinkStatusesContestClaim,
                         et3RequestUpdateHubLinkStatusesContactDetails,
                         et3RequestUpdateHubLinkStatusesCheckYourAnswers,
                         et3RequestUpdateHubLinkStatusesConciliationAndEmploymentDetails,
                         et3RequestUpdateHubLinkStatusesEmployerDetails,
                         et3RequestUpdateHubLinkStatusesPayPensionBenefitDetails,
                         et3RequestUpdateHubLinkStatusesContestClaimCompleted,
                         et3RequestUpdateHubLinkStatusesContactDetailsCompleted,
                         et3RequestUpdateHubLinkStatusesCheckYourAnswersCompleted,
                         et3RequestUpdateHubLinkStatusesConciliationAndEmploymentDetailsCompleted,
                         et3RequestUpdateHubLinkStatusesEmployerDetailsCompleted,
                         et3RequestUpdateHubLinkStatusesPayPensionBenefitDetailsCompleted,
                         et3RequestUpdateHubLinkStatusesEmployersContractClaimCompleted);
    }
}
