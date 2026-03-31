package com.github.DaiYuANg.security.identity;

import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotBuilder;
import com.google.common.base.MoreObjects;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class SecurityPrincipalFactory {
  private static final String DEFAULT_USER_TYPE = "UNKNOWN";

  public AuthenticatedUser authenticatedUser(@NonNull SecurityPrincipalDefinition definition) {
    val normalizedUsername = definition.username().trim();
    val normalizedDisplayName =
        MoreObjects.firstNonNull(normalize(definition.displayName()), normalizedUsername);
    val normalizedUserType =
        MoreObjects.firstNonNull(normalize(definition.userType()), DEFAULT_USER_TYPE);
    val normalizedRoles = immutableCodes(definition.roles());
    val normalizedPermissions = immutableCodes(definition.permissions());
    val attributes = baseAttributes(normalizedUsername, normalizedDisplayName, normalizedUserType);
    attributes.put(PrincipalAttributeKeys.ROLES, normalizedRoles);
    attributes.put(PrincipalAttributeKeys.PERMISSIONS, normalizedPermissions);
    if (definition.userId() != null) {
      attributes.put(PrincipalAttributeKeys.USER_ID, definition.userId());
    }
    val source = normalize(definition.source());
    if (source != null) {
      attributes.put(PrincipalAttributeKeys.SOURCE, source);
    }
    val providerId = normalize(definition.providerId());
    if (providerId != null) {
      attributes.put(PrincipalAttributeKeys.PROVIDER_ID, providerId);
    }
    return AuthenticatedUserBuilder.builder()
      .username(normalizedUsername)
      .displayName(normalizedDisplayName)
      .userType(normalizedUserType)
      .roles(normalizedRoles)
      .permissions(normalizedPermissions)
      .attributes(Map.copyOf(attributes))
      .userId(definition.userId())
      .build();
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
    return PermissionSnapshotBuilder.builder()
      .username(user.username())
      .displayName(user.displayName())
      .userType(user.userType())
      .roles(user.roles())
      .permissions(user.permissions())
      .attributes(Map.copyOf(attributes))
      .userId(user.userId())
      .build();
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

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
