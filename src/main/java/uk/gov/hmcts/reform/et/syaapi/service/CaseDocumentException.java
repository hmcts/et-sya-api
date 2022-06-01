package uk.gov.hmcts.reform.et.syaapi.service;

public class CaseDocumentException extends Exception {
    public CaseDocumentException(String message, Throwable cause) {
        super(message, cause);
    }

    public CaseDocumentException(String message) {
        super(message);
    }
}
