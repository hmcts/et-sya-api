package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.et.syaapi.exception.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationClient notificationClient;

    public SendEmailResponse sendMail(
        String templateId,
        String targetEmail,
        Map<String, String> parameters,
        String reference
    ) {
        SendEmailResponse sendEmailResponse = null;
        try {
            sendEmailResponse = notificationClient.sendEmail(templateId, targetEmail, parameters, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
        return sendEmailResponse;
    }
}

