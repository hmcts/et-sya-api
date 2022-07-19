package uk.gov.hmcts.reform.et.syaapi.service;

/**
 * This is thrown when trying to map bad data within the {@Link PdfMapperService} e.g. null case data.
 */
public class PdfMapperException extends Exception {
    private static final long serialVersionUID = 3042681960184047298L;

    /**
     * Creates a {@link PdfMapperException} with a message.
     *
     * @param message the message explaining why the error occurred
     */
    public PdfMapperException(String message) { super(message);}
}
