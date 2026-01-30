package com.example.service.auth;

import com.example.service.auth.dto.VerifyResponse;
import com.example.service.auth.dto.User;
import com.example.service.config.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ExecutionContext executionContext;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify() {
        User user = executionContext.getContext().user();
        logger.info(executionContext.getContext().traceId());
        return ResponseEntity.ok(new VerifyResponse(user.name()));
    }
}
