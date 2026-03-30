package com.github.DaiYuANg.security.identity;

import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.NonNull;
import lombok.val;

@ApplicationScoped
public class SecurityPrincipalFactory {
  private static final String DEFAULT_USER_TYPE = "UNKNOWN";

  public AuthenticatedUser authenticatedUser(@NonNull SecurityPrincipalDefinition definition) {
    val normalizedUsername = definition.username().trim();
    val normalizedDisplayName =
        definition.displayName() == null || definition.displayName().isBlank()
            ? normalizedUsername
            : definition.displayName().trim();
    val normalizedUserType =
        definition.userType() == null || definition.userType().isBlank()
            ? DEFAULT_USER_TYPE
            : definition.userType().trim();
    val normalizedRoles = immutableCodes(definition.roles());
    val normalizedPermissions = immutableCodes(definition.permissions());
    val attributes = baseAttributes(normalizedUsername, normalizedDisplayName, normalizedUserType);
    attributes.put(PrincipalAttributeKeys.ROLES, normalizedRoles);
    attributes.put(PrincipalAttributeKeys.PERMISSIONS, normalizedPermissions);
    if (definition.userId() != null) {
      attributes.put(PrincipalAttributeKeys.USER_ID, definition.userId());
    }
    if (definition.source() != null && !definition.source().isBlank()) {
      attributes.put(PrincipalAttributeKeys.SOURCE, definition.source().trim());
    }
    if (definition.providerId() != null && !definition.providerId().isBlank()) {
      attributes.put(PrincipalAttributeKeys.PROVIDER_ID, definition.providerId().trim());
    }
    return new AuthenticatedUser(
        normalizedUsername,
        normalizedDisplayName,
        normalizedUserType,
        normalizedRoles,
        normalizedPermissions,
        Map.copyOf(attributes),
        definition.userId());
  }

  public PermissionSnapshot snapshot(
      @NonNull AuthenticatedUser user, @NonNull String authorityVersion) {
    val attributes = new LinkedHashMap<String, Object>();
    if (user.attributes() != null) {
      attributes.putAll(user.attributes());
    }
    attributes.putAll(baseAttributes(user.username(), user.displayName(), user.userType()));
    attributes.put(PrincipalAttributeKeys.ROLES, immutableCodes(user.roles()));
    attributes.put(PrincipalAttributeKeys.PERMISSIONS, immutableCodes(user.permissions()));
    attributes.put(PrincipalAttributeKeys.AUTHORITY_VERSION, authorityVersion);
    if (user.userId() != null) {
      attributes.put(PrincipalAttributeKeys.USER_ID, user.userId());
    }
    return new PermissionSnapshot(
        user.username(),
        user.displayName(),
        user.userType(),
        immutableCodes(user.roles()),
        immutableCodes(user.permissions()),
        authorityVersion,
        Map.copyOf(attributes),
        user.userId());
  }

  private Map<String, Object> baseAttributes(String username, String displayName, String userType) {
    val attributes = new LinkedHashMap<String, Object>();
    attributes.put(PrincipalAttributeKeys.SUBJECT, username);
    attributes.put(PrincipalAttributeKeys.USERNAME, username);
    attributes.put(PrincipalAttributeKeys.DISPLAY_NAME, displayName);
    attributes.put(PrincipalAttributeKeys.USER_TYPE, userType);
    return attributes;
  }

  private Set<String> immutableCodes(Set<String> values) {
    return values == null
        ? Set.of()
        : Set.copyOf(
            values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
  }
}
