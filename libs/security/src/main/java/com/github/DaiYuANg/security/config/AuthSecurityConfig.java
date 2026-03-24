package com.github.DaiYuANg.security.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app.security")
public interface AuthSecurityConfig {
    long accessTokenTtlSeconds();
    long refreshTokenTtlSeconds();
    int loginFailureMaxAttempts();
    long loginFailureLockSeconds();
}
