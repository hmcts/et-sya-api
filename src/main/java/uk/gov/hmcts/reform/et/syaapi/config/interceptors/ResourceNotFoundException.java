package uk.gov.hmcts.reform.et.syaapi.config.interceptors;

public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 4L;

    public ResourceNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }
}
