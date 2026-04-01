package com.github.DaiYuANg.security.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import java.util.Optional;

@ConfigMapping(prefix = "app.security")
public interface AuthSecurityConfig {
  long accessTokenTtlSeconds();

  long refreshTokenTtlSeconds();

  int loginFailureMaxAttempts();

  long loginFailureLockSeconds();

  @WithName("super-admin")
  SuperAdmin superAdmin();

  interface SuperAdmin {
    Optional<String> username();

    @WithName("password-hash")
    Optional<String> passwordHash();

    Optional<String> displayName();
  }
}
