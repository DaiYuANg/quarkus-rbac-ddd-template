package com.github.DaiYuANg.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

  @Test
  void accessTokenExpiresInUsesAccessTokenTtl() {
    AuthSecurityConfig config =
        new AuthSecurityConfig() {
          @Override
          public long accessTokenTtlSeconds() {
            return 120L;
          }

          @Override
          public long refreshTokenTtlSeconds() {
            return 3600L;
          }

          @Override
          public int loginFailureMaxAttempts() {
            return 5;
          }

          @Override
          public long loginFailureLockSeconds() {
            return 900L;
          }
        };

    var service = new JwtTokenService(config);

    assertEquals(120L, service.accessTokenExpiresIn());
  }
}
