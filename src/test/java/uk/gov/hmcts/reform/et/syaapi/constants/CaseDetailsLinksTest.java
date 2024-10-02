package uk.gov.hmcts.reform.et.syaapi.constants;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_CLAIMANT_APPLICATIONS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_CONTACT_TRIBUNAL;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_DOCUMENTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_ET1_CLAIM_FORM;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_HEARING_DETAILS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_PERSONAL_DETAILS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_RESPONDENT_REQUESTS_AND_APPLICATIONS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_RESPONDENT_RESPONSE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_TRIBUNAL_JUDGEMENTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_TRIBUNAL_ORDERS;

class CaseDetailsLinksTest {

    private static final String SECTION_STATUS_IN_PROGRESS = "inProgress";

    @ParameterizedTest
    @MethodSource("provideSetCaseDetailsLinkStatusTestData")
    void theSetCaseDetailsLinkStatus(Et3Request et3Request) {
        RespondentSumType respondent = et3Request.getRespondent();
        CaseDetailsLinks.setCaseDetailsLinkStatus(respondent,
                                                  et3Request.getCaseDetailsLinksSectionId(),
                                                  et3Request.getCaseDetailsLinksSectionStatus());
        switch (et3Request.getCaseDetailsLinksSectionId()) {
            case CASE_DETAILS_SECTION_PERSONAL_DETAILS:
                assertThat(respondent.getEt3CaseDetailsLinksStatuses().getPersonalDetails())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                break;
            case CASE_DETAILS_SECTION_ET1_CLAIM_FORM:
                assertThat(respondent.getEt3CaseDetailsLinksStatuses().getEt1ClaimForm())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                break;
            case CASE_DETAILS_SECTION_RESPONDENT_RESPONSE:
                assertThat(respondent.getEt3CaseDetailsLinksStatuses().getRespondentResponse())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                break;
            case CASE_DETAILS_SECTION_HEARING_DETAILS:
                assertThat(respondent.getEt3CaseDetailsLinksStatuses().getHearingDetails())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                break;
            case CASE_DETAILS_SECTION_RESPONDENT_REQUESTS_AND_APPLICATIONS:
                assertThat(respondent.getEt3CaseDetailsLinksStatuses().getRespondentRequestsAndApplications())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                break;
            default: testRemainingCaseDetailsLinksStatuses(et3Request, respondent);
        }
    }

    private static void testRemainingCaseDetailsLinksStatuses(Et3Request et3Request, RespondentSumType respondent) {
        switch (et3Request.getCaseDetailsLinksSectionId()) {
            case CASE_DETAILS_SECTION_CLAIMANT_APPLICATIONS:
                assertThat(respondent.getEt3CaseDetailsLinksStatuses().getClaimantApplications())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                break;
            case CASE_DETAILS_SECTION_CONTACT_TRIBUNAL:
                assertThat(respondent.getEt3CaseDetailsLinksStatuses().getContactTribunal())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                break;
            case CASE_DETAILS_SECTION_TRIBUNAL_ORDERS:
                assertThat(respondent.getEt3CaseDetailsLinksStatuses().getTribunalOrders())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                break;
            case CASE_DETAILS_SECTION_TRIBUNAL_JUDGEMENTS:
                assertThat(respondent.getEt3CaseDetailsLinksStatuses().getTribunalJudgements())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                break;
            case CASE_DETAILS_SECTION_DOCUMENTS:
                assertThat(respondent.getEt3CaseDetailsLinksStatuses().getDocuments())
                    .isEqualTo(SECTION_STATUS_IN_PROGRESS);
                break;
            default: assertDoesNotThrow(
                () -> CaseDetailsLinks.setCaseDetailsLinkStatus(respondent,
                                                                et3Request.getCaseDetailsLinksSectionId(),
                                                                et3Request.getCaseDetailsLinksSectionStatus()));
        }
    }

    private static Stream<Et3Request> provideSetCaseDetailsLinkStatusTestData() {
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesPersonalDetails = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesPersonalDetails.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.PERSONAL_DETAILS.name()
        );
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesET1ClaimForm = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesET1ClaimForm.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.ET1_CLAIM_FORM.name()
        );
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesRespondentResponse = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesRespondentResponse.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.RESPONDENT_RESPONSE.name()
        );
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesHearingDetails = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesHearingDetails.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.HEARING_DETAILS.name()
        );
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesRespondentRequestAndApplications =
            new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesRespondentRequestAndApplications.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.RESPONDENT_REQUESTS_AND_APPLICATIONS.name()
        );
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesClaimantApplications = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesClaimantApplications.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.CLAIMANT_APPLICATIONS.name()
        );
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesContactTribunal = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesContactTribunal.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.CONTACT_TRIBUNAL.name()
        );
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesTribunalOrders = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesTribunalOrders.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.TRIBUNAL_ORDERS.name()
        );
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesTribunalJudgements = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesTribunalJudgements.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.TRIBUNAL_JUDGEMENTS.name()
        );
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesDocuments = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesDocuments.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.DOCUMENTS.name()
        );
        return Stream.of(et3RequestUpdateCaseDetailsLinkStatusesPersonalDetails,
                         et3RequestUpdateCaseDetailsLinkStatusesET1ClaimForm,
                         et3RequestUpdateCaseDetailsLinkStatusesRespondentResponse,
                         et3RequestUpdateCaseDetailsLinkStatusesHearingDetails,
                         et3RequestUpdateCaseDetailsLinkStatusesRespondentRequestAndApplications,
                         et3RequestUpdateCaseDetailsLinkStatusesClaimantApplications,
                         et3RequestUpdateCaseDetailsLinkStatusesContactTribunal,
                         et3RequestUpdateCaseDetailsLinkStatusesTribunalOrders,
                         et3RequestUpdateCaseDetailsLinkStatusesTribunalJudgements,
                         et3RequestUpdateCaseDetailsLinkStatusesDocuments);
    }
}
