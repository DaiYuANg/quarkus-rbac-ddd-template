package com.github.DaiYuANg.security.auth;

import io.quarkus.security.identity.request.AuthenticationRequest;
import java.util.Map;

public sealed interface LoginAuthenticationRequest extends AuthenticationRequest
    permits UsernamePasswordAuthenticationRequest, RefreshTokenAuthenticationRequest {
  String principal();

  @Override
  default Map<String, Object> getAttributes() {
    return Map.of();
  }

  @Override
  default <T> T getAttribute(String name) {
    return null;
  }

  @Override
  default void setAttribute(String key, Object value) {}
}
