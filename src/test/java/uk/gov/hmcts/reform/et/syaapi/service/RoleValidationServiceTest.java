package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleValidationServiceTest {

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private RoleValidationService roleValidationService;

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String USER_ID = "user-123";
    private static final String CASEWORKER_ROLE = "caseworker-employment-api";
    private static final String ACAS_ROLE = "et-acas-api";
    private static final String OTHER_ROLE = "citizen";

    private UserInfo userInfo;

    @BeforeEach
    void setUp() {
        userInfo = UserInfo.builder()
            .uid(USER_ID)
            .build();
    }

    @Test
    void hasAnyRole_shouldReturnTrue_whenUserHasOneOfRequiredRoles() {
        userInfo = UserInfo.builder()
            .uid(USER_ID)
            .roles(Arrays.asList(CASEWORKER_ROLE, OTHER_ROLE))
            .build();

        when(idamClient.getUserInfo(AUTH_TOKEN)).thenReturn(userInfo);

        List<String> requiredRoles = Arrays.asList(CASEWORKER_ROLE, ACAS_ROLE);
        boolean result = roleValidationService.hasAnyRole(AUTH_TOKEN, requiredRoles);

        assertThat(result).isTrue();
    }

    @Test
    void hasAnyRole_shouldReturnTrue_whenUserHasDifferentRequiredRole() {
        userInfo = UserInfo.builder()
            .uid(USER_ID)
            .roles(Arrays.asList(ACAS_ROLE, OTHER_ROLE))
            .build();

        when(idamClient.getUserInfo(AUTH_TOKEN)).thenReturn(userInfo);

        List<String> requiredRoles = Arrays.asList(CASEWORKER_ROLE, ACAS_ROLE);
        boolean result = roleValidationService.hasAnyRole(AUTH_TOKEN, requiredRoles);

        assertThat(result).isTrue();
    }

    @Test
    void hasAnyRole_shouldReturnFalse_whenUserHasNoRequiredRoles() {
        userInfo = UserInfo.builder()
            .uid(USER_ID)
            .roles(Collections.singletonList(OTHER_ROLE))
            .build();

        when(idamClient.getUserInfo(AUTH_TOKEN)).thenReturn(userInfo);

        List<String> requiredRoles = Arrays.asList(CASEWORKER_ROLE, ACAS_ROLE);
        boolean result = roleValidationService.hasAnyRole(AUTH_TOKEN, requiredRoles);

        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_shouldReturnFalse_whenUserHasNoRoles() {
        userInfo = UserInfo.builder()
            .uid(USER_ID)
            .roles(Collections.emptyList())
            .build();

        when(idamClient.getUserInfo(AUTH_TOKEN)).thenReturn(userInfo);

        List<String> requiredRoles = Arrays.asList(CASEWORKER_ROLE, ACAS_ROLE);
        boolean result = roleValidationService.hasAnyRole(AUTH_TOKEN, requiredRoles);

        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_shouldReturnFalse_whenUserRolesAreNull() {
        userInfo = UserInfo.builder()
            .uid(USER_ID)
            .roles(null)
            .build();

        when(idamClient.getUserInfo(AUTH_TOKEN)).thenReturn(userInfo);

        List<String> requiredRoles = Arrays.asList(CASEWORKER_ROLE, ACAS_ROLE);
        boolean result = roleValidationService.hasAnyRole(AUTH_TOKEN, requiredRoles);

        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_shouldReturnFalse_whenExceptionOccurs() {
        when(idamClient.getUserInfo(AUTH_TOKEN)).thenThrow(new RuntimeException("IDAM error"));

        List<String> requiredRoles = Arrays.asList(CASEWORKER_ROLE, ACAS_ROLE);
        boolean result = roleValidationService.hasAnyRole(AUTH_TOKEN, requiredRoles);

        assertThat(result).isFalse();
    }
}
