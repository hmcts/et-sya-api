package uk.gov.hmcts.reform.et.syaapi.config;


import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.UnAuthorisedServiceException;
import uk.gov.hmcts.reform.et.syaapi.models.ErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Test
    void shouldHandleInvalidTokenException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final InvalidTokenException invalidTokenException = new InvalidTokenException("Unauthorized");
        final ErrorResponse errorResponse = ErrorResponse.builder().message("Unauthorized").code(401).build();

        final ResponseEntity<ErrorResponse> actualResponse =
            exceptionHandler.handleInvalidTokenException(invalidTokenException);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(errorResponse).isEqualTo(actualResponse.getBody());
    }

    @Test
    void shouldHandleUnAuthorisedServiceException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final UnAuthorisedServiceException unAuthorisedServiceException = new UnAuthorisedServiceException("Forbidden");
        final ErrorResponse errorResponse = ErrorResponse.builder().message("Forbidden").code(403).build();



        final ResponseEntity<ErrorResponse> actualResponse =
            exceptionHandler.handleUnAuthorisedServiceException(unAuthorisedServiceException);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(errorResponse).isEqualTo(actualResponse.getBody());
    }

    @Test
    void shouldHandleFeignException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final FeignException feignException = new FeignException.InternalServerError(
            "Call failed",
            mock(Request.class),
            "service is down.".getBytes(),
            null
        );
        final ErrorResponse errorResponse = ErrorResponse.builder()
            .message("Call failed - service is down.")
            .code(500)
            .build();

        final ResponseEntity<ErrorResponse> actualResponse =
            exceptionHandler.handleFeignException(feignException);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(errorResponse).isEqualTo(actualResponse.getBody());
    }
}
