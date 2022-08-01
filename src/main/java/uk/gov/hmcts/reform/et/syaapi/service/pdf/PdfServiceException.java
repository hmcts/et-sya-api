package uk.gov.hmcts.reform.et.syaapi.service.pdf;

public class PdfServiceException extends Exception {
    private static final long serialVersionUID = 304268196018404976L;

    /**
     *
     * @param message
     * @param cause
     */
    public PdfServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
