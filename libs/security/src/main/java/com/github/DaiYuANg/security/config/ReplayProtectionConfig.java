package com.github.DaiYuANg.security.config;

public interface ReplayProtectionConfig {
  boolean enabled();

  String timestampHeader();

  String nonceHeader();

  int maxSkewSeconds();

  int nonceTtlSeconds();

  boolean timestampEpochSeconds();

  int maxNonceLength();
}
