package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.et3links.ET3CaseDetailsLinksStatuses;
import uk.gov.hmcts.et.common.model.ccd.types.et3links.ET3HubLinksStatuses;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_EMPTY_RESPONDENT_COLLECTION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_IDAM_ID_ALREADY_EXISTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_INVALID_IDAM_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_RESPONDENT_NOT_FOUND_WITH_RESPONDENT_NAME;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_CANNOT_START_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_NOT_AVAILABLE_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_NOT_STARTED_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_NOT_VIEWED_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_OPTIONAL;

@Slf4j
public final class RespondentUtil {

    private RespondentUtil() {
        // restrict instantiation
    }

    /**
     * Finds respondent{@link RespondentSumType} in the respondent collection of caseDetails{@link CaseDetails}
     * by using respondent name and assigns idam id to the respondent. It is assumed that caseDetails, respondentName,
     * and idamId is not empty or blank.
     * @param caseDetails object received by elastic search.
     * @param respondentName name of the respondent to search in respondent collection.
     * @param idamId to be assigned to the respondent in the respondent collection.
     */
    public static void setRespondentIdamIdDefaultLinkStatuses(CaseDetails caseDetails,
                                                              String respondentName,
                                                              String idamId) {
        Map<String, Object> existingCaseData = caseDetails.getData();
        if (MapUtils.isEmpty(existingCaseData)) {
            throw new RuntimeException(String.format(EXCEPTION_CASE_DATA_NOT_FOUND, caseDetails.getId()));
        }
        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(existingCaseData);
        if (CollectionUtils.isNotEmpty(caseData.getRespondentCollection())) {
            RespondentSumTypeItem respondentSumTypeItem =
                findRespondentSumTypeItemByRespondentName(caseData.getRespondentCollection(), respondentName);
            setRespondentIdLinkStatuses(respondentSumTypeItem, idamId, respondentName, caseDetails.getId().toString());
            Map<String, Object> updatedCaseData = EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData);
            caseDetails.setData(updatedCaseData);
            return;

        }
        throw new RuntimeException(new Exception(String.format(EXCEPTION_EMPTY_RESPONDENT_COLLECTION,
                                                                caseDetails.getId())));
    }

    private static RespondentSumTypeItem findRespondentSumTypeItemByRespondentName(
        List<RespondentSumTypeItem> respondentCollection, String respondentName) {
        for (RespondentSumTypeItem respondentSumTypeItem : respondentCollection) {
            if (ObjectUtils.isNotEmpty(respondentSumTypeItem)
                && ObjectUtils.isNotEmpty(respondentSumTypeItem.getValue())
                && StringUtils.isNotBlank(respondentName)
                && checkRespondentName(respondentSumTypeItem.getValue(), respondentName)) {
                return respondentSumTypeItem;
            }
        }
        throw new RuntimeException(String.format(EXCEPTION_RESPONDENT_NOT_FOUND_WITH_RESPONDENT_NAME,  respondentName));
    }

    private static boolean checkRespondentName(RespondentSumType respondentSumType, String respondentName) {
        if (respondentName.equals(respondentSumType.getRespondentName())
            || respondentName.equals(respondentSumType.getRespondentOrganisation())) {
            return true;
        }
        return respondentName.equals(generateRespondentNameByRespondentFirstNameAndLastName(
            respondentSumType.getRespondentFirstName(), respondentSumType.getRespondentLastName()));
    }

    private static String generateRespondentNameByRespondentFirstNameAndLastName(String respondentFirstName,
                                                                                 String respondentLastName) {
        if (StringUtils.isNotBlank(respondentFirstName) && StringUtils.isNotBlank(respondentLastName)) {
            return respondentFirstName + StringUtils.SPACE + respondentLastName;
        } else if (StringUtils.isNotBlank(respondentFirstName)) {
            return respondentFirstName;
        } else if (StringUtils.isNotBlank(respondentLastName)) {
            return respondentLastName;
        }
        return StringUtils.EMPTY;
    }

    private static void setRespondentIdLinkStatuses(RespondentSumTypeItem respondentSumTypeItem,
                                        String idamId,
                                        String respondentName,
                                        String submissionReference) {
        if (StringUtils.isBlank(idamId)) {
            throw new RuntimeException(EXCEPTION_INVALID_IDAM_ID);
        }
        if (StringUtils.isNotBlank(respondentSumTypeItem.getValue().getIdamId())) {
            throw new RuntimeException(String.format(EXCEPTION_IDAM_ID_ALREADY_EXISTS,
                                                     respondentName,
                                                     submissionReference));
        }
        respondentSumTypeItem.getValue().setIdamId(idamId);
        respondentSumTypeItem.getValue().setEt3CaseDetailsLinksStatuses(generateDefaultET3CaseDetailsLinksStatuses());
        respondentSumTypeItem.getValue().setEt3HubLinksStatuses(generateDefaultET3HubLinksStatuses());
    }

    private static ET3CaseDetailsLinksStatuses generateDefaultET3CaseDetailsLinksStatuses() {
        ET3CaseDetailsLinksStatuses et3CaseDetailsLinksStatuses = new ET3CaseDetailsLinksStatuses();
        et3CaseDetailsLinksStatuses.setPersonalDetails(LINK_STATUS_NOT_AVAILABLE_YET);
        et3CaseDetailsLinksStatuses.setEt1ClaimForm(LINK_STATUS_NOT_VIEWED_YET);
        et3CaseDetailsLinksStatuses.setRespondentResponse(LINK_STATUS_NOT_STARTED_YET);
        et3CaseDetailsLinksStatuses.setHearingDetails(LINK_STATUS_NOT_AVAILABLE_YET);
        et3CaseDetailsLinksStatuses.setRespondentRequestsAndApplications(LINK_STATUS_NOT_AVAILABLE_YET);
        et3CaseDetailsLinksStatuses.setClaimantApplications(LINK_STATUS_NOT_AVAILABLE_YET);
        et3CaseDetailsLinksStatuses.setContactTribunal(LINK_STATUS_OPTIONAL);
        et3CaseDetailsLinksStatuses.setTribunalOrders(LINK_STATUS_NOT_AVAILABLE_YET);
        et3CaseDetailsLinksStatuses.setTribunalJudgements(LINK_STATUS_NOT_AVAILABLE_YET);
        et3CaseDetailsLinksStatuses.setDocuments(LINK_STATUS_OPTIONAL);
        return et3CaseDetailsLinksStatuses;
    }

    private static ET3HubLinksStatuses generateDefaultET3HubLinksStatuses() {
        ET3HubLinksStatuses et3HubLinksStatuses = new ET3HubLinksStatuses();
        et3HubLinksStatuses.setContactDetails(LINK_STATUS_NOT_STARTED_YET);
        et3HubLinksStatuses.setEmployerDetails(LINK_STATUS_NOT_STARTED_YET);
        et3HubLinksStatuses.setConciliationAndEmployeeDetails(LINK_STATUS_NOT_STARTED_YET);
        et3HubLinksStatuses.setPayPensionBenefitDetails(LINK_STATUS_NOT_STARTED_YET);
        et3HubLinksStatuses.setContestClaim(LINK_STATUS_NOT_STARTED_YET);
        et3HubLinksStatuses.setCheckYorAnswers(LINK_STATUS_CANNOT_START_YET);
        return et3HubLinksStatuses;
    }
}
