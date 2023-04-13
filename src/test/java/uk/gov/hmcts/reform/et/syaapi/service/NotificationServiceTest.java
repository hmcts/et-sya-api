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
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.et.syaapi.service.util.ServiceUtil;
import uk.gov.hmcts.reform.et.syaapi.utils.TestConstants;
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
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UNASSIGNED_OFFICE;
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
    private TestData testData;

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
        testData = new TestData();
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
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(null);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getCaseRequest(),
            testData.getCaseData(),
            testData.getUserInfo(),
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
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(TestConstants.SEND_EMAIL_RESPONSE_ENGLISH);
        testData.getCaseRequest().setCaseId(caseId);
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(null);
        List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
        casePdfFiles.add(TestConstants.PDF_DECODED_MULTIPART_FILE1);
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getCaseRequest(),
            testData.getCaseData(),
            testData.getUserInfo(),
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
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(
            WELSH_LANGUAGE.equals(selectedLanguage) ? TestConstants.SEND_EMAIL_RESPONSE_WELSH
                : ENGLISH_LANGUAGE.equals(selectedLanguage) ? TestConstants.SEND_EMAIL_RESPONSE_ENGLISH
                : TestConstants.INPUT_SEND_EMAIL_RESPONSE);
        testData.getCaseRequest().setCaseId(testData.getExpectedDetails().getId().toString());
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(selectedLanguage);
        List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
        casePdfFiles.add(TestConstants.PDF_DECODED_MULTIPART_FILE1);
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getCaseRequest(),
            testData.getCaseData(),
            testData.getUserInfo(),
            casePdfFiles
        );
        assertThat(response.getBody()).isEqualTo(expectedBody);
    }

    @SneakyThrows
    @Test
    void shouldThrowExceptionWhenSubmitCaseConfirmationEmailNotSent() {
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            when(notificationClient.sendEmail(
                anyString(),
                anyString(),
                any(),
                anyString()
            )).thenThrow(NotificationClientException.class);
            List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
            casePdfFiles.add(TestConstants.PDF_DECODED_MULTIPART_FILE1);
            mockedServiceUtil.when(() -> ServiceUtil.hasPdfFile(casePdfFiles, 0)).thenReturn(true);
            mockedServiceUtil.when(() -> ServiceUtil.findClaimantLanguage(testData.getCaseData()))
                .thenReturn(ENGLISH_LANGUAGE);
            mockedServiceUtil.when(() -> ServiceUtil.findClaimantFirstNameByCaseDataUserInfo(any(), any()))
                .thenReturn(testData.getCaseData().getClaimantIndType().getClaimantFirstNames());
            mockedServiceUtil.when(() -> ServiceUtil.findClaimantLastNameByCaseDataUserInfo(any(), any()))
                .thenReturn(testData.getCaseData().getClaimantIndType().getClaimantLastName());
            mockedServiceUtil.when(() -> ServiceUtil.findPdfFileBySelectedLanguage(any(), anyString()))
                .thenReturn(TEST_SUBMIT_CASE_PDF_FILE_RESPONSE.getBytes());
            NotificationService notificationService =
                new NotificationService(notificationClient, notificationsProperties);
            notificationService.sendSubmitCaseConfirmationEmail(
                testData.getCaseRequest(),
                testData.getCaseData(),
                testData.getUserInfo(),
                casePdfFiles
            );
            mockedServiceUtil.verify(
                () -> ServiceUtil.logException(anyString(),
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
        try (MockedStatic<ServiceUtil> mockedServiceUtil = Mockito.mockStatic(ServiceUtil.class)) {
            when(notificationClient.sendEmail(any(), any(), any(), any())).thenThrow(new NotificationClientException(
                new Exception("Error while trying to send doc upload error notification to service owner")));
            when(notificationsProperties.getSubmitCaseDocUploadErrorEmailTemplateId()).thenReturn(null);
            when(notificationsProperties.getEt1EcmDtsCoreTeamSlackNotificationEmail()).thenReturn(null);
            mockedServiceUtil.when(() -> ServiceUtil.prepareUpload(any(), anyInt()))
                .thenReturn(TestConstants.FILE_NOT_EXISTS);
            UploadedDocumentType claimDescriptionDocument = new UploadedDocumentType();
            notificationService.sendDocUploadErrorEmail(testData.getCaseRequest(),
                                                        List.of(TestConstants.PDF_DECODED_MULTIPART_FILE1),
                                                        List.of(TestConstants.PDF_DECODED_MULTIPART_FILE1),
                                                        claimDescriptionDocument);
            mockedServiceUtil.verify(
                () -> ServiceUtil.logException(anyString(),
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

    @SneakyThrows
    @Test
    void shouldSendEmailToRespondentTypeB() {
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

    @SneakyThrows
    @Test
    void shouldNotSendEmailToRespondentTypeC() {
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

    @SneakyThrows
    @Test
    void shouldSendEmailToTribunalTypeAOrB() {
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

    @SneakyThrows
    @Test
    void shouldSendEmailToTribunalTypeC() {
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

    @SneakyThrows
    @Test
    void shouldNotSendEmailToTribunalUnassignedManagingOffice() {
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

    @SneakyThrows
    @Test
    void shouldSendResponseEmailToTribunal() {
        notificationService.sendResponseEmailToTribunal(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(testData.getCaseData().getTribunalCorrespondenceEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @SneakyThrows
    @Test
    void shouldNotSendResponseEmailToTribunal() {
        testData.getCaseData().setManagingOffice(UNASSIGNED_OFFICE);
        notificationService.sendResponseEmailToTribunal(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getTribunalCorrespondenceEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @SneakyThrows
    @Test
    void shouldSendResponseEmailToClaimant()  {
        notificationService.sendResponseEmailToClaimant(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE,
            new TseRespondType()
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @SneakyThrows
    @Test
    void shouldNotSendResponseWhenClaimantEmailDoesNotExist() {
        testData.getCaseData().getClaimantType().setClaimantEmailAddress("");
        notificationService.sendResponseEmailToClaimant(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE,
            new TseRespondType()
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @SneakyThrows
    @Test
    void shouldNotSendResponseEmailToClaimantForTypeCApplication() {
        notificationService.sendResponseEmailToClaimant(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            WITNESS,
            new TseRespondType()
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @SneakyThrows
    @Test
    void shouldSendResponseEmailToRespondent() {
        notificationService.sendResponseEmailToRespondent(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(testData.getCaseData().getRespondentCollection().get(0).getValue().getRespondentEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @SneakyThrows
    @Test
    void shouldSendResponseEmailToRespondentResp() {
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
            CHANGE_DETAILS_APPLICATION_TYPE
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq("test@resRep.com"),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @SneakyThrows
    @Test
    void shouldNotSendResponseWhenRespondentEmailDoesNotExist() {
        testData.getCaseData().getClaimantType().setClaimantEmailAddress("");
        notificationService.sendResponseEmailToRespondent(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            CHANGE_DETAILS_APPLICATION_TYPE
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }

    @SneakyThrows
    @Test
    void shouldNotSendResponseEmailToRespondentForTypeCApplication() {
        notificationService.sendResponseEmailToRespondent(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            WITNESS
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }
}
