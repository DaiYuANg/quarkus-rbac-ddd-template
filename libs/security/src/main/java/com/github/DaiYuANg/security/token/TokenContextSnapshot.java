package com.github.DaiYuANg.security.token;

import io.quarkus.security.identity.SecurityIdentity;
import java.util.Map;
import java.util.Set;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

public record TokenContextSnapshot(
    String subject,
    String userType,
    String displayName,
    Set<String> roles,
    Set<String> permissions,
    Map<String, Object> attributes,
    SecurityIdentity securityIdentity)
    implements java.io.Serializable {
  public String actorKey() {
    val normalizedUserType = StringUtils.trimToNull(userType);
    return normalizedUserType == null ? subject : normalizedUserType + ":" + subject;
  }
}
