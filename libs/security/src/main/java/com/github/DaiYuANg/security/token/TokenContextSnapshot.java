package com.github.DaiYuANg.security.token;

import io.quarkus.security.identity.SecurityIdentity;
import java.util.Map;
import java.util.Set;

public record TokenContextSnapshot(
    String subject,
    String userType,
    String displayName,
    Set<String> roles,
    Set<String> permissions,
    Map<String, Object> attributes,
    SecurityIdentity securityIdentity
) implements java.io.Serializable {
    public String actorKey() {
        return userType == null || userType.isBlank() ? subject : userType + ":" + subject;
    }
}
