package com.github.DaiYuANg.security;

import java.util.Optional;

/**
 * Permission in resource:action format (e.g. user:view, role:edit).
 */
public record PermissionDescriptor(
    String code,
    String resource,
    String action
) {
    public static PermissionDescriptor ofCode(String code) {
        if (code == null || code.isBlank()) {
            return new PermissionDescriptor("", "", "");
        }
        int colon = code.indexOf(':');
        if (colon > 0 && colon < code.length() - 1) {
            return new PermissionDescriptor(code, code.substring(0, colon), code.substring(colon + 1));
        }
        return new PermissionDescriptor(code, "", "");
    }

    public static PermissionDescriptor of(String resource, String action) {
        return new PermissionDescriptor(resource + ":" + action, resource, action);
    }

    public Optional<String> codeOptional() {
        return code == null || code.isBlank() ? Optional.empty() : Optional.of(code);
    }
}
