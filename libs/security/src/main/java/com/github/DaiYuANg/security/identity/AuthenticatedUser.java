package com.github.DaiYuANg.security.identity;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.Map;
import java.util.Set;

@RecordBuilder
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
