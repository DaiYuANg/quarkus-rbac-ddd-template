package com.liangdian.security;

import jakarta.annotation.Priority;

public interface LoginAuthenticationProvider<T extends LoginAuthenticationRequest> {
    String providerId();

    boolean supports(LoginAuthenticationRequest request);

    AuthenticationProviderResult authenticate(T request);

    default int order() {
        Priority priority = getClass().getAnnotation(Priority.class);
        return priority != null ? priority.value() : 1000;
    }
}
