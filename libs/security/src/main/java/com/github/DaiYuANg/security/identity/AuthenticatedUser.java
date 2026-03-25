package com.github.DaiYuANg.security.identity;

import java.util.Map;
import java.util.Set;

public record AuthenticatedUser(
    String username,
    String displayName,
    String userType,
    Set<String> roles,
    Set<String> permissions,
    Map<String, Object> attributes,
    Long userId) {
  public AuthenticatedUser(
      String username,
      String displayName,
      String userType,
      Set<String> roles,
      Set<String> permissions,
      Map<String, Object> attributes) {
    this(username, displayName, userType, roles, permissions, attributes, null);
  }
}
