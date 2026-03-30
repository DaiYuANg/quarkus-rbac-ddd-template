package com.github.DaiYuANg.security.token;

import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUserBuilder;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
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
    return attributes;
  }

  public Map<String, Object> deserialize(Map<String, Object> attributes) {
    return attributes == null ? Map.of() : new LinkedHashMap<>(attributes);
  }

  public CurrentAuthenticatedUser toCurrentUser(
    Map<String, Object> attributes, @NonNull String principalName) {
    val values = attributes == null ? Map.<String, Object>of() : attributes;
    return CurrentAuthenticatedUserBuilder
      .builder().username((String) values.get(PrincipalAttributeKeys.USER_ID))
      .displayName((String) values.get(PrincipalAttributeKeys.DISPLAY_NAME))
      .userType((String) values.get(PrincipalAttributeKeys.USER_TYPE))
      .roles(asSet(values.get(PrincipalAttributeKeys.ROLES)))
      .permissions(asSet(values.get(PrincipalAttributeKeys.PERMISSIONS)))
      .build();
  }

  private Set<String> asSet(Object value) {
    if (value instanceof Set<?> set) {
      return set.stream()
        .map(String::valueOf)
        .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    if (value instanceof Iterable<?> iterable) {
      return java.util.stream.StreamSupport.stream(iterable.spliterator(), false)
        .filter(Objects::nonNull)
        .map(String::valueOf)
        .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    if (value == null) {
      return Set.of();
    }
    return Set.of(String.valueOf(value));
  }

  private Set<String> immutableCopy(Set<String> values) {
    return values == null ? Set.of() : Set.copyOf(values);
  }

  private String stringValue(Object value, @NonNull String defaultValue) {
    return value == null ? defaultValue : String.valueOf(value);
  }
}
