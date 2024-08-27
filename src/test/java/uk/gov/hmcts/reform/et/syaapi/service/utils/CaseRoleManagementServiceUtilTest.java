package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.HEADER_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.HEADER_SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.MODIFICATION_TYPE_REVOKE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.buildHeaders;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.getHttpMethodByModificationType;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.getRespondentFullNameByUserInfo;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.DUMMY_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.DUMMY_SERVICE_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.DUMMY_USER_ID;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.JACKSON;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.MICHAEL;

class CaseRoleManagementServiceUtilTest {


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
    void theGetHttpMethodByModificationType() {
        assertThat(getHttpMethodByModificationType(MODIFICATION_TYPE_ASSIGNMENT)).isEqualTo(HttpMethod.POST);
        assertThat(getHttpMethodByModificationType(MODIFICATION_TYPE_REVOKE)).isEqualTo(HttpMethod.DELETE);
        assertThat(getHttpMethodByModificationType(null)).isEqualTo(null);
    }
}
