package com.github.DaiYuANg.security.authorization;

import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;

public record AuthorizationDecision(
    boolean allowed,
    PermissionDescriptor permission,
    String reason,
    String actorKey,
    String authorityVersion,
    String source,
    String matchedBy) {
  public static AuthorizationDecision allow(
      PermissionDescriptor permission, CurrentAuthenticatedUser current, String matchedBy) {
    return new AuthorizationDecision(
        true,
        permission,
        "matched",
        current == null ? null : current.actorKey(),
        current == null
            ? null
            : current.attributeAsString(PrincipalAttributeKeys.AUTHORITY_VERSION).orElse(null),
        current == null
            ? null
            : current.attributeAsString(PrincipalAttributeKeys.SOURCE).orElse(null),
        matchedBy);
  }

  public static AuthorizationDecision deny(
      PermissionDescriptor permission, CurrentAuthenticatedUser current, String reason) {
    return new AuthorizationDecision(
        false,
        permission,
        reason,
        current == null ? null : current.actorKey(),
        current == null
            ? null
            : current.attributeAsString(PrincipalAttributeKeys.AUTHORITY_VERSION).orElse(null),
        current == null
            ? null
            : current.attributeAsString(PrincipalAttributeKeys.SOURCE).orElse(null),
        null);
  }

  public String permissionCode() {
    return permission == null ? null : permission.code();
  }
}
