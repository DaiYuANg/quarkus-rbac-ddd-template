package com.github.DaiYuANg.security.config;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * Stable synthetic primary key for the configured super admin account so permission snapshots can
 * follow the same Valkey layout as database users.
 */
@UtilityClass
public final class SuperAdminAuthorityId {

  /** Deterministic negative long derived from username; stable across JVM restarts. */
  public long forUsername(@NonNull String username) {
    val normalizedUsername = StringUtils.trimToNull(username);
    if (normalizedUsername == null) {
      throw new IllegalArgumentException("username required");
    }
    val uuid =
        UUID.nameUUIDFromBytes(("super-admin:" + normalizedUsername).getBytes(StandardCharsets.UTF_8));
    val msb = uuid.getMostSignificantBits();
    val lsb = uuid.getLeastSignificantBits();
    val combined = msb ^ lsb;
    if (combined >= 0) {
      return combined == 0 ? -1L : -combined;
    }
    return combined;
  }
}
