package uk.gov.hmcts.reform.et.syaapi.exception;

import uk.gov.hmcts.reform.et.syaapi.service.NotificationService;

/**
 *   Triggered by {@link NotificationService} when an exception is encountered.
 */
public class NotificationException extends RuntimeException {

    private static final long serialVersionUID = Long.MIN_VALUE;
    /**
     *   Creates an {@link NotificationException} with a cause.
     *   @param cause while trying to send email.
     */
    public NotificationException(Exception cause) {
        super(cause);
    }
}
