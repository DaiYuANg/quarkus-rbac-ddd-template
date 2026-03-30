package com.github.DaiYuANg.modules.identity.application.profile.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.modules.identity.application.profile.SuperAdminProfileProvider;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SuperAdminProfileProviderTest {

  @Test
  void buildProfileUsesCurrentPrincipalPermissionsAndRoles() {
    var authority = mock(AuthorityVersionStore.class);
    when(authority.currentVersion()).thenReturn("v9");
    var provider = new SuperAdminProfileProvider(authority);
    var current =
        new CurrentAuthenticatedUser(
            "root", "Root", "SUPER_ADMIN", Set.of("super-admin", "", "  "), Set.of("a", "b"), Map.of());
    var vo = provider.buildProfile(current);
    assertEquals("root", vo.username());
    assertEquals("Root", vo.nickname());
    assertEquals(Set.of("a", "b"), vo.permissions());
    assertEquals(Set.of("super-admin"), vo.roleCodes());
    assertTrue(vo.authorityKey().startsWith("v9:"));
  }

  @Test
  void supportsSuperAdminTypeOnly() {
    var provider = new SuperAdminProfileProvider(mock(AuthorityVersionStore.class));
    assertTrue(
        provider.supports(
            new CurrentAuthenticatedUser("r", "R", "SUPER_ADMIN", Set.of(), Set.of(), Map.of())));
    assertEquals(
        false,
        provider.supports(
            new CurrentAuthenticatedUser("r", "R", "ADMIN", Set.of(), Set.of(), Map.of())));
  }
}
