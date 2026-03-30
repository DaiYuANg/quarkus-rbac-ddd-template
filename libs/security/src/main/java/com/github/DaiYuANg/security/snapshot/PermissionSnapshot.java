package com.github.DaiYuANg.security.snapshot;

import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.AuthenticatedUserBuilder;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.Map;
import java.util.Set;

@RecordBuilder
public record PermissionSnapshot(
  String username,
  String displayName,
  String userType,
  Set<String> roles,
  Set<String> permissions,
  String authorityVersion,
  Map<String, Object> attributes,
  Long userId) {
  public PermissionSnapshot(
    String username,
    String displayName,
    String userType,
    Set<String> roles,
    Set<String> permissions,
    String authorityVersion,
    Map<String, Object> attributes) {
    this(username, displayName, userType, roles, permissions, authorityVersion, attributes, null);
  }

  public AuthenticatedUser toAuthenticatedUser() {
    return AuthenticatedUserBuilder.builder()
      .username(username)
      .displayName(displayName)
      .userType(userType)
      .roles(roles)
      .permissions(permissions)
      .attributes(attributes)
      .userId(userId)
      .build();
  }
}
