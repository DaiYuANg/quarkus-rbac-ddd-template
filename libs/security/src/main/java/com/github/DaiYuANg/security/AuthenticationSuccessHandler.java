package com.github.DaiYuANg.security;

public interface AuthenticationSuccessHandler {
    AuthenticationResult onSuccess(LoginAuthenticationRequest request, AuthenticationResult result);
}
