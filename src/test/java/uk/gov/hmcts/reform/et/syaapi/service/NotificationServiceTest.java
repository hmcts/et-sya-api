package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.io.IOException;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UNASSIGNED_OFFICE;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.NOTIFICATION_CONFIRMATION_ID;

@SuppressWarnings({"PMD.TooManyMethods"})
class NotificationServiceTest {
    public static final String CLAIMANT = "Michael Jackson";
    public static final String NOT_SET = "Not set";
    public static final String TEST_RESPONDENT = "Test Respondent";
    @MockBean
    private NotificationService notificationService;
    private static final String TEST_TEMPLATE_API_KEY = "dummy template id";
    private static final String SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID = "af0b26b7-17b6-4643-bbdc-e296d11e7b0c";
    private static final String REFERENCE_STRING = "TEST_EMAIL_ALERT";
    private static final String TEST_EMAIL = "TEST@GMAIL.COM";
    private NotificationClient notificationClient;
    private NotificationsProperties notificationsProperties;
    private final ConcurrentHashMap<String, String> parameters = new ConcurrentHashMap<>();
    private SendEmailResponse inputSendEmailResponse;
    private TestData testData;

    @BeforeEach
    void before() throws NotificationClientException {
        parameters.put("firstname", "test");
        parameters.put("references", "123456789");
        inputSendEmailResponse = new SendEmailResponse(
            "{\n"
                + "  \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n"
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
        given(notificationsProperties.getClaimantTseEmailNoTemplateId()).willReturn("No");
        given(notificationsProperties.getClaimantTseEmailYesTemplateId()).willReturn("Yes");
        given(notificationsProperties.getClaimantTseEmailTypeCTemplateId()).willReturn("C");
        given(notificationsProperties.getTribunalAcknowledgementTemplateId()).willReturn("Tribunal");
        given(notificationsProperties.getRespondentTseEmailTypeATemplateId()).willReturn("A");
        given(notificationsProperties.getRespondentTseEmailTypeBTemplateId()).willReturn("B");
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
            UUID.fromString("8835039a-3544-439b-a3da-882490d959eb"));
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
                                                                            TEST_EMAIL, parameters, REFERENCE_STRING
        );
        return notificationService.sendEmail(TEST_TEMPLATE_API_KEY,
                                             TEST_EMAIL, parameters, REFERENCE_STRING
        );
    }

    @Test
    void shouldSendSubmitCaseConfirmationEmail() throws IOException {
        when(notificationService.sendSubmitCaseConfirmationEmail(
            testData.getExpectedDetails(),
            testData.getCaseData(),
            testData.getUserInfo()
        ))
            .thenReturn(testData.getSendEmailResponse());
        assertThat(notificationService.sendSubmitCaseConfirmationEmail(
            testData.getExpectedDetails(),
            testData.getCaseData(),
            testData.getUserInfo()
        ).getNotificationId()).isEqualTo(NOTIFICATION_CONFIRMATION_ID);
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
        testData.getClaimantApplication().setContactApplicationType("witness");
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

    @Test
    void shouldSendEmailToRespondentTypeA() throws NotificationClientException {
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
        testData.getClaimantApplication().setContactApplicationType("witness");
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
        testData.getClaimantApplication().setContactApplicationType("witness");
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
    void shouldThrowNotificationExceptionWhenNotAbleToSendEmailBySendSubmitCaseConfirmationEmail()
        throws NotificationClientException {
        when(notificationClient.sendEmail(
            eq(SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenThrow(new NotificationException(new Exception("Error while trying to sending notification to client")));
        NotificationException notificationException = assertThrows(NotificationException.class, () ->
            notificationService.sendSubmitCaseConfirmationEmail(
                testData.getExpectedDetails(),
                testData.getCaseData(),
                testData.getUserInfo()
            ));
        assertThat(notificationException.getMessage())
            .isEqualTo("java.lang.Exception: Error while trying to sending notification to client");
    }

    @Test
    void shouldSendSubmitCaseConfirmationEmailInWelsh() throws NotificationClientException {
        inputSendEmailResponse = new SendEmailResponse(
            "{\n"
                + "  \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n"
                + "  \"reference\": \"TEST_EMAIL_ALERT\",\n"
                + "  \"template\": {\n"
                + "    \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n"
                + "    \"version\": \"3\",\n"
                + "    \"uri\": \"TEST\"\n"
                + "  },\n"
                + "  \"content\": {\n"
                + "    \"body\": \"Please click here. "
                + "https://www.gov.uk/log-in-register-hmrc-online-services/123456722/?lng=cy.\",\n"
                + "    \"subject\": \"ET Test email created\",\n"
                + "    \"from_email\": \"TEST@GMAIL.COM\"\n"
                + "  }\n"
                + "}\n"
        );
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        when(notificationClient.sendEmail(
            eq("1234_welsh"),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        )).thenReturn(inputSendEmailResponse);
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(EtSyaConstants.WELSH_LANGUAGE);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getExpectedDetails(),
            testData.getCaseData(),
            testData.getUserInfo()
        );
        assertThat(response.getBody()).isEqualTo(
            "Please click here. https://www.gov.uk/log-in-register-hmrc-online-services/123456722/?lng=cy.");
    }

    @Test
    void shouldSendSubmitCaseConfirmationEmailNull() {
        NotificationService notificationService = new NotificationService(notificationClient, notificationsProperties);
        testData.getCaseData().getClaimantHearingPreference().setContactLanguage(null);
        SendEmailResponse response = notificationService.sendSubmitCaseConfirmationEmail(
            testData.getExpectedDetails(),
            testData.getCaseData(),
            testData.getUserInfo()
        );
        assertThat(response.getBody()).isEqualTo("Dear test, Please see your detail as 123456789. "
                                                     + "Regards, ET Team.");
    }

    @Test
    void shouldSendResponseEmailToTribunal() throws NotificationClientException {
        notificationService.sendResponseEmailToTribunal(
            testData.getCaseData(),
            CLAIMANT,
            "1",
            TEST_RESPONDENT,
            NOT_SET,
            testData.getExpectedDetails().getId().toString(),
            "Change my personal details"
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
            "Change my personal details"
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            eq(testData.getCaseData().getTribunalCorrespondenceEmail()),
            any(),
            eq(testData.getExpectedDetails().getId().toString())
        );
    }
}
