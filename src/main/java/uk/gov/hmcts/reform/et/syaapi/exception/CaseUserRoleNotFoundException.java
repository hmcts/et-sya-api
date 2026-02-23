package uk.gov.hmcts.reform.et.syaapi.exception;

import java.io.Serial;

/**
 * Triggered when a required resource (e.g. Case, Respondent, Representative)
 * is not found during case role modification.
 */
public class CaseUserRoleNotFoundException extends ManageCaseRoleException {

    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    /**
     * Creates a {@link CaseUserRoleNotFoundException} with a message.
     * @param message the exception message
     */
    public CaseUserRoleNotFoundException(String message) {
        super(message);
    }
}
