package com.github.DaiYuANg.security.config;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public final class ConfigUserAccounts {

  public static Optional<ConfigUserAccountConfig.ConfigUser> find(
      ConfigUserAccountConfig config, String username) {
    if (config == null || config.users() == null || username == null || username.isBlank()) {
      return Optional.empty();
    }
    val trimmed = username.trim();
    return config.users().values().stream()
        .filter(u -> u.username().equalsIgnoreCase(trimmed))
        .findFirst();
  }
}
