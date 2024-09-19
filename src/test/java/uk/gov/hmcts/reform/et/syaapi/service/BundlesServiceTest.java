package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingBundleType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantBundlesRequest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.SUBMIT_CLAIMANT_BUNDLES;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

class BundlesServiceTest {

    private BundlesService bundlesService;
    @MockBean
    private CaseService caseService;
    @MockBean
    private NotificationService notificationService;

    private final TestData testData;

    private static final String MOCK_TOKEN = "Bearer TestServiceAuth";

    BundlesServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() {
        caseService = mock(CaseService.class);
        notificationService = mock(NotificationService.class);

        ObjectMapper objectMapper = new ObjectMapper();
        bundlesService = new BundlesService(
            caseService,
            new CaseDetailsConverter(objectMapper),
            notificationService
        );

        when(caseService.startUpdate(
            any(),
            any(),
            any(),
            any()
        )).thenReturn(testData.getUpdateCaseEventResponse());
    }

    @Test
    void shouldSubmitUpdateForBundles() {
        ClaimantBundlesRequest testRequest = testData.getClaimantBundlesRequest();

        bundlesService.submitBundles(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest
        );

        verify(caseService, times(1)).submitUpdate(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(testRequest.getCaseId()),
            any(),
            any()
        );
        verify(notificationService, times(1)).sendBundlesEmails(
            any(),
            eq(testRequest.getCaseId()),
            eq(testRequest.getClaimantBundles().getHearing())
        );

    }

    @Test
    void shouldAddBundleToClaimantBundleCollection() {
        ClaimantBundlesRequest request = testData.getClaimantBundlesRequest();

        StartEventResponse updateCaseEventResponse = testData.getUpdateCaseEventResponse();
        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            request.getCaseId(),
            request.getCaseTypeId(),
            SUBMIT_CLAIMANT_BUNDLES
        )).thenReturn(updateCaseEventResponse);

        List<GenericTypeItem<HearingBundleType>> collection = new ArrayList<>();
        UploadedDocumentType file = UploadedDocumentType.builder()
            .documentFilename("filename.pdf").documentBinaryUrl("url").documentUrl("url").build();

        collection.add(
            GenericTypeItem.from(
                HearingBundleType.builder()
                    .agreedDocWith("text")
                    .hearing("122333-abc-1122333")
                    .whatDocuments("supplementary")
                    .whoseDocuments("bothParties")
                    .uploadFile(file)
                    .build())
        );

        ArgumentCaptor<CaseDataContent> contentCaptor = ArgumentCaptor.forClass(CaseDataContent.class);
        bundlesService.submitBundles(MOCK_TOKEN, request);

        verify(caseService, times(1)).submitUpdate(
            eq(MOCK_TOKEN), any(), contentCaptor.capture(), any());

        CaseData data = (CaseData) contentCaptor.getValue().getData();
        Assertions.assertEquals(collection.get(0).getValue(), data.getBundlesClaimantCollection().get(0).getValue());
    }
}
