package uk.gov.hmcts.reform.et.syaapi.service;

/**
 * This is thrown when attempting to generate a document via the DocumentGenerationService fails
 */
public class DocumentGenerationException extends Exception {

    private static final long serialVersionUID = 3042681960184047285L;

    /**
     * Creates a {@link DocumentGenerationException} with no message
     */
    public DocumentGenerationException() {
        super();
    }

    /**
     * Creates a {@link DocumentGenerationException} with a message.
     *
     * @param message the message explaining why the error occurred
     */
    public DocumentGenerationException(String message) {
        super(message);
    }

    /**
     * Creates a {@link DocumentGenerationException} with an error message and the cause of the error.
     *
     * @param message the message explaining why the error occurred
     * @param cause the cause of the error
     */
    public DocumentGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
