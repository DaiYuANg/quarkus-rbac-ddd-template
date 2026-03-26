package com.github.DaiYuANg.security.snapshot;

import java.util.Optional;

public interface PermissionSnapshotLoader {
  Optional<PermissionSnapshot> load(String username);

  default Optional<PermissionSnapshot> load(Long userId, String usernameHint) {
    if (usernameHint == null || usernameHint.isBlank()) {
      return Optional.empty();
    }
    return load(usernameHint);
  }
}
