package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.hmcts.reform.et.syaapi.service.NotificationService.CoreEmailDetails;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.NOT_SET;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.YES;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class NotificationPseServiceTest {

    @Mock
    private NotificationClient notificationClient;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CoreEmailDetails details;
    private NotificationPseService notificationPseService;

    private CaseTestData caseTestData;

    private static final String CLAIMANT_NAME = "Jane Doe";
    private static final String RESPONDENT_IDAM_ID = "respondent-idam-id";
    private static final String REP_EMAIL = "rep@email.com";

    @BeforeEach
    void before() {
        NotificationService notificationService = new NotificationService(
            notificationClient,
            notificationsProperties,
            featureToggleService
        );
        notificationPseService = new NotificationPseService(
            notificationClient,
            notificationsProperties,
            notificationService
        );

        given(notificationsProperties.getPseRespondentResponseTemplateId())
            .willReturn("respondentResponseTemplateId");
        given(notificationsProperties.getPseClaimantResponseYesTemplateId())
            .willReturn("claimantResponseYesTemplateId");
        given(notificationsProperties.getPseClaimantResponseNoTemplateId())
            .willReturn("claimantResponseNoTemplateId");

        caseTestData = new CaseTestData();
        caseTestData.getCaseData().getRespondentCollection().getFirst().getValue().setIdamId(RESPONDENT_IDAM_ID);
        caseTestData.getCaseData().setRepCollection(List.of(
            RepresentedTypeRItem.builder()
                .value(RepresentedTypeR.builder()
                           .myHmctsYesNo(YES)
                           .respRepName("RespRepName")
                           .representativeEmailAddress(REP_EMAIL)
                           .build())
                .build()
        ));
    }

    @Test
    void sendResponseNotificationEmailToTribunal_Normal() throws NotificationClientException {
        caseTestData.getCaseData().setTribunalCorrespondenceEmail("tribunal@test.com");
        notificationPseService.sendResponseNotificationEmailToTribunal(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString()
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(caseTestData.getCaseData().getTribunalCorrespondenceEmail()),
            any(),
            eq(caseTestData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void sendNotResponseNotificationEmailToTribunalMissingEmail() throws NotificationClientException {
        caseTestData.getCaseData().setTribunalCorrespondenceEmail(null);
        notificationPseService.sendResponseNotificationEmailToTribunal(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString()
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void sendResponseNotificationEmailToRespondent_Copy() throws NotificationClientException {
        notificationPseService.sendResponseNotificationEmailToRespondent(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            YES,
            true,
            null
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(caseTestData.getCaseData().getRespondentCollection().getFirst().getValue().getRespondentEmail()),
            any(),
            eq(caseTestData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void sendNotResponseNotificationEmailToRespondent_DoNotCopy() throws NotificationClientException {
        notificationPseService.sendResponseNotificationEmailToRespondent(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            NO,
            true,
            null
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void sendNotResponseNotificationEmailToRespondent_MissingEmail() throws NotificationClientException {
        for (RespondentSumTypeItem respondentSumTypeItem : caseTestData.getCaseData().getRespondentCollection()) {
            respondentSumTypeItem.getValue().setRespondentEmail(null);
        }

        notificationPseService.sendResponseNotificationEmailToRespondent(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            YES,
            true,
            null
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void sendResponseNotificationEmailToRespondent_FromRespondent_CopyNo_SendsToCurrent()
        throws NotificationClientException {
        notificationPseService.sendResponseNotificationEmailToRespondent(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            NO,
            false,
            RESPONDENT_IDAM_ID
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(caseTestData.getCaseData().getRespondentCollection().getFirst().getValue().getRespondentEmail()),
            any(),
            eq(caseTestData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void sendResponseNotificationEmailToRespondent_FromRespondent_CopyNo_NoEmail_NoSend()
        throws NotificationClientException {
        caseTestData.getCaseData().getRespondentCollection().getFirst().getValue().setRespondentEmail(null);

        notificationPseService.sendResponseNotificationEmailToRespondent(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            NO,
            false,
            RESPONDENT_IDAM_ID
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void sendResponseNotificationEmailToRespondent_FromRespondent_CopyYes_AllUnrepresented_AllWithEmail()
        throws NotificationClientException {
        notificationPseService.sendResponseNotificationEmailToRespondent(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            YES,
            false,
            RESPONDENT_IDAM_ID
        );

        verify(notificationClient, times(5)).sendEmail(
            any(),
            any(),
            any(),
            any());
    }

    @Test
    void sendResponseNotificationEmailToRespondent_FromRespondent_CopyYes_RepresentedWithEmail()
        throws NotificationClientException {
        caseTestData.getCaseData().getRespondentCollection().get(1).getValue().setRespondentName("RespRepName");

        notificationPseService.sendResponseNotificationEmailToRespondent(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            YES,
            false,
            RESPONDENT_IDAM_ID
        );

        verify(notificationClient, times(1)).sendEmail(
            any(),
            eq(REP_EMAIL),
            any(),
            any()
        );
    }

    @Test
    void sendResponseNotificationEmailToRespondent_RespondentPseResponse_CopyYes_RepresentedNoEmail_NoSend()
        throws NotificationClientException {
        caseTestData.getCaseData().getRespondentCollection().get(1).getValue().setRespondentName("RespRepName");
        caseTestData.getCaseData().getRepCollection().getFirst().getValue().setRepresentativeEmailAddress("");

        notificationPseService.sendResponseNotificationEmailToRespondent(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            YES,
            false,
            RESPONDENT_IDAM_ID
        );

        verify(notificationClient, never()).sendEmail(
            any(),
            eq(""),
            any(),
            any()
        );
    }

    @Test
    void sendResponseNotificationEmailToClaimant_Copy() throws NotificationClientException {
        notificationPseService.sendResponseNotificationEmailToClaimant(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            YES,
            true
        );

        verify(notificationClient, times(1)).sendEmail(
            eq("claimantResponseYesTemplateId"),
            eq(caseTestData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(caseTestData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void sendResponseNotificationEmailToClaimant_DoNotCopy() throws NotificationClientException {
        notificationPseService.sendResponseNotificationEmailToClaimant(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            NO,
            true
        );

        verify(notificationClient, times(1)).sendEmail(
            eq("claimantResponseNoTemplateId"),
            eq(caseTestData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            any(),
            eq(caseTestData.getExpectedDetails().getId().toString())
        );
    }

    @Test
    void sendNotResponseNotificationEmailToClaimant_MissingEmail() throws NotificationClientException {
        caseTestData.getCaseData().getClaimantType().setClaimantEmailAddress(null);
        notificationPseService.sendResponseNotificationEmailToClaimant(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            YES,
            true
        );

        verify(notificationClient, times(0)).sendEmail(
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void sendResponseNotificationEmailToClaimant_FromRespondent_CopyNo_NoEmailSent()
        throws NotificationClientException {
        caseTestData.getCaseData().setEt1OnlineSubmission("Yes");

        notificationPseService.sendResponseNotificationEmailToClaimant(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            NO,
            false
        );

        verify(notificationClient, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    void sendResponseNotificationEmailToClaimant_FromRespondent_CopyNull_NoEmailSent()
        throws NotificationClientException {
        caseTestData.getCaseData().setEt1OnlineSubmission("Yes");

        notificationPseService.sendResponseNotificationEmailToClaimant(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            null,
            false
        );

        verify(notificationClient, never()).sendEmail(any(), any(), any(), any());
    }

    @Test
    void sendResponseNotificationEmailToClaimant_FromRespondent_ClaimantOffline_NoEmailSent()
        throws NotificationClientException {
        caseTestData.getCaseData().setEt1OnlineSubmission(null);
        caseTestData.getCaseData().setHubLinksStatuses(null);

        notificationPseService.sendResponseNotificationEmailToClaimant(
            caseTestData.getCaseData(),
            caseTestData.getExpectedDetails().getId().toString(),
            YES,
            false
        );

        verify(notificationClient, never()).sendEmail(any(), any(), any(), any());
    }

    @Nested
    class SendNotificationStoredEmailToClaimant {
        @BeforeEach
        void setUp() {
            details = new CoreEmailDetails(
                caseTestData.getCaseData(),
                CLAIMANT_NAME,
                "1",
                "Test Respondent Organisation -1-,"
                    + " Mehmet Tahir Dede, Abuzer Kadayif, Kate Winslet, Jeniffer Lopez",
                NOT_SET,
                caseTestData.getExpectedDetails().getId().toString()
            );
        }

        @Test
        void shouldSendEmailToClaimant_whenEmailPresent() throws NotificationClientException {
            notificationPseService.sendNotificationStoredEmailToClaimant(details, "shortText");
            verify(notificationClient, times(1)).sendEmail(any(), any(), any(), any());
        }


        @Test
        void shouldSendEmailToClaimant_whenEmailNotPresent() throws NotificationClientException {
            details.caseData().getClaimantType().setClaimantEmailAddress("");
            notificationPseService.sendNotificationStoredEmailToClaimant(details, "shortText");
            verify(notificationClient, times(0)).sendEmail(any(), any(), any(), any());
        }
    }

    @Nested
    class SendNotificationStoredEmailToRespondent {
        @BeforeEach
        void setUp() {
            details = new CoreEmailDetails(
                caseTestData.getCaseData(),
                CLAIMANT_NAME,
                "1",
                "Test Respondent Organisation -1-,"
                    + " Mehmet Tahir Dede, Abuzer Kadayif, Kate Winslet, Jeniffer Lopez",
                NOT_SET,
                caseTestData.getExpectedDetails().getId().toString()
            );
        }

        @Test
        void shouldSendEmailToRespondent_whenEmailPresent() throws NotificationClientException {
            notificationPseService.sendNotificationStoredEmailToRespondent(details, "shortText", RESPONDENT_IDAM_ID);
            verify(notificationClient, times(1)).sendEmail(any(), any(), any(), any());
        }

        @Test
        void shouldSendEmailToRespondent_whenResponseEmailPresent() throws NotificationClientException {
            details.caseData().getRespondentCollection().get(5).getValue().setResponseRespondentEmail("test@test.com");
            notificationPseService.sendNotificationStoredEmailToRespondent(details, "shortText",
                                                                           "notifications-test-idam-id");
            verify(notificationClient, times(1)).sendEmail(any(), any(), any(), any());
        }

        @Test
        void shouldSendEmailToRespondent_whenEmailNotPresent() throws NotificationClientException {
            details.caseData().getRespondentCollection().get(5).getValue().setResponseRespondentEmail("");
            notificationPseService.sendNotificationStoredEmailToRespondent(details, "shortText",
                                                                           "notifications-test-idam-id");
            verify(notificationClient, times(0)).sendEmail(any(), any(), any(), any());
        }

        @Test
        void shouldSendEmailToRespondent_whenRespondentNotPresent() throws NotificationClientException {
            notificationPseService.sendNotificationStoredEmailToRespondent(details, "shortText", "dummy");
            verify(notificationClient, times(0)).sendEmail(any(), any(), any(), any());
        }
    }
}
