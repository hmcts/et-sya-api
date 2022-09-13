package uk.gov.hmcts.reform.et.syaapi.config.interceptors;

public class UnAuthorisedServiceException extends RuntimeException {
    private static final long serialVersionUID = -8778329799233256308L;

    public UnAuthorisedServiceException(String message) {
        super(message);
    }
}
