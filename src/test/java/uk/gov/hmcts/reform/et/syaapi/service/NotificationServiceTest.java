package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.et.syaapi.utils.TestConstants;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SUBMIT_CASE_PDF_FILE_RESPONSE;

@SuppressWarnings({"PMD.TooManyMethods"})
class NotificationServiceTest {

    @MockBean
    private NotificationService notificationService;
    private NotificationClient notificationClient;
    private NotificationsProperties notificationsProperties;
    private final ConcurrentHashMap<String, String> parameters = new ConcurrentHashMap<>();


    private List<PdfDecodedMultipartFile> casePdfFiles;
    private PdfDecodedMultipartFile pdfDecodedMultipartFileEt1Pdf1;
    private PdfDecodedMultipartFile pdfDecodedMultipartFileAcasPdf1;
    private PdfDecodedMultipartFile pdfDecodedMultipartFileNotNull;
    List<PdfDecodedMultipartFile> acasCertificates;
    UploadedDocumentType claimDescriptionDocument = new UploadedDocumentType();
    private TestData testData;

    @BeforeEach
    void before() throws NotificationClientException {
        parameters.put("firstname", "test");
        parameters.put("references", "123456789");
        casePdfFiles = new ArrayList<>();
        acasCertificates = new ArrayList<>();
        notificationClient = mock(NotificationClient.class);
        notificationsProperties = mock(NotificationsProperties.class);
        notificationService = new NotificationService(notificationClient, notificationsProperties);
        pdfDecodedMultipartFileEt1Pdf1 = new PdfDecodedMultipartFile(
            TEST_SUBMIT_CASE_PDF_FILE_RESPONSE.getBytes(),
            TestConstants.TEST_PDF_FILE_ORIGINAL_NAME,
            TestConstants.TEST_PDF_FILE_CONTENT_TYPE,
            TestConstants.TEST_PDF_FILE_DOCUMENT_DESCRIPTION
        );
        pdfDecodedMultipartFileAcasPdf1 = new PdfDecodedMultipartFile(
            TEST_SUBMIT_CASE_PDF_FILE_RESPONSE.getBytes(),
            TestConstants.TEST_PDF_FILE_ORIGINAL_NAME,
            TestConstants.TEST_PDF_FILE_CONTENT_TYPE,
            TestConstants.TEST_PDF_FILE_DOCUMENT_DESCRIPTION
        );
        pdfDecodedMultipartFileNotNull = new PdfDecodedMultipartFile(
            TEST_SUBMIT_CASE_PDF_FILE_RESPONSE.getBytes(),
            TestConstants.TEST_PDF_FILE_ORIGINAL_NAME,
            TestConstants.TEST_PDF_FILE_CONTENT_TYPE,
            TestConstants.TEST_PDF_FILE_DOCUMENT_DESCRIPTION
        );
        casePdfFiles.add(pdfDecodedMultipartFileEt1Pdf1);
        PdfDecodedMultipartFile pdfDecodedMultipartFileEt1Pdf2 = new PdfDecodedMultipartFile(
            TEST_SUBMIT_CASE_PDF_FILE_RESPONSE.getBytes(),
            TestConstants.TEST_PDF_FILE_ORIGINAL_NAME,
            TestConstants.TEST_PDF_FILE_CONTENT_TYPE,
            TestConstants.TEST_PDF_FILE_DOCUMENT_DESCRIPTION
        );
        casePdfFiles.add(pdfDecodedMultipartFileEt1Pdf2);
        acasCertificates.add(pdfDecodedMultipartFileAcasPdf1);
        PdfDecodedMultipartFile pdfDecodedMultipartFileAcasPdf2 = new PdfDecodedMultipartFile(
            TEST_SUBMIT_CASE_PDF_FILE_RESPONSE.getBytes(),
            TestConstants.TEST_PDF_FILE_ORIGINAL_NAME,
            TestConstants.TEST_PDF_FILE_CONTENT_TYPE,
            TestConstants.TEST_PDF_FILE_DOCUMENT_DESCRIPTION
        );
        acasCertificates.add(pdfDecodedMultipartFileAcasPdf2);
        given(notificationClient.sendEmail(anyString(), anyString(), any(), anyString()))
            .willReturn(TestConstants.INPUT_SEND_EMAIL_RESPONSE);
        given(notificationsProperties.getCySubmitCaseEmailTemplateId())
            .willReturn(TestConstants.WELSH_DUMMY_PDF_TEMPLATE_ID);
        given(notificationsProperties.getSubmitCaseEmailTemplateId())
            .willReturn(TestConstants.SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID);
        given(notificationsProperties.getCitizenPortalLink()).willReturn(TestConstants.REFERENCE_STRING);
        testData = new TestData();
    }

    @SneakyThrows
    @Test
    void shouldSendEmailByMockingResponse() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
        assertThat(sendEmailResponse.getReference().isPresent()).isTrue();
        assertThat(sendEmailResponse.getReference().get()).isEqualTo(TestConstants.REFERENCE_STRING);
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
        String actualValue = response == null ? "Empty Response" : response.getBody();
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @SneakyThrows
    @Test
    void shouldSendSubmitCaseConfirmationEmailInEnglishWhenSelectedLanguageIsNull() {
        when(notificationClient.sendEmail(
            anyString(),
            anyString(),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(TestConstants.SEND_EMAIL_RESPONSE_ENGLISH);
        testData.getCaseRequest().setCaseId(testData.getExpectedDetails().getId().toString());
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(null);
        List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
        casePdfFiles.add(pdfDecodedMultipartFileNotNull);
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getCaseRequest(),
            testData.getCaseData(),
            testData.getUserInfo(),
            casePdfFiles
        );
        assertThat(response.getBody()).isEqualTo(
            "Please click here. https://www.gov.uk/log-in-register-hmrc-online-services/123456722/?lng=en.");
    }

    @SneakyThrows
    @Test
    void shouldSendSubmitCaseConfirmationEmailInEnglishWhenSelectedLanguageIsEmpty() {
        when(notificationClient.sendEmail(
            anyString(),
            anyString(),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(TestConstants.SEND_EMAIL_RESPONSE_ENGLISH);
        testData.getCaseRequest().setCaseId(testData.getExpectedDetails().getId().toString());
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(null);
        List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
        casePdfFiles.add(pdfDecodedMultipartFileNotNull);
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getCaseRequest(),
            testData.getCaseData(),
            testData.getUserInfo(),
            casePdfFiles
        );
        assertThat(response.getBody()).isEqualTo(
            "Please click here. https://www.gov.uk/log-in-register-hmrc-online-services/123456722/?lng=en.");
    }

    @SneakyThrows
    @Test
    void shouldSendSubmitCaseConfirmationEmailInEnglishWhenCaseRequestCaseIdIsNull() {
        testData.getCaseRequest().setCaseId(null);
        when(notificationClient.sendEmail(
            eq(TestConstants.WELSH_DUMMY_PDF_TEMPLATE_ID),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(TestConstants.SEND_EMAIL_RESPONSE_ENGLISH);
        List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
        casePdfFiles.add(pdfDecodedMultipartFileNotNull);
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getCaseRequest(),
            testData.getCaseData(),
            testData.getUserInfo(),
            casePdfFiles
        );
        assertThat(response.getBody()).isEqualTo(
            "Dear test, Please see your detail as 123456789. Regards, ET Team.");
    }

    @SneakyThrows
    @Test
    void shouldThrowExceptionWhenSubmitCaseConfirmationEmailNotSent() {
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(WELSH_LANGUAGE);
        when(notificationClient.sendEmail(
            eq(TestConstants.WELSH_DUMMY_PDF_TEMPLATE_ID),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenThrow(NotificationException.class);
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(WELSH_LANGUAGE);
        List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
        casePdfFiles.add(pdfDecodedMultipartFileNotNull);
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getCaseRequest(),
            testData.getCaseData(),
            testData.getUserInfo(),
            casePdfFiles
        );
        assertThat(response.getBody()).isEqualTo(
            "Dear test, Please see your detail as 123456789. Regards, ET Team.");
    }

    @SneakyThrows
    @Test
    void shouldSendSubmitCaseConfirmationEmailInWelsh() {
        when(notificationClient.sendEmail(
            eq(TestConstants.WELSH_DUMMY_PDF_TEMPLATE_ID),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(TestConstants.SEND_EMAIL_RESPONSE_WELSH);
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(WELSH_LANGUAGE);
        List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
        casePdfFiles.add(pdfDecodedMultipartFileNotNull);
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getCaseRequest(),
            testData.getCaseData(),
            testData.getUserInfo(),
            casePdfFiles
        );
        assertThat(response.getBody()).isEqualTo(
            "Dear test, Please see your detail as 123456789. Regards, ET Team.");
    }

    @SneakyThrows
    @Test
    void shouldSuccessfullySendDocUploadErrorEmail() {
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
        assertThat(sendEmailResponse.getFromEmail().isPresent()).isTrue();
        assertThat(sendEmailResponse.getFromEmail()).asString()
            .isEqualTo("Optional[" + TestConstants.TEST_EMAIL + "]");
    }


    @Test
    void shouldThrowNotificationExceptionWhenNotAbleToSendDocUploadErrorEmail()
        throws NotificationClientException {
        when(notificationClient.sendEmail(any(), any(), any(), any()
        )).thenThrow(new NotificationException(
            new Exception("Error while trying to send doc upload error notification to service owner")));
        when(notificationsProperties.getSubmitCaseDocUploadErrorEmailTemplateId()).thenReturn(null);
        when(notificationsProperties.getEt1EcmDtsCoreTeamSlackNotificationEmail()).thenReturn(null);

        List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
        casePdfFiles.add(pdfDecodedMultipartFileEt1Pdf1);
        List<PdfDecodedMultipartFile> acasCertificates = new ArrayList<>();
        acasCertificates.add(pdfDecodedMultipartFileAcasPdf1);
        UploadedDocumentType claimDescriptionDocument = new UploadedDocumentType();
        NotificationException notificationException = assertThrows(NotificationException.class, () ->
            notificationService.sendDocUploadErrorEmail(testData.getCaseRequest(),
                                                        casePdfFiles,
                                                        acasCertificates,
                                                        claimDescriptionDocument));
        assertThat(notificationException.getMessage())
            .isEqualTo("java.lang.Exception: Error while trying to send doc upload"
                           + " error notification to service owner");
    }

    private static Stream<Arguments> retrieveSubmitCaseConfirmationEmailPdfFilesArguments() {
        return TestData.submitCaseConfirmationEmailPdfFilesArguments();
    }
}
