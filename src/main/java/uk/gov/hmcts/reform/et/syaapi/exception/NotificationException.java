package uk.gov.hmcts.reform.et.syaapi.exception;

public class NotificationException extends RuntimeException {

    private static final long serialVersionUID = -3042681110164047L;

    public NotificationException(Exception cause) {
        super(cause);
    }

}
