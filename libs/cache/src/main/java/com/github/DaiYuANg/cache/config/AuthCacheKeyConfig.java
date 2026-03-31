package com.github.DaiYuANg.cache.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "app.cache.auth")
public interface AuthCacheKeyConfig {
  @WithName("authority-version-key")
  @WithDefault("rbac-auth:authority:global-version")
  String authorityVersionKey();

  @WithName("login-failure-prefix")
  @WithDefault("rbac-auth:login:failure")
  String loginFailurePrefix();

  @WithName("login-lock-prefix")
  @WithDefault("rbac-auth:login:lock")
  String loginLockPrefix();

  @WithName("refresh-token-prefix")
  @WithDefault("rbac-auth:refresh")
  String refreshTokenPrefix();

  @WithName("replay-nonce-prefix")
  @WithDefault("replay:nonce")
  String replayNoncePrefix();

  default String loginFailureKey(String username) {
    return loginFailurePrefix() + ":" + username;
  }

  default String loginLockKey(String username) {
    return loginLockPrefix() + ":" + username;
  }

  default String refreshTokenKey(String refreshToken) {
    return refreshTokenPrefix() + ":" + refreshToken;
  }

  default String refreshUserKey(String username) {
    return refreshTokenPrefix() + ":user:" + username;
  }

  default String refreshUserIdKey(Long userId) {
    return refreshTokenPrefix() + ":user-id:" + userId;
  }

  default String replayNonceKey(String digest) {
    return replayNoncePrefix() + ":" + digest;
  }
}
