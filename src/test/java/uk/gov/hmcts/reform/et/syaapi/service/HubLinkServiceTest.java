package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.HubLinksStatusesRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

class HubLinkServiceTest {
    private static final String CASE_TYPE = "ET_Scotland";
    private static final String CASE_ID = "1646225213651590";

    @MockBean
    CaseService caseService;
    @MockBean
    CaseDetailsConverter caseDetailsConverter;
    @MockBean
    private HubLinkService hubLinkService;
    @MockBean
    private FeatureToggleService featureToggleService;

    private final TestData testData;
    private HubLinksStatusesRequest hubLinksStatusesRequest;

    HubLinkServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() {
        caseService = mock(CaseService.class);
        caseDetailsConverter = mock(CaseDetailsConverter.class);
        featureToggleService = mock(FeatureToggleService.class);
        hubLinkService = new HubLinkService(caseService, caseDetailsConverter, featureToggleService);
        HubLinksStatuses hubLinksStatuses = new HubLinksStatuses();
        hubLinksStatusesRequest = HubLinksStatusesRequest.builder()
            .caseTypeId(CASE_TYPE)
            .caseId(CASE_ID)
            .hubLinksStatuses(hubLinksStatuses)
            .build();

        when(caseService.startUpdate(
            any(),
            any(),
            any(),
            any()
        )).thenReturn(testData.getUpdateCaseEventResponse());

    }

    @Test
    void shouldUpdateHubLinks() {
        when(featureToggleService.isCaseFlagsEnabled()).thenReturn(true);
        when(caseService.triggerEvent(
            eq(TEST_SERVICE_AUTH_TOKEN),
            any(),
            eq(CaseEvent.UPDATE_HUBLINK_STATUS),
            eq(testData.getClaimantApplicationRequest().getCaseTypeId()),
            any()
        )).thenReturn(testData.getCaseDetailsWithData());

        hubLinkService.updateHubLinkStatuses(hubLinksStatusesRequest,
                                             TEST_SERVICE_AUTH_TOKEN,
                                             CASE_USER_ROLE_CREATOR);

        verify(caseDetailsConverter, times(1)).caseDataContent(
            any(),
            any()
        );
    }

    @Test
    void shouldStartUpdateSubmittedCaseWhenToggleIsFalse() {
        when(caseService.triggerEvent(
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID,
            CaseEvent.valueOf("UPDATE_CASE_SUBMITTED"),
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            null
        )).thenReturn(testData.getCaseDetailsWithData());
        when(featureToggleService.isCaseFlagsEnabled()).thenReturn(false);
        when(caseService.getUserCaseByCaseUserRole(TEST_SERVICE_AUTH_TOKEN, CASE_ID, CASE_USER_ROLE_CREATOR))
            .thenReturn(testData.getCaseDetailsWithData());

        hubLinkService.updateHubLinkStatuses(hubLinksStatusesRequest, TEST_SERVICE_AUTH_TOKEN, CASE_USER_ROLE_CREATOR);

        verify(caseService, times(1)).getUserCaseByCaseUserRole(
            TEST_SERVICE_AUTH_TOKEN,
            hubLinksStatusesRequest.getCaseId(),
            CASE_USER_ROLE_CREATOR
        );
        verify(caseService, times(1)).triggerEvent(
            TEST_SERVICE_AUTH_TOKEN, hubLinksStatusesRequest.getCaseId(),
            CaseEvent.valueOf("UPDATE_CASE_SUBMITTED"),
            hubLinksStatusesRequest.getCaseTypeId(),
            testData.getCaseDetailsWithData().getData());
    }
}
