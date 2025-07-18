package uk.gov.hmcts.reform.et.syaapi.config.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.UNAUTHORIZED_APIS;

/**
 * Intercepts any call to the et-sya-api and validates the token.
 */
@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    private final VerifyTokenService verifyTokenService;

    @Autowired
    public RequestInterceptor(VerifyTokenService verifyTokenService) {
        this.verifyTokenService = verifyTokenService;
    }

    /**
     * Intercepts any incoming calls and throws exception if token is invalid.
     * @param requestServlet current HTTP request
     * @param responseServlet current HTTP response
     * @param handler chosen handler to execute, for type and/or instance evaluation
     * @return true if the token is verified
     */
    public boolean preHandle(HttpServletRequest requestServlet, HttpServletResponse responseServlet, 
                             Object handler) throws Exception {
        if (UNAUTHORIZED_APIS.contains(requestServlet.getRequestURI())) {
            return true;
        }
        String authorizationHeader = requestServlet.getHeader(AUTHORIZATION);
        boolean jwtVerified = verifyTokenService.verifyTokenSignature(authorizationHeader);
        if (!jwtVerified) {
            throw new UnAuthorisedServiceException("Failed to verify bearer token.");
        }
        return true;
    }
}
