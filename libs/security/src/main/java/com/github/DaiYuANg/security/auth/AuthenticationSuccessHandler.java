package com.github.DaiYuANg.security.auth;

public interface AuthenticationSuccessHandler {
  AuthenticationResult onSuccess(LoginAuthenticationRequest request, AuthenticationResult result);
}
