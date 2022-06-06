package uk.gov.hmcts.reform.et.syaapi.service;

/**
* Is thrown when there is an error uploading a file to the document service.
*/
public class CaseDocumentException extends Exception {

    /**
     * Creates an {@link CaseDocumentException} with a message and the cause.
     *
     * @param message the message explaining why this exception is thrown
     * @param cause   the cause (which is saved for later retrieval by the getCause() method).
     *                (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public CaseDocumentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an {@link CaseDocumentException} with a message.
     *
     * @param message the message explaining why this exception is thrown
     */
    public CaseDocumentException(String message) {
        super(message);
    }
}
