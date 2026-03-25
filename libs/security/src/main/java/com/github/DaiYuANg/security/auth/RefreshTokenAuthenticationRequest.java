package com.github.DaiYuANg.security.auth;

public record RefreshTokenAuthenticationRequest(String refreshToken)
    implements LoginAuthenticationRequest {
  @Override
  public String principal() {
    return refreshToken;
  }
}
