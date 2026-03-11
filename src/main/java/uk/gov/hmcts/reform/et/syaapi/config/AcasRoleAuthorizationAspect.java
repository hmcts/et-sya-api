package uk.gov.hmcts.reform.et.syaapi.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.et.syaapi.service.RoleValidationService;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Aspect that intercepts methods annotated with {@link uk.gov.hmcts.reform.et.syaapi.annotation.RequiresAcasRole}
 * and validates that the user has the required ACAS roles.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AcasRoleAuthorizationAspect {

    private static final String CASEWORKER_EMPLOYMENT_API_ROLE = "caseworker-employment-api";
    private static final String ET_ACAS_API_ROLE = "et-acas-api";
    private static final List<String> ALLOWED_ROLES = Arrays.asList(
        CASEWORKER_EMPLOYMENT_API_ROLE,
        ET_ACAS_API_ROLE
    );
    private static final String FORBIDDEN_MESSAGE = 
        "User does not have required role to access this endpoint";

    private final RoleValidationService roleValidationService;

    /**
     * Intercepts methods annotated with @RequiresAcasRole and validates user roles.
     *
     * @param joinPoint the join point representing the method execution
     * @return the result of the method execution if authorized, or a 403 Forbidden response
     * @throws Throwable if an error occurs during method execution
     */
    @Around("@annotation(uk.gov.hmcts.reform.et.syaapi.annotation.RequiresAcasRole)")
    public Object checkAcasRole(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentHttpRequest();
        
        if (request == null) {
            log.error("Unable to retrieve HTTP request from context");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error");
        }

        String authToken = request.getHeader(AUTHORIZATION);
        
        if (authToken == null || authToken.isEmpty()) {
            log.warn("No authorization token found in request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("No authorization token provided");
        }

        boolean hasRequiredRole = roleValidationService.hasAnyRole(authToken, ALLOWED_ROLES);
        
        if (!hasRequiredRole) {
            log.warn("User does not have required ACAS role for endpoint: {}", 
                request.getRequestURI());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(FORBIDDEN_MESSAGE);
        }

        // User has required role, proceed with method execution
        return joinPoint.proceed();
    }

    /**
     * Retrieves the current HTTP request from the RequestContextHolder.
     *
     * @return the current HttpServletRequest, or null if not available
     */
    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
