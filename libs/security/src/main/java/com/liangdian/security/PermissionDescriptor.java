package com.liangdian.security;

import java.util.Optional;

public record PermissionDescriptor(
    String code,
    String domain,
    String resource,
    String action
) {
    public static PermissionDescriptor ofCode(String code) {
        if (code == null || code.isBlank()) {
            return new PermissionDescriptor("", "", "", "");
        }
        String[] parts = code.split(":", 3);
        if (parts.length == 3) {
            return new PermissionDescriptor(code, parts[0], parts[1], parts[2]);
        }
        return new PermissionDescriptor(code, "", "", "");
    }

    public static PermissionDescriptor of(String domain, String resource, String action) {
        return new PermissionDescriptor(domain + ":" + resource + ":" + action, domain, resource, action);
    }

    public Optional<String> codeOptional() {
        return code == null || code.isBlank() ? Optional.empty() : Optional.of(code);
    }
}
