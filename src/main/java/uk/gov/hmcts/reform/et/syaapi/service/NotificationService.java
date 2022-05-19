package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationClient notificationClient;

    /**
     * Given a caseId, initialization of trigger event to start and submit update for case.
     *
     * @param templateId - pass gov notify template id for each use case
     * @param targetEmail - recepient target email id
     * @param parameters - map of strings to add this to the template
     * @param reference - reference string for email template
     * @return response from notification api
     */

    public SendEmailResponse sendEMail(
        String templateId,
        String targetEmail,
        Map<String, String> parameters,
        String reference
    ) {
        SendEmailResponse sendEmailResponse;
        try {
            sendEmailResponse = notificationClient.sendEmail(templateId, targetEmail, parameters, reference);
        } catch (NotificationClientException ne) {
            log.error("Error while trying to sending notification to client", ne);
            throw new NotificationException(ne);
        }
        return sendEmailResponse;
    }
}
