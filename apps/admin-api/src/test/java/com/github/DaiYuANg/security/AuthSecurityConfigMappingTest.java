package com.github.DaiYuANg.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.DaiYuANg.security.config.AuthSecurityConfig;
import com.github.DaiYuANg.testsupport.QuarkusPostgresValkeyTestProfile;
import com.github.DaiYuANg.testsupport.ValkeyTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(ValkeyTestResource.class)
@TestProfile(QuarkusPostgresValkeyTestProfile.class)
class AuthSecurityConfigMappingTest {

  @Inject AuthSecurityConfig authSecurityConfig;

  @Test
  void bindsSuperAdminConfig() {
    assertEquals("root", authSecurityConfig.superAdmin().username().orElse(null));
    assertEquals(
        "$2a$10$c2V22M8f.jUtSuBz.I4r1uHTCwMWsnRabXr6fs9gKdmoRThu/j3a6",
        authSecurityConfig.superAdmin().passwordHash().orElse(null));
    assertEquals("Super Admin", authSecurityConfig.superAdmin().displayName().orElse(null));
  }
}
