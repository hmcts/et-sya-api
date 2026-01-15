package uk.gov.hmcts.reform.et.syaapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.et.syaapi.exception.ProfessionalUserException;
import uk.gov.hmcts.reform.et.syaapi.models.CaseAssignmentResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerProfessionalUserTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setup() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleProfessionalUserExceptionAndReturn200OK() {
        ProfessionalUserException exception = new ProfessionalUserException(
            "User is a professional user - user will be redirected to use MyHMCTS."
        );

        ResponseEntity<CaseAssignmentResponse> response = 
            globalExceptionHandler.handleProfessionalUserException(exception);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus())
            .isEqualTo(CaseAssignmentResponse.AssignmentStatus.PROFESSIONAL_USER);
    }

    @Test
    void shouldReturnCorrectMessageForProfessionalUser() {
        ProfessionalUserException exception = new ProfessionalUserException(
            "User is a professional user - user will be redirected to use MyHMCTS."
        );

        ResponseEntity<CaseAssignmentResponse> response = 
            globalExceptionHandler.handleProfessionalUserException(exception);

        CaseAssignmentResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).contains("professional user");
        assertThat(body.getMessage()).contains("MyHMCTS");
    }

    @Test
    void shouldReturnResponseWithProfessionalUserStatus() {
        ProfessionalUserException exception = new ProfessionalUserException("Test message");

        ResponseEntity<CaseAssignmentResponse> response = 
            globalExceptionHandler.handleProfessionalUserException(exception);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
            CaseAssignmentResponse.AssignmentStatus.PROFESSIONAL_USER,
            response.getBody().getStatus()
        );
    }

    @Test
    void shouldNotReturnErrorStatusForProfessionalUser() {
        ProfessionalUserException exception = new ProfessionalUserException(
            "User is a professional user - user will be redirected to use MyHMCTS."
        );

        ResponseEntity<CaseAssignmentResponse> response = 
            globalExceptionHandler.handleProfessionalUserException(exception);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getStatusCode().is4xxClientError()).isFalse();
        assertThat(response.getStatusCode().is5xxServerError()).isFalse();
    }
}
