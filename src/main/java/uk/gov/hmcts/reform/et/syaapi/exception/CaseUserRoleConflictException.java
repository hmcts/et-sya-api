package uk.gov.hmcts.reform.et.syaapi.exception;

import java.io.Serial;

/**
 * Triggered when a case user role modification conflicts with existing state (e.g. user already assigned).
 */
public class CaseUserRoleConflictException extends ManageCaseRoleException {

    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    /**
     * Creates a {@link CaseUserRoleConflictException} with a message.
     * @param message the exception message
     */
    public CaseUserRoleConflictException(String message) {
        super(message);
    }
}
