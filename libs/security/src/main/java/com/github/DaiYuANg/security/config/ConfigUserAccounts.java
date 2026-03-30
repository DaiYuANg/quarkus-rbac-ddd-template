package com.github.DaiYuANg.security.config;

import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public final class ConfigUserAccounts {

  public static Optional<ConfigUserAccountConfig.ConfigUser> find(
      ConfigUserAccountConfig config, String username) {
    if (config == null || config.users() == null || username == null || username.isBlank()) {
      return Optional.empty();
    }
    var trimmed = username.trim();
    return config.users().values().stream()
        .filter(u -> u.username().equalsIgnoreCase(trimmed))
        .findFirst();
  }
}
