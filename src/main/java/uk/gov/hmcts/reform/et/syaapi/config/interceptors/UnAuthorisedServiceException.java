package uk.gov.hmcts.reform.et.syaapi.config.interceptors;

/**
 * called by {@link RequestInterceptor} when token authentication throws an error.
 */
public class UnAuthorisedServiceException extends RuntimeException {
    private static final long serialVersionUID = -8778329799233256308L;

    /**
     * Create an {@link UnAuthorisedServiceException} with a cause and a message.
     * @param message exception message
     */
    public UnAuthorisedServiceException(String message) {
        super(message);
    }
}
