package com.github.DaiYuANg.security.snapshot;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public interface PermissionSnapshotLoader {
  Optional<PermissionSnapshot> load(String username);

  default Optional<PermissionSnapshot> load(Long userId, String usernameHint) {
    String normalizedUsernameHint = StringUtils.trimToNull(usernameHint);
    if (normalizedUsernameHint == null) {
      return Optional.empty();
    }
    return load(normalizedUsernameHint);
  }
}
