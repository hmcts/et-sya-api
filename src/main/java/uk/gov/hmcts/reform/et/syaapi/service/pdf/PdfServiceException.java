package uk.gov.hmcts.reform.et.syaapi.service.pdf;

/**
 * Is thrown when an exception occurs whiles converting case data in pdf in {@link PdfService}.
 * for example an IOException is encountered while loading pdf template
 */
public class PdfServiceException extends Exception {
    private static final long serialVersionUID = 304268196018404976L;

    /**
     * Creates a {@link PdfServiceException} with an error message and the cause of the error.
     *
     * @param message the message explaining why the error occurred
     * @param cause the cause of the error
     */
    public PdfServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
