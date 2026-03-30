package com.github.DaiYuANg.security.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "app.identity")
public interface IdentityPrincipalConfig {
  @WithName("db-user-type")
  @WithDefault("ADMIN")
  String dbUserType();
}
