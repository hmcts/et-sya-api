package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.HEADER_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.HEADER_SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_REVOKE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.RemoteServiceUtil.buildHeaders;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.RemoteServiceUtil.getHttpMethodByCaseUserRoleModificationType;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.DUMMY_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.DUMMY_SERVICE_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_EXCEPTION_INVALID_MODIFICATION_TYPE;

class RemoteServiceUtilTest {

    @Test
    @SneakyThrows
    void theGetHttpMethodByCaseUserRoleModificationType() {
        assertThat(getHttpMethodByCaseUserRoleModificationType(MODIFICATION_TYPE_ASSIGNMENT))
            .isEqualTo(HttpMethod.POST);
        assertThat(getHttpMethodByCaseUserRoleModificationType(MODIFICATION_TYPE_REVOKE)).isEqualTo(HttpMethod.DELETE);
        Exception exception =
            assertThrows(ManageCaseRoleException.class, () -> getHttpMethodByCaseUserRoleModificationType(null));
        assertThat(exception.getMessage()).contains(TEST_EXCEPTION_INVALID_MODIFICATION_TYPE);
    }

    @Test
    @SneakyThrows
    void theBuildHeaders() {
        HttpHeaders httpHeaders = buildHeaders(DUMMY_AUTHORISATION_TOKEN, DUMMY_SERVICE_AUTHORISATION_TOKEN);
        assertThat(httpHeaders.get(HEADER_AUTHORIZATION)).contains(DUMMY_AUTHORISATION_TOKEN);
        assertThat(httpHeaders.get(HEADER_SERVICE_AUTHORIZATION)).contains(DUMMY_SERVICE_AUTHORISATION_TOKEN);
        assertThrows(
            IOException.class,
            () -> buildHeaders(StringUtils.EMPTY, DUMMY_SERVICE_AUTHORISATION_TOKEN));
    }
}
