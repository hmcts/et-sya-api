package uk.gov.hmcts.reform.et.syaapi.constants;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.service.utils.RespondentUtil;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.SECTION_STATUS_COMPLETED;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_CLAIMANT_APPLICATIONS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_CONTACT_TRIBUNAL;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_DOCUMENTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_ET1_CLAIM_FORM;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_HEARING_DETAILS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.CASE_DETAILS_SECTION_OTHER_RESPONDENT_APPLICATIONS;
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
    OTHER_RESPONDENT_APPLICATIONS(CASE_DETAILS_SECTION_OTHER_RESPONDENT_APPLICATIONS),
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
        // Initialize et3CaseDetailsLinksStatuses with defaults if null, following the pattern from RespondentUtil.java
        if (ObjectUtils.isEmpty(respondent.getEt3CaseDetailsLinksStatuses())) {
            respondent.setEt3CaseDetailsLinksStatuses(RespondentUtil.generateDefaultET3CaseDetailsLinksStatuses());
        }
        switch (sectionId) {
            case CASE_DETAILS_SECTION_PERSONAL_DETAILS:
                respondent.getEt3CaseDetailsLinksStatuses().setPersonalDetails(getSectionValue(
                    respondent.getEt3CaseDetailsLinksStatuses().getPersonalDetails(), sectionStatus));
                break;
            case CASE_DETAILS_SECTION_ET1_CLAIM_FORM:
                respondent.getEt3CaseDetailsLinksStatuses().setEt1ClaimForm(getSectionValue(
                    respondent.getEt3CaseDetailsLinksStatuses().getEt1ClaimForm(), sectionStatus));
                break;
            case CASE_DETAILS_SECTION_RESPONDENT_RESPONSE:
                respondent.getEt3CaseDetailsLinksStatuses().setRespondentResponse(getSectionValue(
                    respondent.getEt3CaseDetailsLinksStatuses().getRespondentResponse(), sectionStatus));
                break;
            case CASE_DETAILS_SECTION_HEARING_DETAILS:
                respondent.getEt3CaseDetailsLinksStatuses().setHearingDetails(getSectionValue(
                    respondent.getEt3CaseDetailsLinksStatuses().getHearingDetails(), sectionStatus));
                break;
            case CASE_DETAILS_SECTION_RESPONDENT_REQUESTS_AND_APPLICATIONS:
                respondent.getEt3CaseDetailsLinksStatuses().setRespondentRequestsAndApplications(getSectionValue(
                    respondent.getEt3CaseDetailsLinksStatuses().getRespondentRequestsAndApplications(), sectionStatus));
                break;
            default: setRemainingCaseDetailsLinkStatus(respondent, sectionId, sectionStatus);
        }
    }

    // For not to have cyclomatic complexity - PMD error
    private static void setRemainingCaseDetailsLinkStatus(RespondentSumType respondent,
                                                          String sectionId,
                                                          String sectionStatus) {
        switch (sectionId) {
            case CASE_DETAILS_SECTION_CLAIMANT_APPLICATIONS ->
                respondent.getEt3CaseDetailsLinksStatuses().setClaimantApplications(getSectionValue(
                    respondent.getEt3CaseDetailsLinksStatuses().getClaimantApplications(), sectionStatus));
            case CASE_DETAILS_SECTION_OTHER_RESPONDENT_APPLICATIONS ->
                respondent.getEt3CaseDetailsLinksStatuses().setOtherRespondentApplications(getSectionValue(
                    respondent.getEt3CaseDetailsLinksStatuses().getOtherRespondentApplications(), sectionStatus));
            case CASE_DETAILS_SECTION_CONTACT_TRIBUNAL ->
                respondent.getEt3CaseDetailsLinksStatuses().setContactTribunal(getSectionValue(
                    respondent.getEt3CaseDetailsLinksStatuses().getContactTribunal(), sectionStatus));
            case CASE_DETAILS_SECTION_TRIBUNAL_ORDERS ->
                respondent.getEt3CaseDetailsLinksStatuses().setTribunalOrders(getSectionValue(
                    respondent.getEt3CaseDetailsLinksStatuses().getTribunalOrders(), sectionStatus));
            case CASE_DETAILS_SECTION_TRIBUNAL_JUDGEMENTS ->
                respondent.getEt3CaseDetailsLinksStatuses().setTribunalJudgements(getSectionValue(
                    respondent.getEt3CaseDetailsLinksStatuses().getTribunalJudgements(), sectionStatus));
            case CASE_DETAILS_SECTION_DOCUMENTS ->
                respondent.getEt3CaseDetailsLinksStatuses().setDocuments(getSectionValue(
                    respondent.getEt3CaseDetailsLinksStatuses().getDocuments(), sectionStatus));
            default -> log.info(INVALID_CASE_DETAILS_SECTION_ID);
        }
    }

    private static String getSectionValue(String existingValue, String newValue) {
        if (!SECTION_STATUS_COMPLETED.equals(existingValue)) {
            return newValue;
        }
        return existingValue;
    }
}
