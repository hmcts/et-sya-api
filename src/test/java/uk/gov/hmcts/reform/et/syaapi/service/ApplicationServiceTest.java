package uk.gov.hmcts.reform.et.syaapi.service;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.ChangeApplicationStatusRequest;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.service.notify.NotificationClientException;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@SuppressWarnings({"PMD.SingularField", "PMD.TooManyMethods"})
class ApplicationServiceTest {

    public static final String RESPONDENT_LIST =
        "Test Respondent Organisation -1-, Mehmet Tahir Dede, Abuzer Kadayif, Kate Winslet, Jeniffer Lopez";
    public static final String NOT_SET = "Not set";
    public static final String CASE_REF = "123456/2022";
    public static final String CLAIMANT = "Michael Jackson";
    public static final String CASE_ID = "1646225213651590";
    public static final String INITIAL_STATE = "initialState";

    @MockBean
    private CaseService caseService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private CaseDocumentService caseDocumentService;
    @MockBean
    private ApplicationService applicationService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    private final TestData testData;

    ApplicationServiceTest() {
        testData = new TestData();
    }

    @BeforeEach
    void before() throws CaseDocumentException, DocumentGenerationException {
        caseService = mock(CaseService.class);
        notificationService = mock(NotificationService.class);
        caseDocumentService = mock(CaseDocumentService.class);
        caseDetailsConverter = mock(CaseDetailsConverter.class);

        applicationService = new ApplicationService(
            caseService,
            notificationService,
            caseDocumentService,
            caseDetailsConverter
        );

        when(caseService.getUserCase(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getClaimantApplicationRequest().getCaseId()
        )).thenReturn(testData.getCaseDetailsWithData());

        doNothing().when(caseService).uploadTseSupportingDocument(any(), any());
        doNothing().when(caseService).uploadTseCyaAsPdf(any(), any(), any(), any());

        when(caseService.triggerEvent(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(testData.getClaimantApplicationRequest().getCaseId()),
            eq(CaseEvent.SUBMIT_CLAIMANT_TSE),
            eq(testData.getClaimantApplicationRequest().getCaseTypeId()),
            any()
        )).thenReturn(testData.getCaseDetailsWithData());

        ResponseEntity<ByteArrayResource> responseEntity =
            new ResponseEntity<>(HttpStatus.OK);
        when(caseDocumentService.downloadDocument(eq(TEST_SERVICE_AUTH_TOKEN), any())).thenReturn(responseEntity);

    }

    @Test
    void shouldSendClaimantEmailWithCorrectParameters() throws NotificationClientException {
        applicationService.submitApplication(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getClaimantApplicationRequest()
        );
        verify(notificationService, times(1)).sendAcknowledgementEmailToClaimant(
            any(),
            eq(CLAIMANT),
            eq(CASE_REF),
            eq(RESPONDENT_LIST),
            eq(NOT_SET),
            eq(CASE_ID),
            any()
        );
    }

    @Test
    void shouldSendRespondentEmailWithCorrectParameters() throws NotificationClientException {
        byte[] bytes = "Sample".getBytes();
        ResponseEntity<ByteArrayResource> responseEntity =
            new ResponseEntity<>(new ByteArrayResource(bytes), HttpStatus.OK);

        when(caseDocumentService.downloadDocument(eq(TEST_SERVICE_AUTH_TOKEN), any())).thenReturn(responseEntity);

        applicationService.submitApplication(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getClaimantApplicationRequest()
        );

        verify(notificationService, times(1)).sendAcknowledgementEmailToRespondents(
            any(),
            eq(CLAIMANT),
            eq(CASE_REF),
            eq(RESPONDENT_LIST),
            eq(NOT_SET),
            eq(CASE_ID),
            any(),
            any()
        );
    }

    @Test
    void shouldSendRespondentEmailWithNoSupportingDocument() throws NotificationClientException {
        applicationService.submitApplication(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getClaimantApplicationRequest()
        );

        verify(notificationService, times(1)).sendAcknowledgementEmailToRespondents(
            any(),
            eq(CLAIMANT),
            eq(CASE_REF),
            eq(RESPONDENT_LIST),
            eq(NOT_SET),
            eq(CASE_ID),
            eq(null),
            any()
        );
    }

    @Test
    void shouldSendTribunalEmailWithCorrectParameters() throws NotificationClientException {
        applicationService.submitApplication(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getClaimantApplicationRequest()
        );

        verify(notificationService, times(1)).sendAcknowledgementEmailToTribunal(
            any(),
            eq(CLAIMANT),
            eq(CASE_REF),
            eq(RESPONDENT_LIST),
            eq(NOT_SET),
            eq(CASE_ID),
            any()
        );
    }

    @Test
    void shouldMarkApplicationAsViewed() {
        ChangeApplicationStatusRequest testRequest = testData.getChangeApplicationStatusRequest();

        when(caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testRequest.getCaseId(),
            testRequest.getCaseTypeId(),
            CaseEvent.CLAIMANT_TSE_RESPOND
        )).thenReturn(testData.getUpdateCaseEventResponse());

        applicationService.changeApplicationStatus(TEST_SERVICE_AUTH_TOKEN, testRequest);

        ArgumentCaptor<CaseData> argumentCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(caseDetailsConverter).caseDataContent(any(), argumentCaptor.capture());

        String actualState
            = argumentCaptor.getValue().getGenericTseApplicationCollection().get(0).getValue().getApplicationState();
        assertThat(actualState).isEqualTo("viewed");
    }

    @Nested
    class RespondToApplicationReplyToTribunal {
        RespondToApplicationRequest testRequest;

        @BeforeEach
        void setUp() {
            testRequest = testData.getRespondToApplicationRequest();
            when(caseService.startUpdate(
                TEST_SERVICE_AUTH_TOKEN,
                testRequest.getCaseId(),
                testRequest.getCaseTypeId(),
                CaseEvent.CLAIMANT_TSE_RESPOND
            )).thenReturn(testData.getUpdateCaseEventResponse());
        }

        @Test
        void shouldSubmitResponseToApplication() {
            applicationService.respondToApplication(
                TEST_SERVICE_AUTH_TOKEN,
                testRequest
            );

            verify(caseDetailsConverter, times(1)).caseDataContent(
                any(),
                any()
            );
        }

        @Test
        void shouldSubmitResponseToApplicationAndSendCopy() throws CaseDocumentException, DocumentGenerationException {
            testRequest.getResponse().setCopyToOtherParty("Yes");

            applicationService.respondToApplication(
                TEST_SERVICE_AUTH_TOKEN,
                testRequest
            );

            verify(caseService, times(1)).createResponsePdf(
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(),
                eq(testRequest),
                any()
            );

            verify(caseDetailsConverter, times(1)).caseDataContent(
                any(),
                any()
            );
        }

        @Test
        void shouldSubmitResponseToApplicationWhenOptionalFileNotProvided()
            throws CaseDocumentException, DocumentGenerationException {
            testRequest.setSupportingMaterialFile(null);
            TseRespondType response = testRequest.getResponse();
            response.setSupportingMaterial(null);
            response.setCopyToOtherParty("Yes");
            response.setCopyNoGiveDetails(null);

            applicationService.respondToApplication(
                TEST_SERVICE_AUTH_TOKEN,
                testRequest
            );

            verify(caseService, times(1)).createResponsePdf(
                eq(TEST_SERVICE_AUTH_TOKEN),
                any(),
                eq(testRequest),
                any()
            );

            verify(caseDetailsConverter, times(1)).caseDataContent(
                any(),
                any()
            );
        }

        @Test
        void shouldNotSubmitResponseToApplication() {
            testRequest.setApplicationId("12");

            assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.respondToApplication(
                    TEST_SERVICE_AUTH_TOKEN,
                    testRequest
                )
            );

            verify(caseDetailsConverter, times(0)).caseDataContent(
                any(),
                any()
            );
        }

        @Test
        void shouldThrowErrorWhenSavingApplication() {
            when(caseService.submitUpdate(
                eq(TEST_SERVICE_AUTH_TOKEN),
                eq(testRequest.getCaseId()),
                any(),
                any()
            )).thenThrow(new JSONException("Could not save response"));

            assertThrows(
                JSONException.class,
                () -> applicationService.respondToApplication(
                    TEST_SERVICE_AUTH_TOKEN,
                    testRequest
                )
            );
        }

        @Test
        void shouldSendResponseEmailsToClaimantWithCorrectParameters() {
            applicationService.respondToApplication(
                TEST_SERVICE_AUTH_TOKEN,
                testRequest
            );

            verify(notificationService, times(1)).sendResponseEmailToClaimant(
                any(),
                eq(CLAIMANT),
                eq(CASE_ID),
                eq(""),
                eq(NOT_SET),
                eq("12345"),
                eq("Amend details"),
                eq("No")
            );
        }

        @ParameterizedTest
        @MethodSource
        void testNewStateAndResponseRequired(String claimantResponseRequired, String expectedApplicationState,
                                    String expectedClaimantResponseRequired) {
            MockedStatic<TseApplicationHelper> mockStatic = mockStatic(TseApplicationHelper.class);

            GenericTseApplicationType application = GenericTseApplicationType.builder()
                .claimantResponseRequired(claimantResponseRequired).applicationState(INITIAL_STATE).build();

            mockStatic.when(() -> TseApplicationHelper.getSelectedApplication(any(), any()))
                .thenReturn(GenericTseApplicationTypeItem.builder().value(application).build());

            applicationService.respondToApplication(TEST_SERVICE_AUTH_TOKEN, testRequest);

            assertThat(application.getApplicationState()).isEqualTo(expectedApplicationState);
            assertThat(application.getClaimantResponseRequired()).isEqualTo(expectedClaimantResponseRequired);

            mockStatic.close();
        }

        private static Stream<Arguments> testNewStateAndResponseRequired() {
            return Stream.of(
                Arguments.of("Yes", "inProgress", "No"),
                Arguments.of("No", INITIAL_STATE, "No"),
                Arguments.of(null, INITIAL_STATE, null)
            );
        }
    }
}
