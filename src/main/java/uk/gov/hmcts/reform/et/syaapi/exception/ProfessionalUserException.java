package uk.gov.hmcts.reform.et.syaapi.exception;

/**
 * Exception thrown when a professional user (legal representative) attempts to self-assign a case.
 * Professional users should use MyHMCTS instead of the citizen portal.
 */
public class ProfessionalUserException extends RuntimeException {
    
    /**
     * Constructs a new ProfessionalUserException with the specified detail message.
     *
     * @param message the detail message
     */
    public ProfessionalUserException(String message) {
        super(message);
    }
}
