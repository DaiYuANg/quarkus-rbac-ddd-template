package com.github.DaiYuANg.modules.identity.application.profile.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.cache.PermissionCatalogEntry;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.modules.identity.application.profile.ConfigUserProfileProvider;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConfigUserProfileProviderTest {

  @Test
  void buildProfileUsesFullCatalogAndPrincipalRoles() {
    var catalog = mock(PermissionCatalogStore.class);
    when(catalog.getAll())
        .thenReturn(
            List.of(
                entry(1L, "a"),
                entry(2L, "b"),
                entry(3L, "")));
    var authority = mock(AuthorityVersionStore.class);
    when(authority.currentVersion()).thenReturn("v9");
    var provider = new ConfigUserProfileProvider(catalog, authority);
    var current =
        new CurrentAuthenticatedUser(
            "root",
            "Root",
            "CONFIG",
            Set.of("admin", "", "  "),
            Set.of("x"),
            Map.of());
    var vo = provider.buildProfile(current);
    assertEquals("root", vo.username());
    assertEquals("Root", vo.nickname());
    assertEquals(Set.of("a", "b"), vo.permissions());
    assertEquals(Set.of("admin"), vo.roleCodes());
    assertTrue(vo.authorityKey().startsWith("v9:"));
  }

  @Test
  void supportsConfigUserTypeOnly() {
    var provider =
        new ConfigUserProfileProvider(
            mock(PermissionCatalogStore.class), mock(AuthorityVersionStore.class));
    assertTrue(
        provider.supports(
            new CurrentAuthenticatedUser("r", "R", "CONFIG", Set.of(), Set.of(), Map.of())));
    assertEquals(
        false,
        provider.supports(
            new CurrentAuthenticatedUser("r", "R", "ADMIN", Set.of(), Set.of(), Map.of())));
  }

  private static PermissionCatalogEntry entry(long id, String code) {
    return new PermissionCatalogEntry(
        id,
        "",
        code,
        "",
        "",
        "",
        "",
        "",
        Instant.EPOCH,
        Instant.EPOCH);
  }
}
