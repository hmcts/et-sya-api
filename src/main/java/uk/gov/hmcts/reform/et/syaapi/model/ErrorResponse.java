package uk.gov.hmcts.reform.et.syaapi.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.et.syaapi.config.GlobalExceptionHandler;

/**
 * Defines the error message returned from a http call to the API with code referring to the http status.
 * Called when an exception is handled by {@link GlobalExceptionHandler}
 */
@Data
@Builder
public class ErrorResponse {
    private String message;
    private Integer code;
}
