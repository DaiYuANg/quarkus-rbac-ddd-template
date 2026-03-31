package com.github.DaiYuANg.security.config;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigMapping(prefix = "app.replay")
public interface ReplayProtectionConfig {
  @WithDefault("true")
  boolean enabled();

  @WithName("timestamp-header")
  @WithDefault("X-Timestamp")
  String timestampHeader();

  @WithName("nonce-header")
  @WithDefault("X-Nonce")
  String nonceHeader();

  @WithName("max-skew-seconds")
  @WithDefault("60")
  int maxSkewSeconds();

  @WithName("nonce-ttl-seconds")
  @WithDefault("120")
  int nonceTtlSeconds();

  @WithName("timestamp-epoch-seconds")
  @WithDefault("true")
  boolean timestampEpochSeconds();

  @WithName("max-nonce-length")
  @WithDefault("128")
  int maxNonceLength();
}
