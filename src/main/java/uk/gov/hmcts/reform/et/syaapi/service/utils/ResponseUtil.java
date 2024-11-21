package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.et3links.ET3HubLinksStatuses;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.constants.CaseDetailsLinks;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants;
import uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants;
import uk.gov.hmcts.reform.et.syaapi.constants.ResponseHubLinks;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.exception.ET3Exception;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_CANNOT_START_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_NOT_STARTED_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_SUBMIT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.SECTION_STATUS_COMPLETED;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.SUBMIT_ET3_FORM;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_ET3_FORM;

public final class ResponseUtil {

    private ResponseUtil() {
        // restrict instantiation
    }

    public static void checkModifyEt3DataParameters(String authorisation, Et3Request et3Request) {
        if (StringUtils.isBlank(authorisation)) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_AUTHORISATION_TOKEN_BLANK));
        }
        checkEt3Request(et3Request);
        checkEt3Respondent(et3Request.getRespondent());
    }

    private static void checkEt3Request(Et3Request et3Request) {
        if (ObjectUtils.isEmpty(et3Request)) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_REQUEST_EMPTY));
        }
        if (StringUtils.isBlank(et3Request.getCaseSubmissionReference())) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_SUBMISSION_REFERENCE_BLANK));
        }
        if (StringUtils.isBlank(et3Request.getCaseTypeId())) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_CASE_TYPE_BLANK));
        }
        if (StringUtils.isBlank(et3Request.getRequestType())) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_REQUEST_TYPE_BLANK));
        }
    }

    private static void checkEt3Respondent(RespondentSumTypeItem et3RespondentSumTypeItem) {
        if (ObjectUtils.isEmpty(et3RespondentSumTypeItem)) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_RESPONDENT_EMPTY));
        }
        if (StringUtils.isBlank(et3RespondentSumTypeItem.getId())) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_RESPONDENT_CCD_ID_IS_BLANK));
        }
        if (StringUtils.isBlank(et3RespondentSumTypeItem.getValue().getIdamId())) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_RESPONDENT_IDAM_ID_IS_BLANK));
        }
    }

    public static RespondentSumTypeItem findSelectedRespondentByRespondentSumTypeItem(
        CaseData caseData, RespondentSumTypeItem et3Respondent) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_RESPONDENT_COLLECTION_IS_EMPTY));
        }
        if (StringUtils.isBlank(et3Respondent.getId())) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_ET3_RESPONDENT_CCD_ID_IS_BLANK));
        }
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            if (et3Respondent.getValue().getIdamId().equals(respondentSumTypeItem.getValue().getIdamId())
                && et3Respondent.getId().equals(respondentSumTypeItem.getId())) {
                return respondentSumTypeItem;
            }
        }
        throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_RESPONDENT_NOT_FOUND));
    }

    public static String getResponseHubCheckYourAnswersStatus(ET3HubLinksStatuses et3HubLinksStatuses) {
        if (ObjectUtils.isEmpty(et3HubLinksStatuses)) {
            return LINK_STATUS_CANNOT_START_YET;
        }
        if (isET3HubLinkStatusCompleted(et3HubLinksStatuses.getContactDetails())
            && isET3HubLinkStatusCompleted(et3HubLinksStatuses.getContestClaim())
            && isET3HubLinkStatusCompleted(et3HubLinksStatuses.getEmployerDetails())
            && isET3HubLinkStatusCompleted(et3HubLinksStatuses.getPayPensionBenefitDetails())
            && isET3HubLinkStatusCompleted(et3HubLinksStatuses.getConciliationAndEmployeeDetails())
            && isET3HubLinkStatusCompleted(et3HubLinksStatuses.getEmployersContractClaim())) {
            return LINK_STATUS_NOT_STARTED_YET;
        }
        return LINK_STATUS_CANNOT_START_YET;
    }

    private static boolean isET3HubLinkStatusCompleted(String status) {
        return StringUtils.isNotBlank(status) && SECTION_STATUS_COMPLETED.equals(status);
    }

    public static void setLinkStatuses(RespondentSumType respondentSumType, Et3Request et3Request) {
        ResponseHubLinks.setResponseHubLinkStatus(
            respondentSumType,
            et3Request.getResponseHubLinksSectionId(),
            et3Request.getResponseHubLinksSectionStatus());
        CaseDetailsLinks.setCaseDetailsLinkStatus(
            respondentSumType,
            et3Request.getCaseDetailsLinksSectionId(),
            et3Request.getCaseDetailsLinksSectionStatus());
        respondentSumType.getEt3HubLinksStatuses()
            .setCheckYorAnswers(getResponseHubCheckYourAnswersStatus(respondentSumType.getEt3HubLinksStatuses()));
    }

    public static CaseEvent findCaseEventByRequestType(String requestType) {
        return MODIFICATION_TYPE_SUBMIT.equals(requestType) ? SUBMIT_ET3_FORM : UPDATE_ET3_FORM;
    }

    public static CaseDetails getCaseDetailsByStartEventResponse(StartEventResponse startEventResponse) {
        if (ObjectUtils.isEmpty(startEventResponse) || ObjectUtils.isEmpty(startEventResponse.getCaseDetails())) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_UNABLE_TO_FIND_CASE_DETAILS));
        }
        return startEventResponse.getCaseDetails();
    }

    public static void setET3SubmitValues(RespondentSumType respondentSumType) {
        respondentSumType.setEt3Status(ManageCaseRoleConstants.RESPONSE_STATUS_COMPLETED);
        respondentSumType.getEt3HubLinksStatuses().setCheckYorAnswers(
            ManageCaseRoleConstants.RESPONSE_STATUS_COMPLETED);
        respondentSumType.setResponseReceived(EtSyaConstants.YES);
        respondentSumType.setResponseReceivedDate(LocalDate.now().toString());
    }
}
