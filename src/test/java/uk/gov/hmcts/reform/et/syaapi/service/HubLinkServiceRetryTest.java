package uk.gov.hmcts.reform.et.syaapi.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.HubLinksStatusesRequest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

/**
 * Integration tests for Spring Retry behaviour on {@link HubLinkService#updateHubLinkStatuses}.
 *
 * <p>These tests run through the Spring AOP proxy (via {@link ContextConfiguration}) so that
 * the {@link org.springframework.retry.annotation.Retryable} annotation is actually exercised.
 * Direct instantiation of the service (e.g. {@code new HubLinkService(...)}) bypasses the proxy
 * and would make the retry logic invisible to the test.</p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HubLinkServiceRetryTest.RetryTestConfig.class)
class HubLinkServiceRetryTest {

    private static final String CASE_TYPE = "ET_Scotland";
    private static final String CASE_ID = "1646225213651590";

    // -------------------------------------------------------------------------
    // Minimal Spring context: just the beans needed to proxy HubLinkService
    // -------------------------------------------------------------------------
    @Configuration
    @EnableRetry
    static class RetryTestConfig {
        @Bean
        CaseService caseService() {
            return mock(CaseService.class);
        }

        @Bean
        CaseDetailsConverter caseDetailsConverter() {
            return mock(CaseDetailsConverter.class);
        }

        @Bean
        FeatureToggleService featureToggleService() {
            return mock(FeatureToggleService.class);
        }

        @Bean
        ManageCaseRoleService manageCaseRoleService() {
            return mock(ManageCaseRoleService.class);
        }

        @Bean
        HubLinkService hubLinkService(CaseService caseService,
                                      CaseDetailsConverter caseDetailsConverter,
                                      FeatureToggleService featureToggleService,
                                      ManageCaseRoleService manageCaseRoleService) {
            return new HubLinkService(caseService, caseDetailsConverter,
                                      featureToggleService, manageCaseRoleService);
        }
    }

    // Autowired via Spring proxy so @Retryable is active
    @Autowired
    private HubLinkService hubLinkService;

    @Autowired
    private CaseService caseService;

    @Autowired
    private FeatureToggleService featureToggleService;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    private TestData testData;
    private HubLinksStatusesRequest hubLinksStatusesRequest;
    private FeignException.Conflict conflictException;

    @BeforeEach
    void setUp() {
        testData = new TestData();

        // Reset shared mocks between tests (beans are Spring singletons)
        reset(caseService, featureToggleService, caseDetailsConverter);

        hubLinksStatusesRequest = HubLinksStatusesRequest.builder()
            .caseTypeId(CASE_TYPE)
            .caseId(CASE_ID)
            .hubLinksStatuses(new HubLinksStatuses())
            .build();

        // A Mockito mock of Conflict avoids coupling to Feign's Request constructor signature
        conflictException = mock(FeignException.Conflict.class);

        // All retry tests exercise the case-flags-enabled path
        when(featureToggleService.isCaseFlagsEnabled()).thenReturn(true);
        when(caseService.startUpdate(any(), any(), any(), any()))
            .thenReturn(testData.getUpdateCaseEventResponse());
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * The most realistic production scenario: {@code submitUpdate} raises a 409 because a
     * concurrent request already committed the case.  The retry obtains a fresh event token via
     * a new {@code startUpdate} call and then succeeds.
     */
    @Test
    void shouldRetryWhenSubmitUpdateThrowsConflictAndSucceedOnThirdAttempt() {
        CaseDetails expectedCaseDetails = testData.getCaseDetailsWithData();

        when(caseService.submitUpdate(any(), any(), any(), any()))
            .thenThrow(conflictException)
            .thenThrow(conflictException)
            .thenReturn(expectedCaseDetails);

        CaseDetails result = hubLinkService.updateHubLinkStatuses(
            hubLinksStatusesRequest, TEST_SERVICE_AUTH_TOKEN, CASE_USER_ROLE_CREATOR
        );

        assertNotNull(result);
        // A fresh startUpdate token must be fetched on every attempt (3 total)
        verify(caseService, times(3)).startUpdate(any(), any(), any(), any());
        // submitUpdate was called each time but only the 3rd succeeded
        verify(caseService, times(3)).submitUpdate(any(), any(), any(), any());
    }

    /**
     * When all three attempts (initial + 2 retries) result in a 409, the final
     * {@link FeignException.Conflict} must propagate to the caller.
     */
    @Test
    void shouldExhaustRetriesAndPropagateConflictException() {
        when(caseService.submitUpdate(any(), any(), any(), any()))
            .thenThrow(conflictException);

        assertThrows(FeignException.Conflict.class, () ->
            hubLinkService.updateHubLinkStatuses(
                hubLinksStatusesRequest, TEST_SERVICE_AUTH_TOKEN, CASE_USER_ROLE_CREATOR
            )
        );

        // maxAttempts = 3, so startUpdate is called 3 times before giving up
        verify(caseService, times(3)).startUpdate(any(), any(), any(), any());
        verify(caseService, times(3)).submitUpdate(any(), any(), any(), any());
    }

    /**
     * Non-409 exceptions must NOT trigger a retry — they should propagate immediately.
     */
    @Test
    void shouldNotRetryOnNonConflictException() {
        RuntimeException serviceUnavailable = new RuntimeException("Service unavailable");

        when(caseService.submitUpdate(any(), any(), any(), any()))
            .thenThrow(serviceUnavailable);

        assertThrows(RuntimeException.class, () ->
            hubLinkService.updateHubLinkStatuses(
                hubLinksStatusesRequest, TEST_SERVICE_AUTH_TOKEN, CASE_USER_ROLE_CREATOR
            )
        );

        // Only one attempt — no retries for non-Conflict exceptions
        verify(caseService, times(1)).startUpdate(any(), any(), any(), any());
        verify(caseService, times(1)).submitUpdate(any(), any(), any(), any());
    }
}
