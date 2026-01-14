package uk.gov.hmcts.reform.et.syaapi.config;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.ResourceNotFoundException;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.UnAuthorisedServiceException;
import uk.gov.hmcts.reform.et.syaapi.exception.ProfessionalUserException;
import uk.gov.hmcts.reform.et.syaapi.models.CaseAssignmentResponse;
import uk.gov.hmcts.reform.et.syaapi.models.ErrorResponse;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * Creates a handler that will handle specific exceptions that occur anywhere in the API.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Intercepts any {@link InvalidTokenException} occurances within the api and builds an appropriate response.
     * @param exception that just occured
     * @return {@link ErrorResponse} with the Unauthorised (401) response
     */
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

    /**
     * Intercepts any {@link UnAuthorisedServiceException} occurances within the api and builds an appropriate response.
     * @param unAuthorisedServiceException that just occured
     * @return {@link ErrorResponse} with the Forbidden response
     */
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

    /**
     * Intercepts any {@link FeignException} occurances within the api and builds an appropriate response.
     * @param exception that just occured
     * @return {@link ErrorResponse} with the exception details
     */
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

    /**
     * Intercepts any {@link ResponseStatusException} occurances within the api and builds an appropriate response.
     * @param exception that just occured
     * @return {@link ErrorResponse} with the exception details
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(exception.getStatusCode()).body(
            ErrorResponse.builder()
                .message(exception.getLocalizedMessage())
                .code(exception.getStatusCode().value())
                .build()
        );
    }

    /**
     * Intercepts any {@link ResourceNotFoundException} occurances within the api and builds an appropriate response.
     * @param exception that just occured
     * @return {@link ErrorResponse} with the Not Found (404) response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleResourceNotFoundException(final ResourceNotFoundException exception) {
        return ResponseEntity.status(NOT_FOUND).body(
            ErrorResponse.builder()
                .message(exception.getMessage())
                .code(NOT_FOUND.value())
                .build()
        );
    }

    /**
     * Intercepts any {@link ProfessionalUserException} occurances within the api and returns a proper response.
     * This is not an error - it's expected behavior when a professional user attempts to self-assign.
     * @param exception that just occured
     * @return {@link CaseAssignmentResponse} with PROFESSIONAL_USER status and 200 OK
     */
    @ExceptionHandler(ProfessionalUserException.class)
    public ResponseEntity<CaseAssignmentResponse> handleProfessionalUserException(ProfessionalUserException exception) {
        log.info("Professional user detected - returning PROFESSIONAL_USER status.");
        return ResponseEntity.ok(CaseAssignmentResponse.builder()
            .status(CaseAssignmentResponse.AssignmentStatus.PROFESSIONAL_USER)
            .message(exception.getMessage())
            .build());
    }
}
