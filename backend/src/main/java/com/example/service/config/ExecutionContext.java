package com.example.service.config;

import com.example.service.auth.dto.User;

public interface ExecutionContext {

    String getAuthorizationToken();

    User getUserAuthenticated();

    RequestContext getContext();

}
