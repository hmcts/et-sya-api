package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.NotifyUserCaseRoleModificationRequest;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRIBUNAL;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.AUTHORISATION_TOKEN_REGEX;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.EXCEPTION_AUTHORISATION_TOKEN_REGEX;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.HEADER_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.HEADER_CONTENT_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.HEADER_SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.HEADER_VALUE_APPLICATION_JSON;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFICATION_TYPE_REVOKE;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFY_ROLE_NOTIFICATION_DETAILS;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFY_ROLE_NOTIFICATION_PARTY;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFY_ROLE_NOTIFICATION_SENT_FROM;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFY_ROLE_NOTIFICATION_SUBJECT;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFY_ROLE_NOTIFICATION_TITLE;

@Slf4j
public final class CaseRoleManagementServiceUtil {
    private CaseRoleManagementServiceUtil() {
        // restrict instantiation
    }

    /**
     * Returns modification subject by userInfo and notifyUserCaseRoleModificationRequest
     * which has case submission id, role and the modification type fields.
     * @param userInfo modified case role's user info
     * @param notifyUserCaseRoleModificationRequest notification request object which has submission id,
     *                                              role and notification type of the modified case user role
     * @return correspondent title for modified case user role
     */
    public static String getNotificationDetails(
        UserInfo userInfo, NotifyUserCaseRoleModificationRequest notifyUserCaseRoleModificationRequest) {
        return String.format(MODIFY_ROLE_NOTIFICATION_DETAILS,
                             notifyUserCaseRoleModificationRequest.getModificationType(),
                            notifyUserCaseRoleModificationRequest.getRole(),
                            userInfo.getUid(),
                            notifyUserCaseRoleModificationRequest.getCaseSubmissionReference());
    }

    /**
     * Returns modification subject by the given modification type with the given notifyUserCaseRoleModificationRequest
     * which has case submission id, role and the modification type fields.
     * @param userInfo modified case role's user info
     * @param notifyUserCaseRoleModificationRequest request object which has submission id, role and notification type
     * @return correspondent string title for modified case user role
     */
    public static List<String> getNotificationSubjectList(
        UserInfo userInfo, NotifyUserCaseRoleModificationRequest notifyUserCaseRoleModificationRequest) {
        return List.of(String.format(MODIFY_ROLE_NOTIFICATION_SUBJECT,
                                    notifyUserCaseRoleModificationRequest.getRole(),
                                    userInfo.getUid(),
                                    notifyUserCaseRoleModificationRequest.getCaseSubmissionReference()));
    }

    /**
     * returns user's full name by the given user info object which has name, given name family name attributes.
     * First checks for username if it exists puts username if not checks given name if it doesn't exist puts an
     * empty string for first name. For user's last name puts family name if exists. If not puts empty string for
     * the family name.
     * @param userInfo user info object that has name, family name and
     * @return user's full name
     */
    public static String getRespondentFullNameByUserInfo(UserInfo userInfo) {
        if (ObjectUtils.isEmpty(userInfo)) {
            return StringUtils.EMPTY;
        }
        return ((StringUtils.isNotBlank(userInfo.getName()) ? userInfo.getName()
            : StringUtils.isNotBlank(userInfo.getGivenName()) ? userInfo.getGivenName() : StringUtils.EMPTY)
            + StringUtils.SPACE
            + (StringUtils.isNotBlank(userInfo.getFamilyName()) ? userInfo.getFamilyName() : StringUtils.EMPTY)).trim();
    }

    /**
     * Gets notification number by the notification collection in case data. If there is no notification collection
     * returns 1 else returns number of elements in notification collection plus 1.
     * @param caseData case data to check notification collection size.
     * @return next available number to be used as notification id.
     */
    public static int getNextNotificationNumber(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getSendNotificationCollection())) {
            return 1;
        }
        return caseData.getSendNotificationCollection().size() + 1;
    }

    /**
     * Generates HttpHeaders with the given user and service authorisation tokens.
     * @param authToken authorisation token of the claimant
     * @param serviceAuthorisation service authorisation created by authorisation token generator
     * @return org.springframework.http.HttpsHeaders to call remote APIs
     * @throws IOException Thrown exception when authorisation token does not match with the authorisation token regex
     *                     which is [a-zA-Z0-9._\s\S]+$
     */
    public static HttpHeaders buildHeaders(String authToken, String serviceAuthorisation) throws IOException {
        if (authToken.matches(AUTHORISATION_TOKEN_REGEX)) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HEADER_AUTHORIZATION, authToken);
            headers.add(HEADER_SERVICE_AUTHORIZATION, serviceAuthorisation);
            headers.add(HEADER_CONTENT_TYPE, HEADER_VALUE_APPLICATION_JSON);
            return headers;
        } else {
            throw new IOException(EXCEPTION_AUTHORISATION_TOKEN_REGEX);
        }
    }

    /**
     * Generates case data with role modification notification by adding send notification type item to send
     * notification collection of the CaseData model.
     * @param userInfo user info generated by user token. Used to get notification subject list.
     * @param caseDetails case details received by case service with case submission id.
     * @param notifyUserCaseRoleModificationRequest details of the modification received from client. It has
     *                                              caseSubmissionReference, role, modificationType, caseType
     * @return CaseData that has modification notification info.
     */
    public static CaseData generateCaseDataByUserInfoCaseDetails(
        UserInfo userInfo,
        CaseDetails caseDetails,
        NotifyUserCaseRoleModificationRequest notifyUserCaseRoleModificationRequest) {

        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        if (CollectionUtils.isEmpty(caseData.getSendNotificationCollection())) {
            caseData.setSendNotificationCollection(new ArrayList<>());
        }
        caseData.setCcdID(notifyUserCaseRoleModificationRequest.getCaseSubmissionReference());
        SendNotificationType sendNotificationType = new SendNotificationType();
        sendNotificationType.setNumber(String.valueOf(getNextNotificationNumber(caseData)));
        sendNotificationType.setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        sendNotificationType.setSendNotificationTitle(MODIFY_ROLE_NOTIFICATION_TITLE);
        sendNotificationType.setSendNotificationLetter(StringUtils.EMPTY);
        sendNotificationType.setSendNotificationUploadDocument(null);
        sendNotificationType.setSendNotificationSubject(
            getNotificationSubjectList(userInfo, notifyUserCaseRoleModificationRequest));
        sendNotificationType.setSendNotificationAdditionalInfo(
            notifyUserCaseRoleModificationRequest.getModificationType());
        sendNotificationType.setSendNotificationNotify(StringUtils.EMPTY);
        sendNotificationType.setSendNotificationSelectHearing(null);
        sendNotificationType.setSendNotificationCaseManagement(StringUtils.EMPTY);
        sendNotificationType.setSendNotificationResponseTribunal(StringUtils.EMPTY);
        sendNotificationType.setSendNotificationWhoCaseOrder(StringUtils.EMPTY);
        sendNotificationType.setSendNotificationSelectParties(MODIFY_ROLE_NOTIFICATION_PARTY);
        sendNotificationType.setSendNotificationFullName(getRespondentFullNameByUserInfo(userInfo));
        sendNotificationType.setSendNotificationFullName2(userInfo.getGivenName());
        sendNotificationType.setSendNotificationDecision(StringUtils.EMPTY);
        sendNotificationType.setSendNotificationDetails(
            getNotificationDetails(userInfo, notifyUserCaseRoleModificationRequest));
        sendNotificationType.setSendNotificationRequestMadeBy(getRespondentFullNameByUserInfo(userInfo));
        sendNotificationType.setSendNotificationEccQuestion(StringUtils.EMPTY);
        sendNotificationType.setSendNotificationWhoMadeJudgement(StringUtils.EMPTY);
        sendNotificationType.setNotificationSentFrom(MODIFY_ROLE_NOTIFICATION_SENT_FROM);
        sendNotificationType.setNotificationState(NOT_VIEWED_YET);
        sendNotificationType.setSendNotificationSentBy(TRIBUNAL);
        sendNotificationType.setSendNotificationSubjectString(MODIFY_ROLE_NOTIFICATION_SUBJECT);
        sendNotificationType.setSendNotificationResponsesCount("0");
        sendNotificationType.setSendNotificationResponseTribunalTable(NO);
        SendNotificationTypeItem sendNotificationTypeItem = new SendNotificationTypeItem();
        sendNotificationTypeItem.setId(UUID.randomUUID().toString());
        sendNotificationTypeItem.setValue(sendNotificationType);
        caseData.getSendNotificationCollection().add(sendNotificationTypeItem);
        return caseData;
    }

    /**
     * Returns HttpMethod by the given modification type. If modification type is Assignment then returns
     * HttpMethod POST else returns HttpMethod DELETE.
     * @param modificationType modification type received from client.
     * @return HttpMethod type by the given modification type
     */
    public static HttpMethod getHttpMethodByModificationType(String modificationType) {
        return MODIFICATION_TYPE_ASSIGNMENT.equals(modificationType) ? HttpMethod.POST
            : MODIFICATION_TYPE_REVOKE.equals(modificationType) ? HttpMethod.DELETE
            : null;
    }
}
