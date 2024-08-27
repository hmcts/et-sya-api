package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.io.IOException;

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
