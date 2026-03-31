package com.github.DaiYuANg.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.User;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.AuthenticatedUserBuilder;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import com.github.DaiYuANg.security.identity.QuarkusSecurityIdentityFactory;
import com.github.DaiYuANg.security.token.PrincipalAttributesSerializer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PrincipalAttributesSerializerBehaviorTest {

  private final PrincipalAttributesSerializer serializer = new PrincipalAttributesSerializer();

  @Test
  void securityIdentityRoundTripsToAuthenticatedUser() {
    var expected =
        AuthenticatedUserBuilder.builder()
            .username("alice")
            .displayName("Alice")
            .userType("ADMIN")
            .roles(Set.of("ops"))
            .permissions(Set.of(User.VIEW))
            .attributes(Map.of("tenantId", "t-1"))
            .userId(7L)
            .build();
    var identity = new QuarkusSecurityIdentityFactory(serializer).create(expected);

    var actual = serializer.toAuthenticatedUser(identity);

    assertEquals(expected.username(), actual.username());
    assertEquals(expected.displayName(), actual.displayName());
    assertEquals(expected.userType(), actual.userType());
    assertEquals(expected.roles(), actual.roles());
    assertEquals(expected.permissions(), actual.permissions());
    assertEquals(expected.userId(), actual.userId());
    assertEquals("t-1", actual.attributes().get("tenantId"));
  }

  @Test
  void toAuthenticatedUserFallsBackToPrincipalNameWhenUsernameMissing() {
    var actual =
        serializer.toAuthenticatedUser(
            Map.of(
                PrincipalAttributeKeys.DISPLAY_NAME,
                "Fallback User",
                PrincipalAttributeKeys.ROLES,
                List.of("admin"),
                PrincipalAttributeKeys.PERMISSIONS,
                Set.of(User.VIEW)),
            "principal-name");

    assertEquals("principal-name", actual.username());
    assertEquals("Fallback User", actual.displayName());
    assertEquals(Set.of("admin"), actual.roles());
    assertEquals(Set.of(User.VIEW), actual.permissions());
  }

  @Test
  void toAuthenticatedUserIgnoresUnparseableUserId() {
    var actual =
        serializer.toAuthenticatedUser(
            Map.of(PrincipalAttributeKeys.USER_ID, "not-a-number"), "principal-name");

    assertEquals("principal-name", actual.username());
    assertNull(actual.userId());
  }

  @Test
  void deserializeReturnsImmutableCopyOfAttributes() {
    var source = Map.<String, Object>of("scope", "admin");

    var actual = serializer.deserialize(source);

    assertEquals(source, actual);
    assertSame("admin", actual.get("scope"));
    assertThrows(UnsupportedOperationException.class, () -> actual.put("next", "value"));
  }
}
