package com.example.service.config;

import com.example.service.auth.dto.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth && authentication.isAuthenticated()) {
                Jwt jwt = jwtAuth.getToken();
                Map<String, Object> claims = jwt.getClaims();
                String given = (String) claims.get("given_name");
                String family = (String) claims.get("family_name");
                String email = (String) claims.get("email");
                String name;
                Object nameObj = claims.get("name");
                if (nameObj != null) {
                    name = String.valueOf(nameObj);
                } else if (given != null || family != null) {
                    name = (given != null ? given : "") + (family != null ? (" " + family) : "");
                } else if (email != null) {
                    name = email;
                } else {
                    name = jwt.getSubject();
                }
                User user = new User(name, email, given, family);
                executionContext.set(user, jwt, request.getHeader("x-traceId"));
            }
            filterChain.doFilter(request, response);
        } finally {
            executionContext.clear();
        }
    }
}
