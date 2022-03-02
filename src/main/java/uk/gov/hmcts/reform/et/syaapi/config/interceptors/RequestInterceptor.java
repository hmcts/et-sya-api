package uk.gov.hmcts.reform.et.syaapi.config.interceptors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired
    VerifyTokenService verifyTokenService;

    private static final String FAILED_TO_VERIFY_TOKEN = "Failed to verify the following token: {}";

    @Override
    public boolean preHandle(HttpServletRequest requestServlet, HttpServletResponse responseServlet, Object handler)
        throws IOException {
        String authorizationHeader = requestServlet.getHeader(AUTHORIZATION);
        boolean jwtVerified = verifyTokenService.verifyTokenSignature(authorizationHeader);
        if (!jwtVerified) {
            log.error(FAILED_TO_VERIFY_TOKEN, authorizationHeader);
            responseServlet.sendError(403);
            return false;
        }
        return true;
    }
}
