package uk.gov.hmcts.reform.et.syaapi.config;


import feign.FeignException;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.UnAuthorisedServiceException;
import uk.gov.hmcts.reform.et.syaapi.models.ErrorResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    @Test
    public void shouldHandleInvalidTokenException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final InvalidTokenException invalidTokenException = mock(InvalidTokenException.class);
        final ErrorResponse errorResponse = ErrorResponse.builder().message("Unauthorized").code(401).build();

        when(invalidTokenException.getMessage()).thenReturn("Unauthorized");

        final ResponseEntity<Object> actualResponse =
            exceptionHandler.handleInvalidTokenException(invalidTokenException);

        assertEquals(HttpStatus.UNAUTHORIZED, actualResponse.getStatusCode());
        assertEquals(errorResponse, actualResponse.getBody());
    }

    @Test
    public void shouldHandleUnAuthorisedServiceException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final UnAuthorisedServiceException unAuthorisedServiceException = mock(UnAuthorisedServiceException.class);
        final ErrorResponse errorResponse = ErrorResponse.builder().message("Forbidden").code(403).build();

        when(unAuthorisedServiceException.getMessage()).thenReturn("Forbidden");

        final ResponseEntity<Object> actualResponse =
            exceptionHandler.handleUnAuthorisedServiceException(unAuthorisedServiceException);

        assertEquals(HttpStatus.FORBIDDEN, actualResponse.getStatusCode());
        assertEquals(errorResponse, actualResponse.getBody());
    }

    @Test
    public void shouldHandleFeignException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final FeignException feignException = mock(FeignException.class);
        final ErrorResponse errorResponse = ErrorResponse.builder().message("Call failed - service is down.").code(500).build();

        when(feignException.status()).thenReturn(500);
        when(feignException.getMessage()).thenReturn("Call failed");
        when(feignException.contentUTF8()).thenReturn("service is down.");

        final ResponseEntity<Object> actualResponse =
            exceptionHandler.handleFeignException(feignException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(errorResponse, actualResponse.getBody());
    }
}
