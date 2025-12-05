package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.notification.NotificationsProperties;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.YES;

@ExtendWith(MockitoExtension.class)
class NotificationPseServiceTest {

    @Mock
    private NotificationClient notificationClient;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private FeatureToggleService featureToggleService;
    private NotificationPseService notificationPseService;

    private CaseTestData caseTestData;

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
}
