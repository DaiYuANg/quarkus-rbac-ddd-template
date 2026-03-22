package com.github.DaiYuANg.security;

import java.util.Map;
import java.util.Set;

public record AuthenticatedUser(
    String username,
    String displayName,
    String userType,
    Set<String> roles,
    Set<String> permissions,
    Map<String, Object> attributes
) {}
