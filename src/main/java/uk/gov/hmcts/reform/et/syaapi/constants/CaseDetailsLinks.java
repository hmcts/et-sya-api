package uk.gov.hmcts.reform.et.syaapi.constants;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

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
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.INVALID_CASE_DETAILS_SECTION_ID;

@Slf4j
public enum CaseDetailsLinks {
    PERSONAL_DETAILS(CASE_DETAILS_SECTION_PERSONAL_DETAILS),
    ET1_CLAIM_FORM(CASE_DETAILS_SECTION_ET1_CLAIM_FORM),
    RESPONDENT_RESPONSE(CASE_DETAILS_SECTION_RESPONDENT_RESPONSE),
    HEARING_DETAILS(CASE_DETAILS_SECTION_HEARING_DETAILS),
    RESPONDENT_REQUESTS_AND_APPLICATIONS(CASE_DETAILS_SECTION_RESPONDENT_REQUESTS_AND_APPLICATIONS),
    CLAIMANT_APPLICATIONS(CASE_DETAILS_SECTION_CLAIMANT_APPLICATIONS),
    CONTACT_TRIBUNAL(CASE_DETAILS_SECTION_CONTACT_TRIBUNAL),
    TRIBUNAL_ORDERS(CASE_DETAILS_SECTION_TRIBUNAL_ORDERS),
    TRIBUNAL_JUDGEMENTS(CASE_DETAILS_SECTION_TRIBUNAL_JUDGEMENTS),
    DOCUMENTS(CASE_DETAILS_SECTION_DOCUMENTS);

    private final String name;

    CaseDetailsLinks(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static void setCaseDetailsLinkStatus(RespondentSumType respondent, String sectionId, String sectionStatus) {
        switch (sectionId) {
            case CASE_DETAILS_SECTION_PERSONAL_DETAILS:
                respondent.getEt3CaseDetailsLinksStatuses().setPersonalDetails(sectionStatus);
                break;
            case CASE_DETAILS_SECTION_ET1_CLAIM_FORM:
                respondent.getEt3CaseDetailsLinksStatuses().setEt1ClaimForm(sectionStatus);
                break;
            case CASE_DETAILS_SECTION_RESPONDENT_RESPONSE:
                respondent.getEt3CaseDetailsLinksStatuses().setRespondentResponse(sectionStatus);
                break;
            case CASE_DETAILS_SECTION_HEARING_DETAILS:
                respondent.getEt3CaseDetailsLinksStatuses().setHearingDetails(sectionStatus);
                break;
            case CASE_DETAILS_SECTION_RESPONDENT_REQUESTS_AND_APPLICATIONS:
                respondent.getEt3CaseDetailsLinksStatuses().setRespondentRequestsAndApplications(sectionStatus);
                break;
            default: setRemainingCaseDetailsLinkStatus(respondent, sectionId, sectionStatus);
        }
    }

    // For not to have cyclomatic complexity - PMD error
    private static void setRemainingCaseDetailsLinkStatus(RespondentSumType respondent,
                                                          String sectionId,
                                                          String sectionStatus) {
        switch (sectionId) {
            case CASE_DETAILS_SECTION_CLAIMANT_APPLICATIONS:
                respondent.getEt3CaseDetailsLinksStatuses().setClaimantApplications(sectionStatus);
                break;
            case CASE_DETAILS_SECTION_CONTACT_TRIBUNAL:
                respondent.getEt3CaseDetailsLinksStatuses().setContactTribunal(sectionStatus);
                break;
            case CASE_DETAILS_SECTION_TRIBUNAL_ORDERS:
                respondent.getEt3CaseDetailsLinksStatuses().setTribunalOrders(sectionStatus);
                break;
            case CASE_DETAILS_SECTION_TRIBUNAL_JUDGEMENTS:
                respondent.getEt3CaseDetailsLinksStatuses().setTribunalJudgements(sectionStatus);
                break;
            case CASE_DETAILS_SECTION_DOCUMENTS:
                respondent.getEt3CaseDetailsLinksStatuses().setDocuments(sectionStatus);
                break;
            default: log.info(INVALID_CASE_DETAILS_SECTION_ID);
        }
    }
}
