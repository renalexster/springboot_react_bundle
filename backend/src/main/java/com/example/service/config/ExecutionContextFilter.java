package com.example.service.config;

import com.example.service.auth.dto.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.example.service.utils.StringUtils.joinNonBlank;
import static com.example.service.utils.StringUtils.trimToNull;

/**
 * Populates {@link ExecutionContextImpl} from the Spring Security context for each request.
 * Ensures clearing ThreadLocal after the request is processed to avoid leaks.
 */
@Component
public class ExecutionContextFilter extends OncePerRequestFilter {

    private final ExecutionContextImpl executionContext;

    public ExecutionContextFilter(ExecutionContextImpl executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth && authentication.isAuthenticated()) {
                Jwt jwt = jwtAuth.getToken();
                String given = jwt.getClaimAsString("given_name");
                String family = jwt.getClaimAsString("family_name");
                String email = jwt.getClaimAsString("email");
                String name = resolveDisplayName(jwt, given, family, email);
                User user = new User(name, email, given, family);
                String traceId = request.getHeader("x-traceId"); // HTTP headers are case-insensitive
                if (traceId != null) {
                    MDC.put("traceId", traceId);
                }
                executionContext.set(user, jwt, traceId);
            }
            filterChain.doFilter(request, response);
        } finally {
            executionContext.clear();
            MDC.clear();
        }
    }

    private static String resolveDisplayName(Jwt jwt, String given, String family, String email) {
        // 1) Explicit name claim
        String name = trimToNull(jwt.getClaimAsString("name"));
        if (name != null) return name;

        // 2) Full name composed from given + family
        String fullName = joinNonBlank(given, family);
        if (fullName != null) return fullName;

        // 3) Preferred username (common with some IdPs)
        String preferred = trimToNull(jwt.getClaimAsString("preferred_username"));
        if (preferred != null) return preferred;

        // 4) Email
        String emailTrim = trimToNull(email);
        if (emailTrim != null) return emailTrim;

        // 5) Fallback to subject
        String sub = jwt.getSubject();
        return sub != null ? sub : "";
    }

}
