package uk.gov.hmcts.reform.et.syaapi.config;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.UnAuthorisedServiceException;
import uk.gov.hmcts.reform.et.syaapi.models.ErrorResponse;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidTokenException(InvalidTokenException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse.builder()
                .message(exception.getMessage())
                .code(HttpStatus.UNAUTHORIZED.value())
                .build()
        );
    }

    @ExceptionHandler(UnAuthorisedServiceException.class)
    public ResponseEntity<Object> handleUnAuthorisedServiceException(
        UnAuthorisedServiceException unAuthorisedServiceException
    ) {
        log.error(unAuthorisedServiceException.getMessage(), unAuthorisedServiceException);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse.builder()
                .message(unAuthorisedServiceException.getMessage())
                .code(HttpStatus.FORBIDDEN.value())
                .build()
        );
    }

    @ExceptionHandler(FeignException.class)
    ResponseEntity<Object> handleFeignException(FeignException exception) {
        log.error(exception.getMessage(), exception);

        return ResponseEntity.status(exception.status()).body(
            ErrorResponse.builder()
                .message(String.format("%s - %s", exception.getMessage(), exception.contentUTF8()))
                .code(exception.status())
                .build()
        );
    }
}
