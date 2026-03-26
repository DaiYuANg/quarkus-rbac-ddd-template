package com.github.DaiYuANg.security.token;

import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class PrincipalAttributesSerializer {
  public Map<String, Object> toAttributes(AuthenticatedUser user) {
    var attributes = new LinkedHashMap<String, Object>();
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

  @SuppressWarnings("unchecked")
  public CurrentAuthenticatedUser toCurrentUser(
      Map<String, Object> attributes, String principalName) {
    var values = attributes == null ? Map.<String, Object>of() : attributes;
    return new CurrentAuthenticatedUser(
        stringValue(values.getOrDefault(PrincipalAttributeKeys.SUBJECT, principalName), principalName),
        stringValue(
            values.getOrDefault(PrincipalAttributeKeys.DISPLAY_NAME, principalName), principalName),
        stringValue(values.getOrDefault(PrincipalAttributeKeys.USER_TYPE, "SYSTEM"), "SYSTEM"),
        asSet(values.get(PrincipalAttributeKeys.ROLES)),
        asSet(values.get(PrincipalAttributeKeys.PERMISSIONS)),
        values);
  }

  private Set<String> asSet(Object value) {
    if (value instanceof Set<?> set) {
      return set.stream()
          .map(String::valueOf)
          .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }
    if (value instanceof Iterable<?> iterable) {
      var result = new LinkedHashSet<String>();
      iterable.forEach(item -> result.add(String.valueOf(item)));
      return result;
    }
    if (value == null) {
      return Set.of();
    }
    return Set.of(String.valueOf(value));
  }

  private Set<String> immutableCopy(Set<String> values) {
    return values == null ? Set.of() : Set.copyOf(values);
  }

  private String stringValue(Object value, String defaultValue) {
    return value == null ? defaultValue : String.valueOf(value);
  }
}
