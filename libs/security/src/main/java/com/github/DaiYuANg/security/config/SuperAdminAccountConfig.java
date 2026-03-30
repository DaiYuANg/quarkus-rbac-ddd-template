package com.github.DaiYuANg.security.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import java.util.Optional;

@ConfigMapping(prefix = "app.security.super-admin")
public interface SuperAdminAccountConfig {
  Optional<String> username();

  @WithName("password-hash")
  Optional<String> passwordHash();

  Optional<String> displayName();
}
