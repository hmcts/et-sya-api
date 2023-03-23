package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.DOC_UPLOAD_ERROR_EMAIL_TEMPLATE_ID;

@SuppressWarnings({"PMD.TooManyMethods"})
class NotificationServiceTest {
    private static final String TEST_TEMPLATE_API_KEY =
        "mtd_test-002d2170-e381-4545-8251-5e87dab724e7-ac8ef473-1f28-4bfc-8906-9babd92dc5d8";
    private static final String SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID = "af0b26b7-17b6-4643-bbdc-e296d11e7b0c";
    private static final String REFERENCE_STRING = "TEST_EMAIL_ALERT";
    private static final String TEST_EMAIL = "TEST@GMAIL.COM";

    @MockBean
    private NotificationService notificationService;
    private NotificationClient notificationClient;
    private NotificationsProperties notificationsProperties;
    private final ConcurrentHashMap<String, String> parameters = new ConcurrentHashMap<>();
    private SendEmailResponse inputSendEmailResponse;
    private TestData testData;

    @BeforeEach
    void before() throws NotificationClientException {
        parameters.put("firstname", "test");
        parameters.put("references", "123456789");
        inputSendEmailResponse = new SendEmailResponse("{\n"
                   + "  \"id\": \"f30b2148-b1a6-4c0d-8a10-50109c96dc2c\",\n"
                   + "  \"reference\": \"TEST_EMAIL_ALERT\",\n"
                   + "  \"template\": {\n"
                   + "    \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n"
                   + "    \"version\": \"3\",\n"
                   + "    \"uri\": \"TEST\"\n"
                   + "  },\n"
                   + "  \"content\": {\n"
                   + "    \"body\": \"Dear test, Please see your detail as 123456789. Regards, ET Team.\",\n"
                   + "    \"subject\": \"ET Test email created\",\n"
                   + "    \"from_email\": \"TEST@GMAIL.COM\"\n"
                   + "  }\n"
                   + "}\n");
        notificationClient = mock(NotificationClient.class);
        notificationsProperties = mock(NotificationsProperties.class);
        notificationService = new NotificationService(notificationClient, notificationsProperties);
        given(notificationClient.sendEmail(anyString(), anyString(), any(), anyString()))
            .willReturn(inputSendEmailResponse);
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
        doReturn(inputSendEmailResponse).when(notificationClient).sendEmail(TEST_TEMPLATE_API_KEY,
                                                                            TEST_EMAIL, parameters,
                                                                            REFERENCE_STRING
        );
        return notificationService.sendEmail(TEST_TEMPLATE_API_KEY,
                                             TEST_EMAIL, parameters,
                                             REFERENCE_STRING
        );
    }

    @Test
    void shouldSendSubmitCaseConfirmationEmailNull() {
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(null);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getExpectedDetails(),
            testData.getCaseData(),
            testData.getUserInfo(),
            new byte[0]
        );
        assertThat(response.getBody()).isEqualTo("Dear test, Please see your detail as 123456789. Regards, ET Team.");
    }

    @SneakyThrows
    @Test
    void shouldSuccessfullySendDocUploadErrorEmail() {
        inputSendEmailResponse = new SendEmailResponse("{\n"
                                                           + "  \"id\": \"f30b2148-b1a6-4c0d-8a10-50109c96dc2c\",\n"
                                                           + "  \"reference\": \"TEST_EMAIL_ALERT\",\n"
                                                           + "  \"template\": {\n"
                                                           + "    \"id\": \""
                                                           + DOC_UPLOAD_ERROR_EMAIL_TEMPLATE_ID
                                                           + "\",\n"
                                                           + "    \"version\": \"3\",\n"
                                                           + "    \"uri\": \"TEST\"\n"
                                                           + "  },\n"
                                                           + "  \"content\": {\n"
                                                           + "    \"body\": \"Dear test, "
                                                           + "Please see your detail as 123456789. Regards, "
                                                           + "ET Team.\",\n"
                                                           + "    \"subject\": \"ET Test email created\",\n"
                                                           + "    \"from_email\": \"" + TEST_EMAIL + "\"\n"
                                                           + "  }\n"
                                                           + "}\n");
        when(notificationsProperties.getEt1ServiceOwnerNotificationEmail()).thenReturn(TEST_EMAIL);
        when(notificationsProperties.getEt1EcmDtsCoreTeamSlackNotificationEmail()).thenReturn(TEST_EMAIL);
        when(notificationsProperties.getSubmitCaseDocUploadErrorEmailTemplateId())
            .thenReturn(DOC_UPLOAD_ERROR_EMAIL_TEMPLATE_ID);
        when(notificationClient.sendEmail(TEST_TEMPLATE_API_KEY,TEST_EMAIL, parameters, REFERENCE_STRING))
            .thenReturn(inputSendEmailResponse);

        CaseDetails caseDetails = CaseDetails.builder().build();
        caseDetails.setId(1_231_231L);
        CaseData caseData = new CaseData();
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantFirstNames("TestFName");
        claimantIndType.setClaimantLastName("TestLName");
        caseData.setClaimantIndType(claimantIndType);
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantAddressUK(new Address());
        claimantType.setClaimantEmailAddress(TEST_EMAIL);
        ClaimantHearingPreference hearingPreference = new ClaimantHearingPreference();
        hearingPreference.setContactLanguage(EtSyaConstants.ENGLISH_LANGUAGE);
        caseData.setClaimantHearingPreference(hearingPreference);
        caseData.setClaimantType(claimantType);
        caseData.getClaimantHearingPreference().setContactLanguage(EtSyaConstants.ENGLISH_LANGUAGE);
        byte[] acasPdfData = new byte[2];
        byte[] et1PdfData = new byte[2];

        SendEmailResponse sendEmailResponse = notificationService.sendDocUploadErrorEmail(caseDetails,
                                                                                          acasPdfData,
                                                                                          et1PdfData);
        assertThat(sendEmailResponse.getFromEmail().isPresent()).isTrue();
        assertThat(sendEmailResponse.getFromEmail()).asString()
            .isEqualTo("Optional[" + TEST_EMAIL + "]");
    }

    @Test
    void shouldThrowNotificationExceptionWhenNotAbleToSendDocUploadErrorEmail()
        throws NotificationClientException {
        ConcurrentHashMap<String, String> testHashMap = new ConcurrentHashMap<>();
        testHashMap.put("testKey", "testValue");

        when(notificationClient.sendEmail(any(), any(), any(), any()
        )).thenThrow(new NotificationException(
            new Exception("Error while trying to send doc upload error notification to service owner")));
        when(notificationsProperties.getSubmitCaseDocUploadErrorEmailTemplateId()).thenReturn(null);
        when(notificationsProperties.getEt1EcmDtsCoreTeamSlackNotificationEmail()).thenReturn(null);

        byte[] testEt1PdfByteArray = "test et1 Pdf String".getBytes();
        byte[] testAcasPdfByteArray = "test Acas padf String".getBytes();

        NotificationException notificationException = assertThrows(NotificationException.class, () ->
            notificationService.sendDocUploadErrorEmail(testData.getExpectedDetails(),
                                                        testEt1PdfByteArray,
                                                        testAcasPdfByteArray));
        assertThat(notificationException.getMessage())
            .isEqualTo("java.lang.Exception: Error while trying to send doc upload"
                       + " error notification to service owner");
    }
}
