package com.github.DaiYuANg.security.auth;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DefaultAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
  @Override
  public AuthenticationResult onSuccess(
      LoginAuthenticationRequest request, AuthenticationResult result) {
    return result;
  }
}
