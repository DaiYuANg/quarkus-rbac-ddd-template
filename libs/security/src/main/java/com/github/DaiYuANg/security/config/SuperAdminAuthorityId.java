package com.github.DaiYuANg.security.config;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Stable synthetic primary key for the configured super admin account so permission snapshots can
 * follow the same Valkey layout as database users.
 */
@UtilityClass
public final class SuperAdminAuthorityId {

  /** Deterministic negative long derived from username; stable across JVM restarts. */
  public long forUsername(String username) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username required");
    }
    val uuid =
        UUID.nameUUIDFromBytes(("super-admin:" + username.trim()).getBytes(StandardCharsets.UTF_8));
    val msb = uuid.getMostSignificantBits();
    val lsb = uuid.getLeastSignificantBits();
    val combined = msb ^ lsb;
    if (combined >= 0) {
      return combined == 0 ? -1L : -combined;
    }
    return combined;
  }
}
