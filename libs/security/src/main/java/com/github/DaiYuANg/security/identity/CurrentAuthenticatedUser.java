package com.github.DaiYuANg.security.identity;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@RecordBuilder
public record CurrentAuthenticatedUser(
    String username,
    String displayName,
    String userType,
    Set<String> roles,
    Set<String> permissions,
    Map<String, Object> attributes) {
  public Optional<String> attributeAsString(String name) {
    val value = attributes == null ? null : attributes.get(name);
    return value == null ? Optional.empty() : Optional.of(String.valueOf(value));
  }

  public String actorKey() {
    val normalizedUserType = StringUtils.trimToNull(userType);
    return normalizedUserType == null ? username : normalizedUserType + ":" + username;
  }
}
