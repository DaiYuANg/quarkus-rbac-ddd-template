package com.github.DaiYuANg.security.identity;

import java.util.Set;
import lombok.Builder;
import lombok.NonNull;

@Builder
public record SecurityPrincipalDefinition(
    @NonNull String username,
    String displayName,
    String userType,
    String source,
    String providerId,
    Set<String> roles,
    Set<String> permissions,
    Long userId) {}
