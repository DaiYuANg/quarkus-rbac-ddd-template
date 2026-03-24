package com.github.DaiYuANg.security.identity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record CurrentAuthenticatedUser(
    String username,
    String displayName,
    String userType,
    Set<String> roles,
    Set<String> permissions,
    Map<String, Object> attributes
) {
    public Optional<String> attributeAsString(String name) {
        Object value = attributes == null ? null : attributes.get(name);
        return value == null ? Optional.empty() : Optional.of(String.valueOf(value));
    }

    public String actorKey() {
        return userType == null || userType.isBlank() ? username : userType + ":" + username;
    }
}
