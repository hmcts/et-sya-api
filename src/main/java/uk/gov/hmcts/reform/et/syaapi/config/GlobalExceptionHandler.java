package uk.gov.hmcts.reform.et.syaapi.config;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.UnAuthorisedServiceException;
import uk.gov.hmcts.reform.et.syaapi.models.ErrorResponse;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(UNAUTHORIZED).body(
            ErrorResponse.builder()
                .message(exception.getMessage())
                .code(UNAUTHORIZED.value())
                .build()
        );
    }

    @ExceptionHandler(UnAuthorisedServiceException.class)
    public ResponseEntity<ErrorResponse> handleUnAuthorisedServiceException(
        UnAuthorisedServiceException unAuthorisedServiceException
    ) {
        log.error(unAuthorisedServiceException.getMessage(), unAuthorisedServiceException);
        return ResponseEntity.status(FORBIDDEN).body(
            ErrorResponse.builder()
                .message(unAuthorisedServiceException.getMessage())
                .code(FORBIDDEN.value())
                .build()
        );
    }

    @ExceptionHandler(FeignException.class)
    ResponseEntity<ErrorResponse> handleFeignException(FeignException exception) {
        log.error(exception.getMessage(), exception);

        return ResponseEntity.status(exception.status()).body(
            ErrorResponse.builder()
                .message(String.format("%s - %s", exception.getMessage(), exception.contentUTF8()))
                .code(exception.status())
                .build()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(exception.getStatus()).body(
            ErrorResponse.builder()
                .message(exception.getLocalizedMessage())
                .code(exception.getStatus().value())
                .build()
        );
    }
}
