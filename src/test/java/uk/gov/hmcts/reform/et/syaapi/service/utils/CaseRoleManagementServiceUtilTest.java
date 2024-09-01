package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
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
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.buildHeaders;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.CaseRoleManagementServiceUtil.getHttpMethodByModificationType;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.DUMMY_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.DUMMY_SERVICE_AUTHORISATION_TOKEN;

class CaseRoleManagementServiceUtilTest {

    private static final String AAC_URL_TEST_VALUE = "https://test.url.com";
    private static final String EXPECTED_AAC_URI_WITH_ONLY_CASE_DETAILS =
        "https://test.url.com/case-users?case_ids=1646225213651533&case_ids=1646225213651512";
    private static final String EXPECTED_AAC_URI_WITH_CASE_AND_USER_IDS =
        "https://test.url.com/case-users?case_ids=1646225213651533&case_ids=1646225213651512&"
            + "user_ids=123456789012345678901234567890&user_ids=123456789012345678901234567890";

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

    @ParameterizedTest
    @MethodSource("provideTheCreateAacSearchCaseUsersUriByCaseAndUserIdsTestData")
    void theCreateAacSearchCaseUsersUriByCaseAndUserIds(String aacUrl,
                                                        List<CaseDetails> caseDetailsList,
                                                        List<UserInfo> userInfoList) {
        if (StringUtils.isBlank(aacUrl) || CollectionUtils.isEmpty(caseDetailsList)) {
            assertThat(CaseRoleManagementServiceUtil.createAacSearchCaseUsersUriByCaseAndUserIds(
                aacUrl, caseDetailsList, userInfoList)).isEqualTo(StringUtils.EMPTY);
            return;
        }
        if (CollectionUtils.isEmpty(userInfoList)) {
            assertThat(CaseRoleManagementServiceUtil.createAacSearchCaseUsersUriByCaseAndUserIds(
                aacUrl, caseDetailsList, userInfoList)).isEqualTo(EXPECTED_AAC_URI_WITH_ONLY_CASE_DETAILS);
            return;
        }
        assertThat(CaseRoleManagementServiceUtil.createAacSearchCaseUsersUriByCaseAndUserIds(
            aacUrl, caseDetailsList, userInfoList)).isEqualTo(EXPECTED_AAC_URI_WITH_CASE_AND_USER_IDS);
    }

    private static Stream<Arguments> provideTheCreateAacSearchCaseUsersUriByCaseAndUserIdsTestData() {
        List<CaseDetails> caseDetailsList = new CaseTestData().getRequestCaseDataListScotland();
        UserInfo userInfo = new CaseTestData().getUserInfo();
        List<UserInfo> userInfoList = List.of(userInfo, userInfo);
        return Stream.of(Arguments.of(null, null, null),
                         Arguments.of(null, caseDetailsList, null),
                         Arguments.of(StringUtils.EMPTY, caseDetailsList, null),
                         Arguments.of(AAC_URL_TEST_VALUE, null, null),
                         Arguments.of(AAC_URL_TEST_VALUE, new ArrayList<>(), null),
                         Arguments.of(AAC_URL_TEST_VALUE, caseDetailsList, null),
                         Arguments.of(AAC_URL_TEST_VALUE, caseDetailsList, new ArrayList<>()),
                         Arguments.of(AAC_URL_TEST_VALUE, caseDetailsList, userInfoList));
    }
}
