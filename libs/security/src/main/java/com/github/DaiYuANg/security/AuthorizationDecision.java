package com.github.DaiYuANg.security;

public record AuthorizationDecision(
    boolean allowed,
    PermissionDescriptor permission,
    String reason,
    String actorKey,
    String authorityVersion,
    String source,
    String matchedBy
) {
    public static AuthorizationDecision allow(PermissionDescriptor permission, CurrentAuthenticatedUser current, String matchedBy) {
        return new AuthorizationDecision(
            true,
            permission,
            "matched",
            current == null ? null : current.actorKey(),
            current == null ? null : current.attributeAsString("authorityVersion").orElse(null),
            current == null ? null : current.attributeAsString("source").orElse(null),
            matchedBy
        );
    }

    public static AuthorizationDecision deny(PermissionDescriptor permission, CurrentAuthenticatedUser current, String reason) {
        return new AuthorizationDecision(
            false,
            permission,
            reason,
            current == null ? null : current.actorKey(),
            current == null ? null : current.attributeAsString("authorityVersion").orElse(null),
            current == null ? null : current.attributeAsString("source").orElse(null),
            null
        );
    }

    public String permissionCode() {
        return permission == null ? null : permission.code();
    }
}
