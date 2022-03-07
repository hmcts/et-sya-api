package uk.gov.hmcts.reform.et.syaapi.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.FORBIDDEN;

class ErrorResponseTest {
    @Test
    void shouldTestErrorResponseBuilder() {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .message("test")
            .code(FORBIDDEN.value())
            .build();
        assertEquals("test", errorResponse.getMessage());
        assertEquals(403, errorResponse.getCode());
    }
}
