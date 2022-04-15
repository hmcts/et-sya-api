package uk.gov.hmcts.reform.et.syaapi.exception;

public class NotificationException extends RuntimeException {

    public NotificationException(Exception cause) {
        super(cause);
    }

}
