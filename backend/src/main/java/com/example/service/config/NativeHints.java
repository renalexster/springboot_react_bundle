package com.example.service.config;

import com.example.service.auth.dto.User;
import com.example.service.auth.dto.VerifyResponse;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * Provides runtime hints for GraalVM Native Image compilation.
 * Registers classes that need reflection, serialization, or resource access.
 */
@Configuration
@ImportRuntimeHints(NativeHints.Registrar.class)
public class NativeHints {

    static class Registrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register DTOs for JSON serialization/deserialization
            hints.reflection()
                    .registerType(User.class, hint -> hint
                            .withMembers(org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                                    org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS))
                    .registerType(VerifyResponse.class, hint -> hint
                            .withMembers(org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                                    org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS));

            // Register Google API client classes if needed
            // Uncomment if you encounter reflection issues with Google libraries
            // hints.reflection()
            //         .registerType(com.google.api.client.json.JsonFactory.class,
            //                 hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
        }
    }
}
