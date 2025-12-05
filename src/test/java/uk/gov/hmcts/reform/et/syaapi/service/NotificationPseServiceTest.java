package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.NOT_SET;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.YES;

@ExtendWith(MockitoExtension.class)
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

    private CaseData caseData;
    private CaseTestData caseTestData;

    public static final String CLAIMANT = "Jane Doe";

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

        caseData = new CaseData();
        caseTestData = new CaseTestData();
        caseTestData.getCaseData().setRepCollection(List.of(
            RepresentedTypeRItem.builder()
                .value(RepresentedTypeR.builder()
                           .myHmctsYesNo(YES)
                           .respRepName("RespRepName")
                           .build())
                .build()
        ));
    }

    @Test
    void sendResponseNotificationEmailToTribunal() throws NotificationClientException {
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
    void sendResponseNotificationEmailToRespondent() throws NotificationClientException {
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
    void sendNotResponseNotificationEmailToRespondentDoNotCopy() throws NotificationClientException {
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
    void sendNotResponseNotificationEmailToRespondentMissingEmail() throws NotificationClientException {
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
    void sendResponseNotificationEmailToClaimant() throws NotificationClientException {
        given(notificationsProperties.getPseClaimantResponseYesTemplateId())
            .willReturn("claimantResponseYesTemplateId");

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
    void sendResponseNotificationEmailToClaimantDoNotCopy() throws NotificationClientException {
        given(notificationsProperties.getPseClaimantResponseNoTemplateId())
            .willReturn("claimantResponseNoTemplateId");

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
    void sendNotResponseNotificationEmailToClaimantMissingEmail() throws NotificationClientException {
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

    @Nested
    class SendNotificationStoredEmailToClaimant {
        @BeforeEach
        void setUp() {
            details = new CoreEmailDetails(
                caseTestData.getCaseData(),
                CLAIMANT,
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
                CLAIMANT,
                "1",
                "Test Respondent Organisation -1-,"
                    + " Mehmet Tahir Dede, Abuzer Kadayif, Kate Winslet, Jeniffer Lopez",
                NOT_SET,
                caseTestData.getExpectedDetails().getId().toString()
            );
        }

        @Test
        void shouldSendEmailToRespondent_whenEmailPresent() throws NotificationClientException {
            notificationPseService.sendNotificationStoredEmailToRespondent(details, "shortText", "1234567890");
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
