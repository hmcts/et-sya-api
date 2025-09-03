package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentTse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.RespondentApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED_STATE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

class StoreRespondentTseServiceTest {
    @MockBean
    private CaseService caseService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;
    @InjectMocks
    private StoreRespondentTseService storeRespondentTseService;

    private final TestData testData;

    private static final String CASE_DETAILS_NOT_FOUND = "submitUpdate CaseDetails not found";
    private static final long CASE_ID = 1_646_225_213_651_590L;
    private static final String CASE_TYPE_ID = "ET_EnglandWales";

    StoreRespondentTseServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() {
        caseService = mock(CaseService.class);
        caseDetailsConverter = mock(CaseDetailsConverter.class);

        storeRespondentTseService = new StoreRespondentTseService(
            caseService,
            caseDetailsConverter,
            mock(NotificationsProperties.class),
            mock(NotificationClient.class)
        );
    }

    @Test
    void storeApplicationShouldReturnCaseDetails() {
        RespondentTse respondentTse = new RespondentTse();
        respondentTse.setContactApplicationType("withdraw");
        respondentTse.setRespondentIdamId("test");

        RespondentApplicationRequest testRequest = RespondentApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .respondentTse(respondentTse)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.STORE_RESPONDENT_TSE
        )).thenReturn(testData.getSendNotificationCollectionResponse());

        when(caseService.submitUpdate(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(testRequest.getCaseId()),
            any(),
            eq(testRequest.getCaseTypeId())
        )).thenReturn(testData.getCaseDetailsWithData());

        storeRespondentTseService.storeApplication(TEST_SERVICE_AUTH_TOKEN, testRequest);

        verify(caseService, times(1)).submitUpdate(
            any(),
            any(),
            any(),
            any()
        );

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        List<GenericTseApplicationTypeItem> actualCollection =
            argumentCaptor.getValue().getTseRespondentStoredCollection();
        assertThat(actualCollection.toArray())
            .hasSize(1);

        GenericTseApplicationType actual = actualCollection.getFirst().getValue();
        assertThat(actual.getDate())
            .isEqualTo(UtilHelper.formatCurrentDate(LocalDate.now()));
        assertThat(actual.getApplicant())
            .isEqualTo(RESPONDENT_TITLE);
        assertThat(actual.getStatus())
            .isEqualTo(STORED_STATE);
        assertThat(actual.getApplicationState())
            .isEqualTo(STORED);
    }

    @Test
    void storeApplicationShouldFinalCaseDetailsException() {
        RespondentTse respondentTse = new RespondentTse();
        respondentTse.setContactApplicationType("withdraw");

        RespondentApplicationRequest testRequest = RespondentApplicationRequest.builder()
            .caseId(String.valueOf(CASE_ID))
            .caseTypeId(CASE_TYPE_ID)
            .respondentTse(respondentTse)
            .build();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.STORE_RESPONDENT_TSE
        )).thenReturn(testData.getSendNotificationCollectionResponse());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            storeRespondentTseService.storeApplication(TEST_SERVICE_AUTH_TOKEN, testRequest));
        assertThat(exception.getMessage())
            .isEqualTo(CASE_DETAILS_NOT_FOUND);
    }
}
