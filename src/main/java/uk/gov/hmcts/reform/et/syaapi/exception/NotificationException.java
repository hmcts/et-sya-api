package uk.gov.hmcts.reform.et.syaapi.exception;

public class NotificationException extends RuntimeException {

    private static final long serialVersionUID = -3042681110164047L;

    /**
     *   Creates an {@link NotificationException} with a cause.
     *   @param cause while trying to send email.
     */
    public NotificationException(Exception cause) {
        super(cause);
    }
}
