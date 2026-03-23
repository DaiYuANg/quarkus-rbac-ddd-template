package com.github.DaiYuANg.security;

import java.util.Map;
import java.util.Set;

public record PermissionSnapshot(
    String username,
    String displayName,
    String userType,
    Set<String> roles,
    Set<String> permissions,
    String authorityVersion,
    Map<String, Object> attributes,
    Long userId
) {
    public PermissionSnapshot(String username, String displayName, String userType,
            Set<String> roles, Set<String> permissions, String authorityVersion,
            Map<String, Object> attributes) {
        this(username, displayName, userType, roles, permissions, authorityVersion, attributes, null);
    }

    public AuthenticatedUser toAuthenticatedUser() {
        return new AuthenticatedUser(username, displayName, userType, roles, permissions, attributes, userId);
    }
}
