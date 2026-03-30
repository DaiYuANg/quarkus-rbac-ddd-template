package com.github.DaiYuANg.security.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.Config;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DefaultReplayProtectionConfig implements ReplayProtectionConfig {
  private static final String PREFIX = "app.replay.";

  private final Config config;

  @Override
  public boolean enabled() {
    return value("enabled", Boolean.class, true);
  }

  @Override
  public String timestampHeader() {
    return value("timestamp-header", String.class, "X-Timestamp");
  }

  @Override
  public String nonceHeader() {
    return value("nonce-header", String.class, "X-Nonce");
  }

  @Override
  public int maxSkewSeconds() {
    return value("max-skew-seconds", Integer.class, 60);
  }

  @Override
  public int nonceTtlSeconds() {
    return value("nonce-ttl-seconds", Integer.class, 120);
  }

  @Override
  public boolean timestampEpochSeconds() {
    return value("timestamp-epoch-seconds", Boolean.class, true);
  }

  @Override
  public int maxNonceLength() {
    return value("max-nonce-length", Integer.class, 128);
  }

  private <T> T value(@NonNull String key, @NonNull Class<T> type, @NonNull T defaultValue) {
    return config.getOptionalValue(PREFIX + key, type).orElse(defaultValue);
  }
}
