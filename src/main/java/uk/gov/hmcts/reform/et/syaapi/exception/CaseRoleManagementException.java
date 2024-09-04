package uk.gov.hmcts.reform.et.syaapi.exception;

import uk.gov.hmcts.reform.et.syaapi.service.ManageCaseRoleService;

import java.io.Serial;

/**
 *   Triggered by {@link ManageCaseRoleService} when an exception is encountered.
 */
public class CaseRoleManagementException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    /**
     *   Creates an {@link CaseRoleManagementException} with a cause.
     *   @param cause while trying to modify case role management
     */
    public CaseRoleManagementException(Exception cause) {
        super(cause);
    }

}
