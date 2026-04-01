package com.github.DaiYuANg.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.DaiYuANg.security.auth.UsernamePasswordAuthenticationRequest;
import com.github.DaiYuANg.security.config.SuperAdminAuthorityVersion;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.token.PrincipalAttributesSerializer;
import com.github.DaiYuANg.testsupport.QuarkusPostgresValkeyTestProfile;
import com.github.DaiYuANg.testsupport.ValkeyTestResource;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import lombok.val;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(ValkeyTestResource.class)
@TestProfile(QuarkusPostgresValkeyTestProfile.class)
class SuperAdminAuthenticationProviderTest {

  @Inject IdentityProviderManager identityProviderManager;
  @Inject PrincipalAttributesSerializer principalAttributesSerializer;

  @Test
  void authenticatesSuperAdminThroughIdentityProviderManager() {
    val identity =
        identityProviderManager.authenticateBlocking(
            new UsernamePasswordAuthenticationRequest("root", "root"));
    assertNotNull(identity);
    assertEquals("root", identity.getPrincipal().getName());
    assertEquals(
        SuperAdminAuthorityVersion.VALUE,
        java.util.Objects.toString(identity.getAttribute("authorityVersion"), null));

    AuthenticatedUser user = principalAttributesSerializer.toAuthenticatedUser(identity);
    assertEquals("root", user.username());
    assertEquals("SUPER_ADMIN", user.userType());
  }
}

