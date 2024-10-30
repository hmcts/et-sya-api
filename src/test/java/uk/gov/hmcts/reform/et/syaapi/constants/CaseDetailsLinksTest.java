package uk.gov.hmcts.reform.et.syaapi.constants;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.SECTION_STATUS_COMPLETED;
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
                checkSectionStatus(respondent.getEt3CaseDetailsLinksStatuses().getPersonalDetails());
                break;
            case CASE_DETAILS_SECTION_ET1_CLAIM_FORM:
                checkSectionStatus(respondent.getEt3CaseDetailsLinksStatuses().getEt1ClaimForm());
                break;
            case CASE_DETAILS_SECTION_RESPONDENT_RESPONSE:
                checkSectionStatus(respondent.getEt3CaseDetailsLinksStatuses().getRespondentResponse());
                break;
            case CASE_DETAILS_SECTION_HEARING_DETAILS:
                checkSectionStatus(respondent.getEt3CaseDetailsLinksStatuses().getHearingDetails());
                break;
            case CASE_DETAILS_SECTION_RESPONDENT_REQUESTS_AND_APPLICATIONS:
                checkSectionStatus(respondent.getEt3CaseDetailsLinksStatuses().getRespondentRequestsAndApplications());
                break;
            default: testRemainingCaseDetailsLinksStatuses(et3Request, respondent);
        }
    }

    private static void testRemainingCaseDetailsLinksStatuses(Et3Request et3Request, RespondentSumType respondent) {
        switch (et3Request.getCaseDetailsLinksSectionId()) {
            case CASE_DETAILS_SECTION_CLAIMANT_APPLICATIONS:
                checkSectionStatus(respondent.getEt3CaseDetailsLinksStatuses().getClaimantApplications());
                break;
            case CASE_DETAILS_SECTION_CONTACT_TRIBUNAL:
                checkSectionStatus(respondent.getEt3CaseDetailsLinksStatuses().getContactTribunal());
                break;
            case CASE_DETAILS_SECTION_TRIBUNAL_ORDERS:
                checkSectionStatus(respondent.getEt3CaseDetailsLinksStatuses().getTribunalOrders());
                break;
            case CASE_DETAILS_SECTION_TRIBUNAL_JUDGEMENTS:
                checkSectionStatus(respondent.getEt3CaseDetailsLinksStatuses().getTribunalJudgements());
                break;
            case CASE_DETAILS_SECTION_DOCUMENTS:
                checkSectionStatus(respondent.getEt3CaseDetailsLinksStatuses().getDocuments());
                break;
            default: assertDoesNotThrow(
                () -> CaseDetailsLinks.setCaseDetailsLinkStatus(respondent,
                                                                et3Request.getCaseDetailsLinksSectionId(),
                                                                et3Request.getCaseDetailsLinksSectionStatus()));
        }
    }

    private static void checkSectionStatus(String existingStatus) {
        if (!existingStatus.equals(SECTION_STATUS_COMPLETED)) {
            assertThat(existingStatus).isEqualTo(SECTION_STATUS_IN_PROGRESS);
            return;
        }
        assertThat(existingStatus).isEqualTo(SECTION_STATUS_COMPLETED);
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
        // Completed Section
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesPersonalDetailsCompleted = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesPersonalDetailsCompleted.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.PERSONAL_DETAILS.name()
        );
        et3RequestUpdateCaseDetailsLinkStatusesPersonalDetailsCompleted.getRespondent()
            .getEt3CaseDetailsLinksStatuses().setPersonalDetails(SECTION_STATUS_COMPLETED);
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesET1ClaimFormCompleted = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesET1ClaimFormCompleted.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.ET1_CLAIM_FORM.name()
        );
        et3RequestUpdateCaseDetailsLinkStatusesET1ClaimFormCompleted.getRespondent().getEt3CaseDetailsLinksStatuses()
            .setEt1ClaimForm(SECTION_STATUS_COMPLETED);
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesRespondentResponseCompleted =
            new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesRespondentResponseCompleted.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.RESPONDENT_RESPONSE.name()
        );
        et3RequestUpdateCaseDetailsLinkStatusesRespondentResponseCompleted.getRespondent()
            .getEt3CaseDetailsLinksStatuses().setRespondentResponse(SECTION_STATUS_COMPLETED);
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesHearingDetailsCompleted = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesHearingDetailsCompleted.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.HEARING_DETAILS.name()
        );
        et3RequestUpdateCaseDetailsLinkStatusesHearingDetailsCompleted.getRespondent().getEt3CaseDetailsLinksStatuses()
            .setHearingDetails(SECTION_STATUS_COMPLETED);
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesRespondentRequestAndApplicationsCompleted =
            new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesRespondentRequestAndApplicationsCompleted.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.RESPONDENT_REQUESTS_AND_APPLICATIONS.name()
        );
        et3RequestUpdateCaseDetailsLinkStatusesRespondentRequestAndApplicationsCompleted.getRespondent()
            .getEt3CaseDetailsLinksStatuses().setRespondentRequestsAndApplications(SECTION_STATUS_COMPLETED);
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesClaimantApplicationsCompleted =
            new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesClaimantApplicationsCompleted.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.CLAIMANT_APPLICATIONS.name()
        );
        et3RequestUpdateCaseDetailsLinkStatusesClaimantApplicationsCompleted.getRespondent()
            .getEt3CaseDetailsLinksStatuses().setClaimantApplications(SECTION_STATUS_COMPLETED);
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesContactTribunalCompleted = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesContactTribunalCompleted.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.CONTACT_TRIBUNAL.name()
        );
        et3RequestUpdateCaseDetailsLinkStatusesContactTribunalCompleted.getRespondent()
            .getEt3CaseDetailsLinksStatuses().setContactTribunal(SECTION_STATUS_COMPLETED);
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesTribunalOrdersCompleted = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesTribunalOrdersCompleted.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.TRIBUNAL_ORDERS.name()
        );
        et3RequestUpdateCaseDetailsLinkStatusesTribunalOrdersCompleted.getRespondent()
            .getEt3CaseDetailsLinksStatuses().setTribunalOrders(SECTION_STATUS_COMPLETED);
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesTribunalJudgementsCompleted =
            new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesTribunalJudgementsCompleted.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.TRIBUNAL_JUDGEMENTS.name()
        );
        et3RequestUpdateCaseDetailsLinkStatusesTribunalJudgementsCompleted.getRespondent()
            .getEt3CaseDetailsLinksStatuses().setTribunalJudgements(SECTION_STATUS_COMPLETED);
        Et3Request et3RequestUpdateCaseDetailsLinkStatusesDocumentsCompleted = new CaseTestData().getEt3Request();
        et3RequestUpdateCaseDetailsLinkStatusesDocumentsCompleted.setCaseDetailsLinksSectionId(
            CaseDetailsLinks.DOCUMENTS.name()
        );
        et3RequestUpdateCaseDetailsLinkStatusesDocumentsCompleted.getRespondent().getEt3CaseDetailsLinksStatuses()
            .setDocuments(SECTION_STATUS_COMPLETED);
        return Stream.of(et3RequestUpdateCaseDetailsLinkStatusesPersonalDetails,
                         et3RequestUpdateCaseDetailsLinkStatusesET1ClaimForm,
                         et3RequestUpdateCaseDetailsLinkStatusesRespondentResponse,
                         et3RequestUpdateCaseDetailsLinkStatusesHearingDetails,
                         et3RequestUpdateCaseDetailsLinkStatusesRespondentRequestAndApplications,
                         et3RequestUpdateCaseDetailsLinkStatusesClaimantApplications,
                         et3RequestUpdateCaseDetailsLinkStatusesContactTribunal,
                         et3RequestUpdateCaseDetailsLinkStatusesTribunalOrders,
                         et3RequestUpdateCaseDetailsLinkStatusesTribunalJudgements,
                         et3RequestUpdateCaseDetailsLinkStatusesDocuments,
                         et3RequestUpdateCaseDetailsLinkStatusesPersonalDetailsCompleted,
                         et3RequestUpdateCaseDetailsLinkStatusesET1ClaimFormCompleted,
                         et3RequestUpdateCaseDetailsLinkStatusesRespondentResponseCompleted,
                         et3RequestUpdateCaseDetailsLinkStatusesHearingDetailsCompleted,
                         et3RequestUpdateCaseDetailsLinkStatusesRespondentRequestAndApplicationsCompleted,
                         et3RequestUpdateCaseDetailsLinkStatusesClaimantApplicationsCompleted,
                         et3RequestUpdateCaseDetailsLinkStatusesContactTribunalCompleted,
                         et3RequestUpdateCaseDetailsLinkStatusesTribunalOrdersCompleted,
                         et3RequestUpdateCaseDetailsLinkStatusesTribunalJudgementsCompleted,
                         et3RequestUpdateCaseDetailsLinkStatusesDocumentsCompleted);
    }
}
