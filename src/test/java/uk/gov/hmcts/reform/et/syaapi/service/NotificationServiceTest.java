package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.DOC_UPLOAD_ERROR_EMAIL_TEMPLATE_ID;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.INPUT_SEND_EMAIL_RESPONSE;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.SEND_EMAIL_RESPONSE_DOC_UPLOAD_FAILURE;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.SEND_EMAIL_RESPONSE_ENGLISH;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.SEND_EMAIL_RESPONSE_WELSH;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_EMAIL;


@SuppressWarnings({"PMD.TooManyMethods"})
class NotificationServiceTest {
    private static final String TEST_TEMPLATE_API_KEY =
        "mtd_test-002d2170-e381-4545-8251-5e87dab724e7-ac8ef473-1f28-4bfc-8906-9babd92dc5d8";
    private static final String SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID = "af0b26b7-17b6-4643-bbdc-e296d11e7b0c";
    private static final String REFERENCE_STRING = "TEST_EMAIL_ALERT";
    @MockBean
    private NotificationService notificationService;
    private NotificationClient notificationClient;
    private NotificationsProperties notificationsProperties;
    private final ConcurrentHashMap<String, String> parameters = new ConcurrentHashMap<>();


    private List<PdfDecodedMultipartFile> casePdfFiles;
    private PdfDecodedMultipartFile pdfDecodedMultipartFileEt1Pdf1;
    private PdfDecodedMultipartFile pdfDecodedMultipartFileAcasPdf1;
    private PdfDecodedMultipartFile pdfDecodedMultipartFileNull;
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
            "test et1 Pdf String 1".getBytes(),
            "ET1_PDF1",
            "ET1 PDF1 Content Type",
            "Test Document Description ET1 PDF1"
        );
        pdfDecodedMultipartFileAcasPdf1 = new PdfDecodedMultipartFile(
            "test Acas pdf String 1".getBytes(),
            "ACAS_PDF1",
            "ACAS PDF1 Content Type",
            "ACAS PDF1"
        );

        pdfDecodedMultipartFileNull = new PdfDecodedMultipartFile(
            null,
            "MultiPartPdfFileNull",
            "Empty Content",
            "Null Multipart PDF File"
        );
        pdfDecodedMultipartFileNotNull = new PdfDecodedMultipartFile(
            new byte[2],
            "MultiPartPdfFileNotNull",
            "Not Null Content",
            "Not Null Multipart PDF File"
        );
        casePdfFiles.add(pdfDecodedMultipartFileEt1Pdf1);
        PdfDecodedMultipartFile pdfDecodedMultipartFileEt1Pdf2 = new PdfDecodedMultipartFile(
            "test et1 Pdf String 2".getBytes(),
            "Test Name",
            "Test Content Type",
            "Test Document Description"
        );
        casePdfFiles.add(pdfDecodedMultipartFileEt1Pdf2);
        acasCertificates.add(pdfDecodedMultipartFileAcasPdf1);
        PdfDecodedMultipartFile pdfDecodedMultipartFileAcasPdf2 = new PdfDecodedMultipartFile(
            "test Acas pdf String 2".getBytes(),
            "Test Name",
            "Test Content Type",
            "Test Document Description"
        );
        acasCertificates.add(pdfDecodedMultipartFileAcasPdf2);
        given(notificationClient.sendEmail(anyString(), anyString(), any(), anyString()))
            .willReturn(INPUT_SEND_EMAIL_RESPONSE);
        given(notificationsProperties.getCySubmitCaseEmailTemplateId()).willReturn("1234_welsh");
        given(notificationsProperties.getSubmitCaseEmailTemplateId())
            .willReturn(SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID);
        given(notificationsProperties.getCitizenPortalLink()).willReturn(REFERENCE_STRING);
        testData = new TestData();
    }

    @SneakyThrows
    @Test
    void shouldSendEmailByMockingResponse() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
        assertThat(sendEmailResponse.getReference().isPresent()).isTrue();
        assertThat(sendEmailResponse.getReference().get()).isEqualTo(REFERENCE_STRING);
    }

    @SneakyThrows
    @Test
    void shouldRetrieveTemplateIdCorrectly() {
        SendEmailResponse sendEmailResponse = mockSendEmailResponse();
        assertThat(sendEmailResponse.getTemplateId()).isEqualTo(
            UUID.fromString("8835039a-3544-439b-a3da-882490d959eb"));
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
            () -> notificationService.sendEmail(TEST_TEMPLATE_API_KEY, null, parameters, REFERENCE_STRING))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("email_address is a required property");
    }


    @Test
    void ifTemplateIdIsNullWillThrowNotificationException() throws NotificationClientException {
        given(notificationClient.sendEmail(nullable(String.class), anyString(), any(), anyString()))
            .willThrow(new NotificationClientException("template_id is a required property"));
        assertThatThrownBy(
            () -> notificationService.sendEmail(null, TEST_EMAIL, parameters, REFERENCE_STRING))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("template_id is a required property");
    }

    @Test
    void ifTemplateNotFoundWillThrowNotificationException() throws NotificationClientException {

        given(notificationClient.sendEmail(anyString(), anyString(), any(), anyString()))
            .willThrow(new NotificationClientException("Template not found"));
        assertThatThrownBy(
            () -> notificationService.sendEmail(TEST_TEMPLATE_API_KEY, TEST_EMAIL, parameters, REFERENCE_STRING))
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("Template not found");
    }

    @SneakyThrows
    private SendEmailResponse mockSendEmailResponse() {
        notificationClient = mock(NotificationClient.class);
        notificationService = new NotificationService(notificationClient, notificationsProperties);
        doReturn(INPUT_SEND_EMAIL_RESPONSE).when(notificationClient).sendEmail(TEST_TEMPLATE_API_KEY,
                                                                            TEST_EMAIL, parameters, REFERENCE_STRING
        );
        return notificationService.sendEmail(TEST_TEMPLATE_API_KEY,
                                             TEST_EMAIL, parameters, REFERENCE_STRING
        );
    }

    @Test
    void shouldNotSendSubmitCaseConfirmationEmailWhenCasePdfFilesIsNull() {
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(null);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getCaseRequest(),
            testData.getCaseData(),
            testData.getUserInfo(),
            null
        );
        assertThat(response).isNull();
    }

    @Test
    void shouldNotSendSubmitCaseConfirmationEmailWhenCasePdfFilesIsEmpty() {
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(null);
        List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getCaseRequest(),
            testData.getCaseData(),
            testData.getUserInfo(),
            casePdfFiles
        );
        assertThat(response).isNull();
    }

    @Test
    void shouldNotSendSubmitCaseConfirmationEmailWhenCasePdfFilesFirstContentIsEmpty() {
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(null);
        List<PdfDecodedMultipartFile> casePdfFiles = new ArrayList<>();
        casePdfFiles.add(pdfDecodedMultipartFileNull);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getCaseRequest(),
            testData.getCaseData(),
            testData.getUserInfo(),
            casePdfFiles
        );
        assertThat(response).isNull();
    }

    @SneakyThrows
    @Test
    void shouldSendSubmitCaseConfirmationEmailInEnglish() {
        when(notificationClient.sendEmail(
            eq("1234_welsh"),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(SEND_EMAIL_RESPONSE_ENGLISH);
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(EtSyaConstants.WELSH_LANGUAGE);
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
            eq("1234_welsh"),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(SEND_EMAIL_RESPONSE_WELSH);
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(EtSyaConstants.WELSH_LANGUAGE);
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
        when(notificationsProperties.getEt1ServiceOwnerNotificationEmail()).thenReturn(TEST_EMAIL);
        when(notificationsProperties.getEt1EcmDtsCoreTeamSlackNotificationEmail()).thenReturn(TEST_EMAIL);
        when(notificationsProperties.getSubmitCaseDocUploadErrorEmailTemplateId())
            .thenReturn(DOC_UPLOAD_ERROR_EMAIL_TEMPLATE_ID);
        when(notificationClient.sendEmail(TEST_TEMPLATE_API_KEY,TEST_EMAIL, parameters, REFERENCE_STRING))
            .thenReturn(SEND_EMAIL_RESPONSE_DOC_UPLOAD_FAILURE);

        CaseRequest caseRequest = CaseRequest.builder().build();
        caseRequest.setCaseId("1_231_231");

        SendEmailResponse sendEmailResponse = notificationService.sendDocUploadErrorEmail(caseRequest,
                                                                                          casePdfFiles,
                                                                                          acasCertificates,
                                                                                          claimDescriptionDocument);
        assertThat(sendEmailResponse.getFromEmail().isPresent()).isTrue();
        assertThat(sendEmailResponse.getFromEmail()).asString()
            .isEqualTo("Optional[" + TEST_EMAIL + "]");
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
}
