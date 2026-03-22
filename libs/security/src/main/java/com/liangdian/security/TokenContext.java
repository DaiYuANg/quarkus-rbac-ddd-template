package com.liangdian.security;

import io.quarkus.security.identity.SecurityIdentity;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record TokenContext(
    String subject,
    String userType,
    String displayName,
    Set<String> roles,
    Set<String> permissions,
    Map<String, Object> attributes,
    SecurityIdentity securityIdentity
) {
    public Optional<String> attributeAsString(String name) {
        Object value = attributes == null ? null : attributes.get(name);
        return value == null ? Optional.empty() : Optional.of(String.valueOf(value));
    }

    public Optional<Principal> principal() {
        return Optional.ofNullable(securityIdentity).map(SecurityIdentity::getPrincipal);
    }

    public String actorKey() {
        return userType == null || userType.isBlank() ? subject : userType + ":" + subject;
    }
}
