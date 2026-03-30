package com.github.DaiYuANg.security.authorization;

import java.util.Optional;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.Contract;

/** Permission in resource:action format (e.g. user:view, role:edit). */
public record PermissionDescriptor(String code, String resource, String action) {
  @Contract("null -> new")
  public static @NonNull PermissionDescriptor ofCode(String code) {
    if (code == null || code.isBlank()) {
      return new PermissionDescriptor("", "", "");
    }
    val colon = code.indexOf(':');
    if (colon > 0 && colon < code.length() - 1) {
      return new PermissionDescriptor(code, code.substring(0, colon), code.substring(colon + 1));
    }
    return new PermissionDescriptor(code, "", "");
  }

  @Contract("_, _ -> new")
  public static @NonNull PermissionDescriptor of(String resource, String action) {
    return new PermissionDescriptor(resource + ":" + action, resource, action);
  }

  public Optional<String> codeOptional() {
    return code == null || code.isBlank() ? Optional.empty() : Optional.of(code);
  }
}
