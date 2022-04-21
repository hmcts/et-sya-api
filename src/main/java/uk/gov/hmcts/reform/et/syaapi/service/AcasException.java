package uk.gov.hmcts.reform.et.syaapi.service;

/**
 * Is thrown when there are problems accessing ACAS to obtain certificate data.
 */
public class AcasException extends Exception {

    private static final long serialVersionUID = -3042681111658047285L;

    /**
     * Creates an {@link AcasException} with a message and the cause.
     *
     * @param message the message explaining why this exception is thrown
     * @param cause   the cause (which is saved for later retrieval by the getCause() method).
     *                (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public AcasException(String message, Throwable cause) {
        super(message, cause);
    }
}
