package com.liangdian.security;

public interface AuthenticationSuccessHandler {
    AuthenticationResult onSuccess(LoginAuthenticationRequest request, AuthenticationResult result);
}
