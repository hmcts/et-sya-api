package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.et.syaapi.service.utils.GenericServiceUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SUBMIT_CASE_PDF_FILE_RESPONSE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UNASSIGNED_OFFICE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.YES;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.NOTIFICATION_CONFIRMATION_ID;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SUBMIT_CASE_PDF_FILE_RESPONSE;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.WELSH_LANGUAGE;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
class NotificationServiceTest {
    public static final String CLAIMANT = "Michael Jackson";
    public static final String NOT_SET = "Not set";
    public static final String TEST_RESPONDENT = "Test Respondent";
    private static final String WITNESS = "witness";
    private static final String CHANGE_DETAILS_APPLICATION_TYPE = "Change my personal details";

    @MockBean
    private NotificationService notificationService;
    private NotificationClient notificationClient;
    private NotificationsProperties notificationsProperties;
    private final ConcurrentHashMap<String, String> parameters = new ConcurrentHashMap<>();
    private CaseTestData caseTestData;

    @BeforeEach
    void before() throws NotificationClientException {
        parameters.put("firstname", "test");
        parameters.put("references", "123456789");
        notificationClient = mock(NotificationClient.class);
        notificationsProperties = mock(NotificationsProperties.class);
        notificationService = new NotificationService(notificationClient, notificationsProperties);
        given(notificationClient.sendEmail(anyString(), anyString(), any(), anyString()))
            .willReturn(TestConstants.INPUT_SEND_EMAIL_RESPONSE);
        given(notificationsProperties.getCySubmitCaseEmailTemplateId())
            .willReturn(TestConstants.WELSH_DUMMY_PDF_TEMPLATE_ID);
        given(notificationsProperties.getSubmitCaseEmailTemplateId())
            .willReturn(TestConstants.SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID);
        given(notificationsProperties.getCitizenPortalLink()).willReturn(TestConstants.REFERENCE_STRING);
        given(notificationsProperties.getClaimantTseEmailNoTemplateId()).willReturn("No");
        given(notificationsProperties.getClaimantTseEmailYesTemplateId()).willReturn("Yes");
        given(notificationsProperties.getClaimantTseEmailTypeCTemplateId()).willReturn("C");
        given(notificationsProperties.getTribunalAcknowledgementTemplateId()).willReturn("Tribunal");
        given(notificationsProperties.getRespondentTseEmailTypeATemplateId()).willReturn("A");
        given(notificationsProperties.getRespondentTseEmailTypeBTemplateId()).willReturn("B");
        // todo add pse / tse?
        given(notificationsProperties.getPseClaimantResponseYesTemplateId())
            .willReturn("claimantResponseYesTemplateId");
        given(notificationsProperties.getPseClaimantResponseNoTemplateId())
            .willReturn("claimantResponseNoTemplateId");

        given(notificationsProperties.getTseTribunalResponseToRequestTemplateId())
            .willReturn("tseTribunalResponseToRequestTemplateId");
        given(notificationsProperties.getTseClaimantResponseToRequestYesTemplateId())
            .willReturn("tseClaimantResponseToRequestYesTemplateId");
        given(notificationsProperties.getTseClaimantResponseToRequestNoTemplateId())
            .willReturn("tseClaimantResponseToRequestNoTemplateId");
        caseTestData = new CaseTestData();
    }

    @SneakyThrows
    @Test
    void shouldSendEmailByMockingResponse() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
        assertThat(sendEmailResponse.getReference()).isPresent();
    }

    @SneakyThrows
    @Test
    void shouldRetrieveTemplateIdCorrectly() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
        assertThat(sendEmailResponse.getTemplateId()).isEqualTo(
            UUID.fromString(TestConstants.UUID_DUMMY_STRING));
    }

    @SneakyThrows
    @Test
    void shouldRetrieveEmailIdCorrectly() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
        assertThat(sendEmailResponse.getFromEmail()).isNotEmpty();
    }

    @SneakyThrows
    @Test
    void shouldRetrieveNotificationIdCorrectly() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
        assertThat(sendEmailResponse.getNotificationId()).isEqualTo(
            UUID.fromString("f30b2148-b1a6-4c0d-8a10-50109c96dc2c"));
    }

    @Test
    void ifTargetEmailIsNullWillThrowNotificationException() throws NotificationClientException {
        given(notificationClient.sendEmail(anyString(), nullable(String.class), any(), anyString()))
            .willThrow(new NotificationClientException("email_address is a required property"));
        assertThatThrownBy(
            () -> notificationService.sendEmail(TestConstants.TEST_TEMPLATE_API_KEY, null, parameters,
                                                TestConstants.REFERENCE_STRING))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("email_address is a required property");
    }

    @Test
    void ifTemplateIdIsNullWillThrowNotificationException() throws NotificationClientException {
        given(notificationClient.sendEmail(nullable(String.class), anyString(), any(), anyString()))
            .willThrow(new NotificationClientException("template_id is a required property"));
        assertThatThrownBy(
            () -> notificationService.sendEmail(null, TestConstants.TEST_EMAIL, parameters,
                                                TestConstants.REFERENCE_STRING))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("template_id is a required property");
    }

    @Test
    void ifTemplateNotFoundWillThrowNotificationException() throws NotificationClientException {

        given(notificationClient.sendEmail(anyString(), anyString(), any(), anyString()))
            .willThrow(new NotificationClientException("Template not found"));
        assertThatThrownBy(
            () -> notificationService.sendEmail(TestConstants.TEST_TEMPLATE_API_KEY, TestConstants.TEST_EMAIL,
                                                parameters, TestConstants.REFERENCE_STRING))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("Template not found");
    }

    @SneakyThrows
    private SendEmailResponse mockSendEmailResponse() {
        notificationClient = mock(NotificationClient.class);
        notificationService = new NotificationService(notificationClient, notificationsProperties);
        doReturn(TestConstants.INPUT_SEND_EMAIL_RESPONSE).when(notificationClient)
            .sendEmail(TestConstants.TEST_TEMPLATE_API_KEY,
                       TestConstants.TEST_EMAIL, parameters, TestConstants.REFERENCE_STRING
        );
        return notificationService.sendEmail(TestConstants.TEST_TEMPLATE_API_KEY,
                                             TestConstants.TEST_EMAIL, parameters, TestConstants.REFERENCE_STRING
        );
    }

    @ParameterizedTest
    @MethodSource("retrieveSubmitCaseConfirmationEmailPdfFilesArguments")
    void shouldTestSubmitCaseConfirmationWithGivenPdfFilesArguments(List<PdfDecodedMultipartFile> pdfFiles,
                                                                  String expectedValue) {
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        caseTestData.getCaseData().getClaimantHearingPreference().setContactLanguage(null);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            caseTestData.getCaseRequest(),
            caseTestData.getCaseData(),
            caseTestData.getUserInfo(),
            pdfFiles
        );
        String actualValue = response == null ? TestConstants.EMPTY_RESPONSE : response.getBody();
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @SneakyThrows
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1234567890111213141516"})
    void shouldSendSubmitCaseConfirmationEmailWithGivenCaseIdsEvenItIsEmpty(String caseId) {
        when(notificationClient.sendEmail(
            anyString(),
            anyString(),
            any(),
            eq(caseTestData.getExpectedDetails().getId().toString())
        )).thenReturn(TestConstants.SEND_EMAIL_RESPONSE_ENGLISH);
        caseTestData.getCaseRequest().setCaseId(caseId);
        caseTestData.getCaseData().getClaimantHearingPreference().setContactLanguage(null);
        List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
        casePdfFiles.add(TestConstants.PDF_DECODED_MULTIPART_FILE1);
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            caseTestData.getCaseRequest(),
            caseTestData.getCaseData(),
            caseTestData.getUserInfo(),
            casePdfFiles
        );
        assertThat(response.getBody()).isEqualTo(TestConstants.SEND_NOTIFICATION_NO_LANGUAGE_RESPONSE_BODY);
    }

    @SneakyThrows
    @ParameterizedTest
    @CsvSource(value = {"|" + TestConstants.SEND_NOTIFICATION_NO_LANGUAGE_RESPONSE_BODY,
        "  |" + TestConstants.SEND_NOTIFICATION_NO_LANGUAGE_RESPONSE_BODY,
        TestConstants.UUID_DUMMY_STRING + "|" + TestConstants.SEND_NOTIFICATION_NO_LANGUAGE_RESPONSE_BODY,
        WELSH_LANGUAGE + "|" + TestConstants.SEND_NOTIFICATION_WELSH_RESPONSE_BODY,
        ENGLISH_LANGUAGE + "|" + TestConstants.SEND_NOTIFICATION_ENGLISH_RESPONSE_BODY},
        delimiter = '|')
    void shouldSendSubmitCaseConfirmationEmailAccordingToSelectedLanguage(String selectedLanguage,
                                                                          String expectedBody) {
        when(notificationClient.sendEmail(
            anyString(),
            anyString(),
            any(),
            eq(caseTestData.getExpectedDetails().getId().toString())
        )).thenReturn(
            WELSH_LANGUAGE.equals(selectedLanguage) ? TestConstants.SEND_EMAIL_RESPONSE_WELSH
                : ENGLISH_LANGUAGE.equals(selectedLanguage) ? TestConstants.SEND_EMAIL_RESPONSE_ENGLISH
                : TestConstants.INPUT_SEND_EMAIL_RESPONSE);
        caseTestData.getCaseRequest().setCaseId(caseTestData.getExpectedDetails().getId().toString());
        caseTestData.getCaseData().getClaimantHearingPreference().setContactLanguage(selectedLanguage);
        List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
        casePdfFiles.add(TestConstants.PDF_DECODED_MULTIPART_FILE1);
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            caseTestData.getCaseRequest(),
            caseTestData.getCaseData(),
            caseTestData.getUserInfo(),
            casePdfFiles
        );
        assertThat(response.getBody()).isEqualTo(expectedBody);
    }

    @SneakyThrows
    @Test
    void shouldThrowExceptionWhenSubmitCaseConfirmationEmailNotSent() {
        try (MockedStatic<GenericServiceUtil> mockedServiceUtil = Mockito.mockStatic(GenericServiceUtil.class)) {
            when(notificationClient.sendEmail(
                anyString(),
                anyString(),
                any(),
                anyString()
            )).thenThrow(NotificationClientException.class);
            List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
            casePdfFiles.add(TestConstants.PDF_DECODED_MULTIPART_FILE1);
            mockedServiceUtil.when(() -> GenericServiceUtil.hasPdfFile(casePdfFiles, 0)).thenReturn(true);
            mockedServiceUtil.when(() -> GenericServiceUtil.findClaimantLanguage(caseTestData.getCaseData()))
                .thenReturn(ENGLISH_LANGUAGE);
            mockedServiceUtil.when(() -> GenericServiceUtil.findClaimantFirstNameByCaseDataUserInfo(any(), any()))
                .thenReturn(caseTestData.getCaseData().getClaimantIndType().getClaimantFirstNames());
            mockedServiceUtil.when(() -> GenericServiceUtil.findClaimantLastNameByCaseDataUserInfo(any(), any()))
                .thenReturn(caseTestData.getCaseData().getClaimantIndType().getClaimantLastName());
            mockedServiceUtil.when(() -> GenericServiceUtil.findPdfFileBySelectedLanguage(any(), anyString()))
                .thenReturn(TEST_SUBMIT_CASE_PDF_FILE_RESPONSE.getBytes());
            NotificationService notificationService =
                new NotificationService(notificationClient, notificationsProperties);
            notificationService.sendSubmitCaseConfirmationEmail(
                caseTestData.getCaseRequest(),
                caseTestData.getCaseData(),
                caseTestData.getUserInfo(),
                casePdfFiles
            );
            mockedServiceUtil.verify(
                () -> GenericServiceUtil.logException(anyString(),
                                                      anyString(),
                                                      eq(null),
                                                      anyString(),
                                                      anyString()),
                times(1)
            );
        }
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("retrieveSendDocUploadErrorEmailPdfFilesArguments")
    void shouldSuccessfullySendDocUploadErrorEmailWithGivenPdfFilesList(List<PdfDecodedMultipartFile> casePdfFiles,
                                                                        List<PdfDecodedMultipartFile> acasCertificates,
                                                                        UploadedDocumentType claimDescriptionDocument) {
        when(notificationsProperties.getEt1ServiceOwnerNotificationEmail()).thenReturn(TestConstants.TEST_EMAIL);
        when(notificationsProperties.getEt1EcmDtsCoreTeamSlackNotificationEmail()).thenReturn(TestConstants.TEST_EMAIL);
        when(notificationsProperties.getSubmitCaseDocUploadErrorEmailTemplateId())
            .thenReturn(TestConstants.DOC_UPLOAD_ERROR_EMAIL_TEMPLATE_ID);
        when(notificationClient.sendEmail(TestConstants.TEST_TEMPLATE_API_KEY, TestConstants.TEST_EMAIL, parameters,
                                          TestConstants.REFERENCE_STRING))
            .thenReturn(TestConstants.SEND_EMAIL_RESPONSE_DOC_UPLOAD_FAILURE);

        CaseRequest caseRequest = CaseRequest.builder().build();
        caseRequest.setCaseId("1_231_231");

        SendEmailResponse sendEmailResponse = notificationService.sendDocUploadErrorEmail(caseRequest,
                                                                                          casePdfFiles,
                                                                                          acasCertificates,
                                                                                          claimDescriptionDocument);
        assertThat(sendEmailResponse.getFromEmail()).isPresent();
        assertThat(sendEmailResponse.getFromEmail()).asString()
            .isEqualTo("Optional[" + TestConstants.TEST_EMAIL + "]");
    }

    @SneakyThrows
    @Test
    void shouldThrowNotificationExceptionWhenNotAbleToSendDocUploadErrorEmail() {
        try (MockedStatic<GenericServiceUtil> mockedServiceUtil = Mockito.mockStatic(GenericServiceUtil.class)) {
            when(notificationClient.sendEmail(any(), any(), any(), any())).thenThrow(new NotificationClientException(
                new Exception("Error while trying to send doc upload error notification to service owner")));
            when(notificationsProperties.getSubmitCaseDocUploadErrorEmailTemplateId()).thenReturn(null);
            when(notificationsProperties.getEt1EcmDtsCoreTeamSlackNotificationEmail()).thenReturn(null);
            mockedServiceUtil.when(() -> GenericServiceUtil.prepareUpload(any(), anyInt()))
                .thenReturn(TestConstants.FILE_NOT_EXISTS);
            UploadedDocumentType claimDescriptionDocument = new UploadedDocumentType();
            notificationService.sendDocUploadErrorEmail(
                caseTestData.getCaseRequest(),
                List.of(TestConstants.PDF_DECODED_MULTIPART_FILE1),
                List.of(TestConstants.PDF_DECODED_MULTIPART_FILE1),
                claimDescriptionDocument);
            mockedServiceUtil.verify(
                () -> GenericServiceUtil.logException(anyString(),
                                                      eq(null),
                                                      anyString(),
                                                      anyString(),
                                                      anyString()),
                times(1)
            );
        }
    }

    private static Stream<Arguments> retrieveSubmitCaseConfirmationEmailPdfFilesArguments() {
        return TestData.generateSubmitCaseConfirmationEmailPdfFilesArguments();
    }

    private static Stream<Arguments> retrieveSendDocUploadErrorEmailPdfFilesArguments() {
        return TestData.generateSendDocUploadErrorEmailPdfFilesArguments();
    }

    @Test
    void shouldSendCopyYesEmail() throws NotificationClientException, IOException {
        when(notificationClient.sendEmail(
            eq("Yes"),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(testData.getSendEmailResponse());

        assertThat(notificationService.sendAcknowledgementEmailToClaimant(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            testData.getClaimantApplication()
        ).getNotificationId()).isEqualTo(NOTIFICATION_CONFIRMATION_ID);
    }

    @Test
    void shouldSendCopyNoEmail() throws NotificationClientException, IOException {
        testData.getClaimantApplication().setCopyToOtherPartyYesOrNo("No");
        when(notificationClient.sendEmail(
            eq("No"),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(testData.getSendEmailResponse());

        assertThat(notificationService.sendAcknowledgementEmailToClaimant(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            testData.getClaimantApplication()
        ).getNotificationId()).isEqualTo(NOTIFICATION_CONFIRMATION_ID);
    }

    @Test
    void shouldSendTypeCEmail() throws NotificationClientException, IOException {
        testData.getClaimantApplication().setContactApplicationType(WITNESS);
        when(notificationClient.sendEmail(
            eq("C"),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(testData.getSendEmailResponse());

        assertThat(notificationService.sendAcknowledgementEmailToClaimant(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            testData.getClaimantApplication()
        ).getNotificationId()).isEqualTo(NOTIFICATION_CONFIRMATION_ID);
    }

    @Test
    void shouldSendEmailToRespondentTypeB() throws NotificationClientException {
        notificationService.sendAcknowledgementEmailToRespondents(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            "Test Respondent Organisation -1-, Mehmet Tahir Dede, Abuzer Kadayif, Kate Winslet, Jeniffer Lopez",
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            null,
            testData.getClaimantApplication()
        );

        verify(notificationClient, times(5)).sendEmail(
            eq("B"),
            any(),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @SneakyThrows
    @Test
    void shouldSendEmailToRespondentTypeA() {
        testData.getClaimantApplication().setContactApplicationType("strike");
        notificationService.sendAcknowledgementEmailToRespondents(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            "Test Respondent Organisation -1-, Mehmet Tahir Dede, Abuzer Kadayif, Kate Winslet, Jeniffer Lopez",
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            null,
            testData.getClaimantApplication()
        );

        verify(notificationClient, times(5)).sendEmail(
            eq("A"),
            any(),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldNotSendEmailToRespondentTypeC() throws NotificationClientException {
        testData.getClaimantApplication().setContactApplicationType(WITNESS);
        notificationService.sendAcknowledgementEmailToRespondents(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            "Test Respondent Organisation -1-, Mehmet Tahir Dede, Abuzer Kadayif, Kate Winslet, Jeniffer Lopez",
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            null,
            testData.getClaimantApplication()
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldSendEmailToTribunalTypeAOrB() throws NotificationClientException {
        notificationService.sendAcknowledgementEmailToTribunal(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            testData.getClaimantApplication()
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(testData.getCaseData().getTribunalCorrespondenceEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );

    }

    @Test
    void shouldSendEmailToTribunalTypeC() throws NotificationClientException {
        testData.getClaimantApplication().setContactApplicationType(WITNESS);
        notificationService.sendAcknowledgementEmailToTribunal(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            testData.getClaimantApplication()
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(testData.getCaseData().getTribunalCorrespondenceEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldNotSendEmailToTribunalUnassignedManagingOffice() throws NotificationClientException {
        testData.getCaseData().setManagingOffice(UNASSIGNED_OFFICE);
        notificationService.sendAcknowledgementEmailToTribunal(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            testData.getClaimantApplication()
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            any(),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldSendResponseEmailToTribunal() throws NotificationClientException {
        testData.getCaseData().setTribunalCorrespondenceEmail("tribunal@test.com");
        notificationService.sendResponseEmailToTribunal(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE,
            false
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(testData.getCaseData().getTribunalCorrespondenceEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldNotSendResponseEmailToTribunal() throws NotificationClientException {
        testData.getCaseData().setManagingOffice(UNASSIGNED_OFFICE);
        notificationService.sendResponseEmailToTribunal(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE,
            false
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getTribunalCorrespondenceEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldSendResponseEmailToClaimant() throws NotificationClientException {
        notificationService.sendResponseEmailToClaimant(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE,
            "No",
            false
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldNotSendResponseWhenClaimantEmailDoesNotExist() throws NotificationClientException {
        testData.getCaseData().getClaimantType().setClaimantEmailAddress("");
        notificationService.sendResponseEmailToClaimant(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE,
            "No",
            false
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldNotSendResponseEmailToClaimantForTypeCApplication() throws NotificationClientException {
        notificationService.sendResponseEmailToClaimant(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            WITNESS,
            "No",
            false
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldSendResponseEmailToRespondent() throws NotificationClientException {
        notificationService.sendResponseEmailToRespondent(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE,
            YES
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(testData.getCaseData().getRespondentCollection().get(0).getValue().getRespondentEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldNotSendResponseEmailToRespondent() throws NotificationClientException {
        notificationService.sendResponseEmailToRespondent(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE,
            NO
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getRespondentCollection().get(0).getValue().getRespondentEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldSendResponseEmailToRespondentResp() throws NotificationClientException {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentEmail("test@resRep.com");

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        respondentSumTypeItem.setId(String.valueOf(UUID.randomUUID()));

        CaseData caseData = testData.getCaseData();
        caseData.getRespondentCollection().add(respondentSumTypeItem);

        notificationService.sendResponseEmailToRespondent(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE,
            YES
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq("test@resRep.com"),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldNotSendResponseWhenRespondentEmailDoesNotExist() throws NotificationClientException {
        testData.getCaseData().getClaimantType().setClaimantEmailAddress("");
        notificationService.sendResponseEmailToRespondent(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE,
            YES
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldNotSendResponseEmailToRespondentForTypeCApplication() throws NotificationClientException {
        notificationService.sendResponseEmailToRespondent(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            WITNESS,
            YES
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void sendResponseNotificationEmailToTribunal() throws NotificationClientException {
        testData.getCaseData().setTribunalCorrespondenceEmail("tribunal@test.com");
        notificationService.sendResponseNotificationEmailToTribunal(
            testData.getCaseData(),
            testData.getExpectedDetails().getId().toString()
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(testData.getCaseData().getTribunalCorrespondenceEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void sendNotResponseNotificationEmailToTribunalMissingEmail() throws NotificationClientException {
        testData.getCaseData().setTribunalCorrespondenceEmail(null);
        notificationService.sendResponseNotificationEmailToTribunal(
            testData.getCaseData(),
            testData.getExpectedDetails().getId().toString()
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void sendResponseNotificationEmailToRespondent() throws NotificationClientException {
        notificationService.sendResponseNotificationEmailToRespondent(
            testData.getCaseData(),
            testData.getExpectedDetails().getId().toString(),
            YES
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(testData.getCaseData().getRespondentCollection().get(0).getValue().getRespondentEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void sendNotResponseNotificationEmailToRespondentDoNotCopy() throws NotificationClientException {
        notificationService.sendResponseNotificationEmailToRespondent(
            testData.getCaseData(),
            testData.getExpectedDetails().getId().toString(),
            NO
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void sendNotResponseNotificationEmailToRespondentMissingEmail() throws NotificationClientException {
        for (RespondentSumTypeItem respondentSumTypeItem : testData.getCaseData().getRespondentCollection()) {
            respondentSumTypeItem.getValue().setRespondentEmail(null);
        }
        notificationService.sendResponseNotificationEmailToRespondent(
            testData.getCaseData(),
            testData.getExpectedDetails().getId().toString(),
            YES
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void sendResponseNotificationEmailToClaimant() throws NotificationClientException {
        notificationService.sendResponseNotificationEmailToClaimant(
            testData.getCaseData(),
            testData.getExpectedDetails().getId().toString(),
            YES
        );

        verify(notificationClient, times(1)).sendEmail(
            eq("claimantResponseYesTemplateId"),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void sendResponseNotificationEmailToClaimantDoNotCopy() throws NotificationClientException {
        notificationService.sendResponseNotificationEmailToClaimant(
            testData.getCaseData(),
            testData.getExpectedDetails().getId().toString(),
            NO
        );

        verify(notificationClient, times(1)).sendEmail(
            eq("claimantResponseNoTemplateId"),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void sendNotResponseNotificationEmailToClaimantMissingEmail() throws NotificationClientException {
        testData.getCaseData().getClaimantType().setClaimantEmailAddress(null);
        notificationService.sendResponseNotificationEmailToClaimant(
            testData.getCaseData(),
            testData.getExpectedDetails().getId().toString(),
            YES
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            any(),
            any(),
            any()
        );
    }

    @ParameterizedTest
    @MethodSource("responseToRequestArguments")
    void sendResponseToRequestNotificationEmailToClaimant(String yesOrNo, String template)
        throws NotificationClientException {
        notificationService.sendResponseEmailToClaimant(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE,
            yesOrNo,
            true
        );

        verify(notificationClient, times(1)).sendEmail(
            eq(template),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void shouldSendResponseToRequestEmailToTribunal() throws NotificationClientException {
        testData.getCaseData().setTribunalCorrespondenceEmail("tribunal@test.com");
        notificationService.sendResponseEmailToTribunal(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE,
            true
        );

        verify(notificationClient, times(1)).sendEmail(
            eq("tseTribunalResponseToRequestTemplateId"),
            eq(testData.getCaseData().getTribunalCorrespondenceEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    private static Stream<Arguments> responseToRequestArguments() {
        return Stream.of(
            Arguments.of("Yes", "tseClaimantResponseToRequestYesTemplateId"),
            Arguments.of("No", "tseClaimantResponseToRequestNoTemplateId")
        );
    }
}
