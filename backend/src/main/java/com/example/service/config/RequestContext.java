package com.example.service.config;

import com.example.service.auth.dto.User;
import org.springframework.security.oauth2.jwt.Jwt;

public record RequestContext(User user, Jwt token, String traceId) {
    static RequestContext empty() { return new RequestContext(null, null, null); }
}
