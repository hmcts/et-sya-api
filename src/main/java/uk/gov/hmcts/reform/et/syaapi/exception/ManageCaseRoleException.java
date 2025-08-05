package uk.gov.hmcts.reform.et.syaapi.exception;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.et.syaapi.service.ManageCaseRoleService;

import java.io.Serial;
import java.util.Arrays;

/**
 *   Triggered by {@link ManageCaseRoleService} when an exception is encountered.
 */
@Slf4j
public class ManageCaseRoleException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    /**
     *   Creates an {@link ManageCaseRoleException} with a cause.
     *   @param cause while trying to modify case role management
     */
    public ManageCaseRoleException(Exception cause) {
        super(cause);
        String errorMessage = "Error occurred while modifying case role: " + cause.getMessage();
        log.error("************ ManageCaseRoleException ************");
        log.error(errorMessage);
        log.error(Arrays.toString(cause.getStackTrace()));
        log.error("***************************************************");
    }

}
