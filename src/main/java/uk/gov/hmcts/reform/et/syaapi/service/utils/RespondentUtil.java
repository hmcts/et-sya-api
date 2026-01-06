package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignedUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.et3links.ET3CaseDetailsLinksStatuses;
import uk.gov.hmcts.et.common.model.ccd.types.et3links.ET3HubLinksStatuses;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.ET3_STATUS_IN_PROGRESS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_EMPTY_RESPONDENT_COLLECTION_NOT_ABLE_TO_ADD_RESPONDENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_IDAM_ID_ALREADY_EXISTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_IDAM_ID_ALREADY_EXISTS_SAME_USER;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_INVALID_IDAM_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_INVALID_RESPONDENT_INDEX;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_NO_RESPONDENT_DEFINED;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_RESPONDENT_NOT_EXISTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_RESPONDENT_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_RESPONDENT_NOT_FOUND_WITH_INDEX;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_CANNOT_START_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_NOT_AVAILABLE_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_NOT_STARTED_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_NOT_VIEWED_YET;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.LINK_STATUS_OPTIONAL;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_REVOKE;

@Slf4j
public final class RespondentUtil {

    private RespondentUtil() {
        // restrict instantiation
    }

    /**
     * Finds respondent{@link RespondentSumType} in the respondent collection of caseDetails{@link CaseDetails}
     * by using respondent name and assigns idam id to the respondent. It is assumed that caseDetails, respondentName,
     * and idamId is not empty or blank.
     *
     * @param caseDetails    object received by elastic search.
     * @param respondentName name of the respondent to search in respondent collection.
     * @param idamId         to be assigned to the respondent in the respondent collection.
     * @param userEmail      email of the user performing the operation.
     */
    public static void setRespondentIdamIdAndDefaultLinkStatuses(CaseDetails caseDetails,
                                                                 String respondentName,
                                                                 String idamId,
                                                                 String modificationType,
                                                                 String userEmail) {
        Map<String, Object> existingCaseData = caseDetails.getData();
        if (MapUtils.isEmpty(existingCaseData)) {
            throw new RuntimeException(String.format(EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA, caseDetails.getId()));
        }
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(existingCaseData);
        if (CollectionUtils.isNotEmpty(caseData.getRespondentCollection())) {
            List<RespondentSumTypeItem> respondentSumTypeItems =
                findRespondentSumTypeItems(caseData.getRespondentCollection(),
                                           modificationType,
                                           respondentName,
                                           idamId);
            for (RespondentSumTypeItem respondentSumTypeItem : respondentSumTypeItems) {
                setRespondentIdAndLinkStatuses(respondentSumTypeItem,
                                               idamId,
                                               caseDetails.getId().toString(),
                                               modificationType,
                                               userEmail);
            }
            Map<String, Object> updatedCaseData = EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData);
            caseDetails.setData(updatedCaseData);
            return;

        }
        throw new ManageCaseRoleException(new Exception(String.format(
            EXCEPTION_EMPTY_RESPONDENT_COLLECTION_NOT_ABLE_TO_ADD_RESPONDENT,
            caseDetails.getId())));
    }

    private static List<RespondentSumTypeItem> findRespondentSumTypeItems(
        List<RespondentSumTypeItem> respondentCollection,
        String modificationType,
        String respondentName,
        String idamId) {
        List<RespondentSumTypeItem> respondentSumTypeItems = new ArrayList<>();
        for (RespondentSumTypeItem respondentSumTypeItem : respondentCollection) {
            RespondentSumTypeItem tmpRespondentSumTypeItem =
                getRespondentSumTypeByModificationType(respondentSumTypeItem,
                                                       modificationType,
                                                       respondentName,
                                                       idamId);
            if (ObjectUtils.isNotEmpty(tmpRespondentSumTypeItem)) {
                respondentSumTypeItems.add(tmpRespondentSumTypeItem);
            }
        }
        if (CollectionUtils.isNotEmpty(respondentSumTypeItems)) {
            return respondentSumTypeItems;
        }
        throw new RuntimeException(String.format(EXCEPTION_RESPONDENT_NOT_FOUND, respondentName));
    }

    private static RespondentSumTypeItem getRespondentSumTypeByModificationType(
        RespondentSumTypeItem respondentSumTypeItem,
        String modificationType,
        String respondentName,
        String idamId) {
        if (ObjectUtils.isNotEmpty(respondentSumTypeItem)
            && ObjectUtils.isNotEmpty(respondentSumTypeItem.getValue())) {
            if (MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)
                && StringUtils.isNotBlank(respondentName)
                && checkRespondentName(respondentSumTypeItem.getValue(), respondentName)) {
                return respondentSumTypeItem;
            } else if (MODIFICATION_TYPE_REVOKE.equals(modificationType)
                && StringUtils.isNotBlank(respondentSumTypeItem.getValue().getIdamId())
                && idamId.equals(respondentSumTypeItem.getValue().getIdamId())) {
                return respondentSumTypeItem;
            }
        }
        return null;
    }

    private static boolean checkRespondentName(RespondentSumType respondentSumType, String respondentName) {
        if (respondentName.equalsIgnoreCase(respondentSumType.getRespondentName())
            || respondentName.equalsIgnoreCase(respondentSumType.getRespondentOrganisation())) {
            return true;
        }
        return respondentName.equalsIgnoreCase(generateRespondentNameByRespondentFirstNameAndLastName(
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

    private static void setRespondentIdAndLinkStatuses(RespondentSumTypeItem respondentSumTypeItem,
                                                       String idamId,
                                                       String submissionReference,
                                                       String modificationType,
                                                       String userEmail) {
        if (StringUtils.isBlank(idamId)) {
            throw new RuntimeException(EXCEPTION_INVALID_IDAM_ID);
        }
        if (MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)
            && StringUtils.isNotBlank(respondentSumTypeItem.getValue().getIdamId())) {
            if (idamId.equals(respondentSumTypeItem.getValue().getIdamId())) {
                throw new RuntimeException(String.format(EXCEPTION_IDAM_ID_ALREADY_EXISTS_SAME_USER,
                                                         submissionReference));
            }
            throw new RuntimeException(String.format(EXCEPTION_IDAM_ID_ALREADY_EXISTS, submissionReference));
        }
        if (MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType)) {
            respondentSumTypeItem.getValue().setIdamId(idamId);
            if (isBlank(respondentSumTypeItem.getValue().getResponseRespondentEmail())) {
                respondentSumTypeItem.getValue().setResponseRespondentEmail(userEmail);
            }
            if (ObjectUtils.isEmpty(respondentSumTypeItem.getValue().getEt3CaseDetailsLinksStatuses())) {
                respondentSumTypeItem.getValue()
                    .setEt3CaseDetailsLinksStatuses(generateDefaultET3CaseDetailsLinksStatuses());
            }
            if (ObjectUtils.isEmpty(respondentSumTypeItem.getValue().getEt3HubLinksStatuses())) {
                respondentSumTypeItem.getValue().setEt3HubLinksStatuses(generateDefaultET3HubLinksStatuses());
            }
            if (ObjectUtils.isEmpty(respondentSumTypeItem.getValue().getEt3Status())) {
                respondentSumTypeItem.getValue().setEt3Status(ET3_STATUS_IN_PROGRESS);
            }
        } else {
            respondentSumTypeItem.getValue().setIdamId(StringUtils.EMPTY);
        }
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


    public static boolean checkIsUserCreator(CaseAssignedUserRolesResponse caseAssignedUserRolesResponse) {
        if (ObjectUtils.isEmpty(caseAssignedUserRolesResponse)
            || CollectionUtils.isEmpty(caseAssignedUserRolesResponse.getCaseAssignedUserRoles())) {
            return false;
        }
        for (CaseAssignmentUserRole caseAssignmentUserRole : caseAssignedUserRolesResponse.getCaseAssignedUserRoles()) {
            if (CASE_USER_ROLE_CREATOR.equals(caseAssignmentUserRole.getCaseRole())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves a {@link RespondentSumTypeItem} from the provided list based on the specified respondent index.
     *
     * <p>
     * The {@code respondentIndex} must be a string representation of an integer. This index is used to access
     * the corresponding item in the {@code respondentSumTypeItems} list. The method ensures that:
     * <ul>
     *   <li>The list is not null or empty</li>
     *   <li>The index is a valid integer and within list bounds</li>
     *   <li>The item at the given index and its value are not null or empty</li>
     * </ul>
     * If any of these validations fail, a {@link ManageCaseRoleException} is thrown with a context-specific message.
     * </p>
     *
     * <p>
     * The {@code caseId} is used in exception messages to provide context for logging and debugging.
     * </p>
     *
     * @param respondentSumTypeItems the list of respondent items to retrieve from
     * @param respondentIndex the index (as a string) of the desired respondent in the list
     * @param caseId the case identifier used for error context
     * @return the {@code RespondentSumTypeItem} at the specified index
     * @throws ManageCaseRoleException if:
     *         <ul>
     *           <li>the list is null or empty</li>
     *           <li>the index is not a valid integer</li>
     *           <li>the index is out of bounds</li>
     *           <li>the item or its value at the index is null or empty</li>
     *         </ul>
     */
    public static RespondentSumTypeItem findRespondentSumTypeItemByIndex(
        List<RespondentSumTypeItem> respondentSumTypeItems, String respondentIndex, String caseId) {
        try {
            if (CollectionUtils.isEmpty(respondentSumTypeItems)) {
                throw new ManageCaseRoleException(new Exception(
                    String.format(EXCEPTION_NO_RESPONDENT_DEFINED, caseId)));
            }
            int index = Integer.parseInt(respondentIndex);
            RespondentSumTypeItem respondentSumTypeItem = respondentSumTypeItems.get(index);
            if (ObjectUtils.isEmpty(respondentSumTypeItem)
                || ObjectUtils.isEmpty(respondentSumTypeItem.getValue())) {
                throw new ManageCaseRoleException(new Exception(
                    String.format(EXCEPTION_RESPONDENT_NOT_FOUND_WITH_INDEX, respondentIndex)));
            }
            return respondentSumTypeItem;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new ManageCaseRoleException(
                new Exception(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX, respondentIndex, caseId)));
        }
    }

    /**
     * Finds and returns the {@link RepresentedTypeRItem} (i.e., the representative) associated with the given
     * {@link RespondentSumTypeItem} from the provided collection of representatives.
     *
     * <p>
     * The method matches the respondent by comparing the respondent's ID (from {@code respondentSumTypeItem})
     * with the {@code respondentId} field of each {@link RepresentedTypeRItem}'s value.
     * </p>
     *
     * <p>
     * Several validations are performed:
     * <ul>
     *   <li>Checks if the provided {@code respondentSumTypeItem} is not null or empty.</li>
     *   <li>Ensures the {@code representativeCollection} is not null or empty.</li>
     *   <li>Iterates through the collection to find a non-empty representative whose {@code respondentId} matches the
     *   respondent’s ID.</li>
     * </ul>
     * If no match is found, or if input validations fail, a {@link ManageCaseRoleException} is thrown with a
     * context-specific error message.
     * </p>
     *
     * @param respondentSumTypeItem the respondent item whose representative is to be found
     * @param representativeCollection the list of potential respondent representatives
     * @param caseId the case identifier used for contextual exception messages
     * @return the {@link RepresentedTypeRItem} that represents the provided respondent
     * @throws ManageCaseRoleException if:
     *         <ul>
     *           <li>{@code respondentSumTypeItem} is null or empty</li>
     *           <li>{@code representativeCollection} is null or empty</li>
     *           <li>no matching representative is found for the respondent</li>
     *         </ul>
     */
    public static RepresentedTypeRItem findRespondentRepresentative(RespondentSumTypeItem respondentSumTypeItem,
                                                                    List<RepresentedTypeRItem> representativeCollection,
                                                                    String caseId) {
        if (ObjectUtils.isEmpty(respondentSumTypeItem)) {
            throw new ManageCaseRoleException(new Exception(
                String.format(EXCEPTION_RESPONDENT_NOT_EXISTS, caseId)));
        }
        if (CollectionUtils.isEmpty(representativeCollection)) {
            throw new ManageCaseRoleException(new Exception(
                String.format(EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND, caseId)));
        }
        for (RepresentedTypeRItem representativeType : representativeCollection) {
            if (ObjectUtils.isNotEmpty(representativeType)
                && ObjectUtils.isNotEmpty(representativeType.getValue())
                && StringUtils.isNotBlank(representativeType.getValue().getRespondentId())
                && representativeType.getValue().getRespondentId()
                .equals(respondentSumTypeItem.getId())) {
                return representativeType;
            }
        }
        throw new ManageCaseRoleException(new Exception(
            String.format(EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND, caseId)));
    }
}
