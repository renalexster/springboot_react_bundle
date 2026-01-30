package com.example.service.config;

import com.example.service.auth.dto.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Thread-local backed execution context for the current HTTP request.
 * Stores the authenticated User and raw JWT token value when available.
 */
@Component
public class ExecutionContextImpl implements ExecutionContext {

    private static final ThreadLocal<RequestContext> HOLDER = ThreadLocal.withInitial(RequestContext::empty);

    @Override
    public String getAuthorizationToken() {
        return HOLDER.get().token().getTokenValue();
    }

    @Override
    public RequestContext getContext() {return HOLDER.get();}

    @Override
    public User getUserAuthenticated() {
        return HOLDER.get().user();
    }

    // Methods used by the request filter
    void set(User user, Jwt token, String traceId) {
        HOLDER.set(new RequestContext(user, token, traceId));
    }

    void clear() {
        HOLDER.remove();
    }

}
