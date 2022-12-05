package uk.gov.hmcts.reform.et.syaapi.config.interceptors;

import uk.gov.hmcts.reform.et.syaapi.service.CaseService;

/**
 * Called by {@link CaseService} when exception is encountered whilst downloading document resource.
 */
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 4L;

    /**
     * Throws a {@link ResourceNotFoundException} with a message and a cause.
     * @param message
     * @param ex
     */
    public ResourceNotFoundException(String message, Throwable ex) {
        super(message, ex);
    }
}
