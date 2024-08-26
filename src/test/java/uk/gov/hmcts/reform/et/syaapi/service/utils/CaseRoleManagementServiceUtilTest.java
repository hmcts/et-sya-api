package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.NotifyUserCaseRoleModificationRequest;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.HEADER_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.HEADER_SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFICATION_TYPE_REVOKE;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFY_ROLE_NOTIFICATION_DETAILS;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFY_ROLE_NOTIFICATION_SUBJECT;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.buildHeaders;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.generateCaseDataByUserInfoCaseDetails;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.getHttpMethodByModificationType;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.getNextNotificationNumber;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.getNotificationDetails;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.getNotificationSubjectList;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.getRespondentFullNameByUserInfo;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.DUMMY_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.DUMMY_CASE_SUBMISSION_REFERENCE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.DUMMY_SERVICE_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.DUMMY_USER_ID;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.JACKSON;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MICHAEL;

class CaseRoleManagementServiceUtilTest {

    private NotifyUserCaseRoleModificationRequest notifyUserCaseRoleModificationRequest;
    private UserInfo userInfo;

    @BeforeEach
    void setUp() {
        userInfo = new CaseTestData().getUserInfo();
        notifyUserCaseRoleModificationRequest = NotifyUserCaseRoleModificationRequest.builder()
            .role("[DEFENDANT]")
            .caseSubmissionReference(DUMMY_CASE_SUBMISSION_REFERENCE)
            .modificationType(MODIFICATION_TYPE_ASSIGNMENT)
            .caseType(ENGLAND_CASE_TYPE)
            .build();
    }

    @Test
    void theGetNotificationDetails() {
        assertThat(getNotificationDetails(userInfo, notifyUserCaseRoleModificationRequest)).isEqualTo(
            String.format(MODIFY_ROLE_NOTIFICATION_DETAILS,
                          notifyUserCaseRoleModificationRequest.getModificationType(),
                          notifyUserCaseRoleModificationRequest.getRole(),
                          userInfo.getUid(),
                          notifyUserCaseRoleModificationRequest.getCaseSubmissionReference()));
    }

    @Test
    void theGetNotificationSubjectList() {
        assertThat(getNotificationSubjectList(userInfo, notifyUserCaseRoleModificationRequest)).isEqualTo(
            List.of(String.format(MODIFY_ROLE_NOTIFICATION_SUBJECT,
                                  notifyUserCaseRoleModificationRequest.getRole(),
                                  userInfo.getUid(),
                                  notifyUserCaseRoleModificationRequest.getCaseSubmissionReference())));
    }

    @ParameterizedTest
    @MethodSource("generateUserInfoTestArguments")
    void theGetRespondentFullNameByUserInfo(UserInfo userInfo, String expectedFullName) {
        assertThat(getRespondentFullNameByUserInfo(userInfo)).isEqualTo(expectedFullName);
    }

    private static Stream<Arguments> generateUserInfoTestArguments() {
        UserInfo userInfoWithGivenName = new CaseTestData().getUserInfo();
        UserInfo userInfoWithName = new UserInfo(
            null, DUMMY_USER_ID, MICHAEL, null, JACKSON, null);
        UserInfo userInfoWithOutName = new UserInfo(
            null, DUMMY_USER_ID, null, null, JACKSON, null);
        UserInfo userInfoWithOutFamilyName = new UserInfo(
            null, DUMMY_USER_ID, MICHAEL, null, null, null);
        return Stream.of(
            Arguments.of(userInfoWithGivenName,
                         userInfoWithGivenName.getGivenName()
                             + StringUtils.SPACE
                             + userInfoWithGivenName.getFamilyName()),
            Arguments.of(userInfoWithName,
                         userInfoWithName.getName()
                             + StringUtils.SPACE
                             + userInfoWithName.getFamilyName()),
            Arguments.of(userInfoWithOutName, userInfoWithOutName.getFamilyName()),
            Arguments.of(userInfoWithOutFamilyName, userInfoWithOutFamilyName.getName()));
    }

    @ParameterizedTest
    @MethodSource("generateCaseDataTestArguments")
    void theGetNextNotificationNumber(CaseData caseData, int expectedNotificationNumber) {
        assertThat(getNextNotificationNumber(caseData)).isEqualTo(expectedNotificationNumber);
    }

    private static Stream<Arguments> generateCaseDataTestArguments() {
        CaseData caseDataNullSendNotificationCollection = new TestData().getCaseData();
        caseDataNullSendNotificationCollection.setSendNotificationCollection(null);
        CaseData caseDataEmptySendNotificationCollection = new TestData().getCaseData();
        caseDataEmptySendNotificationCollection.setSendNotificationCollection(new ArrayList<>());
        CaseData caseDataHasOneSendNotificationCollectionItem = new TestData().getCaseData();
        caseDataHasOneSendNotificationCollectionItem.setSendNotificationCollection(List.of(
            new SendNotificationTypeItem()
        ));
        return Stream.of(
            Arguments.of(caseDataNullSendNotificationCollection, 1),
            Arguments.of(caseDataEmptySendNotificationCollection, 1),
            Arguments.of(caseDataHasOneSendNotificationCollectionItem, 2));
    }

    @Test
    @SneakyThrows
    void theBuildHeaders() {
        HttpHeaders httpHeaders = buildHeaders(DUMMY_AUTHORISATION_TOKEN, DUMMY_SERVICE_AUTHORISATION_TOKEN);
        assertThat(httpHeaders.get(HEADER_AUTHORIZATION)).contains(DUMMY_AUTHORISATION_TOKEN);
        assertThat(httpHeaders.get(HEADER_SERVICE_AUTHORIZATION)).contains(DUMMY_SERVICE_AUTHORISATION_TOKEN);
        assertThrows(IOException.class,
                     () -> buildHeaders(StringUtils.EMPTY, DUMMY_SERVICE_AUTHORISATION_TOKEN));
    }

    @Test
    @SneakyThrows
    void theGenerateCaseDataByUserInfoCaseDetails() {
        SendNotificationTypeItem sendNotificationTypeItem = new SendNotificationTypeItem();
        SendNotificationType sendNotificationType = new SendNotificationType();
        sendNotificationType.setNumber("1");
        sendNotificationTypeItem.setId("1");
        sendNotificationTypeItem.setValue(sendNotificationType);
        CaseDetails caseDetails = new TestData().getCaseDetailsWithData();
        caseDetails.getData().put("sendNotificationCollection", List.of(sendNotificationTypeItem));
        assertThat(
            generateCaseDataByUserInfoCaseDetails(
                userInfo,
                new TestData().getCaseDetailsWithData(),
                notifyUserCaseRoleModificationRequest).getSendNotificationCollection()).hasSize(1);
        List<SendNotificationTypeItem> sendNotificationTypeItems =
            generateCaseDataByUserInfoCaseDetails(userInfo, caseDetails, notifyUserCaseRoleModificationRequest)
                .getSendNotificationCollection();
        assertThat(sendNotificationTypeItems).hasSize(2);
        assertThat(sendNotificationTypeItems.get(0).getValue().getNumber()).isEqualTo("1");
        assertThat(sendNotificationTypeItems.get(1).getValue().getNumber()).isEqualTo("2");
    }

    @Test
    @SneakyThrows
    void theGetHttpMethodByModificationType() {
        assertThat(getHttpMethodByModificationType(MODIFICATION_TYPE_ASSIGNMENT)).isEqualTo(HttpMethod.POST);
        assertThat(getHttpMethodByModificationType(MODIFICATION_TYPE_REVOKE)).isEqualTo(HttpMethod.DELETE);
        assertThat(getHttpMethodByModificationType(null)).isEqualTo(null);
    }
}
