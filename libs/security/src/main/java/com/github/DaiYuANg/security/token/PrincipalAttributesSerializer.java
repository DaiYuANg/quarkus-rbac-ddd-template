package com.github.DaiYuANg.security.token;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.AuthenticatedUserBuilder;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUserBuilder;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.val;

@ApplicationScoped
public class PrincipalAttributesSerializer {
  public Map<String, Object> toAttributes(@NonNull AuthenticatedUser user) {
    val attributes = new LinkedHashMap<String, Object>();
    if (user.attributes() != null) {
      attributes.putAll(user.attributes());
    }
    if (user.userId() != null) {
      attributes.put(PrincipalAttributeKeys.USER_ID, user.userId());
    }
    attributes.put(PrincipalAttributeKeys.SUBJECT, user.username());
    attributes.put(PrincipalAttributeKeys.DISPLAY_NAME, user.displayName());
    attributes.put(PrincipalAttributeKeys.USER_TYPE, user.userType());
    attributes.put(PrincipalAttributeKeys.ROLES, immutableCopy(user.roles()));
    attributes.put(PrincipalAttributeKeys.PERMISSIONS, immutableCopy(user.permissions()));
    return ImmutableMap.copyOf(attributes);
  }

  public Map<String, Object> deserialize(Map<String, Object> attributes) {
    return attributes == null ? Map.of() : ImmutableMap.copyOf(attributes);
  }

  public CurrentAuthenticatedUser toCurrentUser(
      Map<String, Object> attributes, @NonNull String principalName) {
    val values = attributes == null ? Map.<String, Object>of() : attributes;
    return CurrentAuthenticatedUserBuilder
        .builder()
        .username(stringValue(values.get(PrincipalAttributeKeys.USERNAME), principalName))
        .displayName((String) values.get(PrincipalAttributeKeys.DISPLAY_NAME))
        .userType((String) values.get(PrincipalAttributeKeys.USER_TYPE))
        .roles(asSet(values.get(PrincipalAttributeKeys.ROLES)))
        .permissions(asSet(values.get(PrincipalAttributeKeys.PERMISSIONS)))
        .build();
  }

  public AuthenticatedUser toAuthenticatedUser(@NonNull SecurityIdentity identity) {
    return toAuthenticatedUser(identity.getAttributes(), identity.getPrincipal().getName());
  }

  public AuthenticatedUser toAuthenticatedUser(
      Map<String, Object> attributes, @NonNull String principalName) {
    val values = attributes == null ? Map.<String, Object>of() : attributes;
    val username = stringValue(values.get(PrincipalAttributeKeys.USERNAME), principalName);
    val displayName = stringValue(values.get(PrincipalAttributeKeys.DISPLAY_NAME), username);
    val userType = stringValue(values.get(PrincipalAttributeKeys.USER_TYPE), "UNKNOWN");
    return AuthenticatedUserBuilder.builder()
        .username(username)
        .displayName(displayName)
        .userType(userType)
        .roles(asSet(values.get(PrincipalAttributeKeys.ROLES)))
        .permissions(asSet(values.get(PrincipalAttributeKeys.PERMISSIONS)))
        .attributes(deserialize(values))
        .userId(asLong(values.get(PrincipalAttributeKeys.USER_ID)))
        .build();
  }

  private Set<String> asSet(Object value) {
    if (value instanceof Set<?> set) {
      return set.stream()
          .map(String::valueOf)
          .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ImmutableSet::copyOf));
    }
    if (value instanceof Iterable<?> iterable) {
      return java.util.stream.StreamSupport.stream(iterable.spliterator(), false)
          .filter(Objects::nonNull)
          .map(String::valueOf)
          .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ImmutableSet::copyOf));
    }
    if (value == null) {
      return Set.of();
    }
    return ImmutableSet.of(String.valueOf(value));
  }

  private Set<String> immutableCopy(Set<String> values) {
    return values == null ? Set.of() : ImmutableSet.copyOf(values);
  }

  private String stringValue(Object value, @NonNull String defaultValue) {
    val text = value == null ? null : String.valueOf(value);
    return text == null || text.isBlank() ? defaultValue : text;
  }

  private Long asLong(Object value) {
    if (value instanceof Number number) {
      return number.longValue();
    }
    if (value == null) {
      return null;
    }
    try {
      return Long.parseLong(String.valueOf(value).trim());
    } catch (NumberFormatException ignored) {
      return null;
    }
  }
}
