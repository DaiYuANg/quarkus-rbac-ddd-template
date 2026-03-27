package com.github.DaiYuANg.modules.security.runtime.identity.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.github.DaiYuANg.cache.PermissionSnapshotStore;
import com.github.DaiYuANg.modules.security.runtime.identity.AdminPermissionSecurityIdentityAugmentor;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import com.github.DaiYuANg.security.identity.QuarkusSecurityIdentityFactory;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotLoader;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotRefreshPolicy;
import com.github.DaiYuANg.security.token.PrincipalAttributesSerializer;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class AdminPermissionSecurityIdentityAugmentorTest {

  @Test
  void staleJwtWithoutUserIdIsRejectedInsteadOfFallingBackToRawIdentity() {
    var permissionSnapshotStore = mock(PermissionSnapshotStore.class);
    var permissionSnapshotLoader = mock(PermissionSnapshotLoader.class);
    var refreshPolicy = mock(PermissionSnapshotRefreshPolicy.class);
    when(permissionSnapshotStore.get("alice")).thenReturn(Optional.empty());
    when(refreshPolicy.shouldReuse(eq("v1"), any())).thenReturn(false);
    var augmentor =
        new AdminPermissionSecurityIdentityAugmentor(
            new QuarkusSecurityIdentityFactory(new PrincipalAttributesSerializer()),
            permissionSnapshotStore,
            permissionSnapshotLoader,
            refreshPolicy);

    SecurityIdentity identity =
        QuarkusSecurityIdentity.builder()
            .setPrincipal(() -> "alice")
            .setAnonymous(false)
            .addAttribute(PrincipalAttributeKeys.AUTHORITY_VERSION, "v1")
            .build();

    var result = augmentor.augment(identity, immediateContext()).await().indefinitely();

    assertTrue(result.isAnonymous());
    verifyNoInteractions(permissionSnapshotLoader);
  }

  @Test
  void userIdBoundJwtIsRejectedWhenLoadedSnapshotBelongsToRenamedPrincipal() {
    var permissionSnapshotStore = mock(PermissionSnapshotStore.class);
    var permissionSnapshotLoader = mock(PermissionSnapshotLoader.class);
    var refreshPolicy = mock(PermissionSnapshotRefreshPolicy.class);
    when(permissionSnapshotStore.get(7L)).thenReturn(Optional.empty());
    when(refreshPolicy.shouldReuse(eq("v2"), any())).thenReturn(false);
    when(permissionSnapshotLoader.load(7L, "alice"))
        .thenReturn(
            Optional.of(
                new PermissionSnapshot(
                    "alice-renamed",
                    "Alice",
                    "ADMIN",
                    Set.of("ops"),
                    Set.of("user:view"),
                    "v3",
                    Map.of(),
                    7L)));
    var augmentor =
        new AdminPermissionSecurityIdentityAugmentor(
            new QuarkusSecurityIdentityFactory(new PrincipalAttributesSerializer()),
            permissionSnapshotStore,
            permissionSnapshotLoader,
            refreshPolicy);

    SecurityIdentity identity =
        QuarkusSecurityIdentity.builder()
            .setPrincipal(() -> "alice")
            .setAnonymous(false)
            .addAttribute(PrincipalAttributeKeys.AUTHORITY_VERSION, "v2")
            .addAttribute(PrincipalAttributeKeys.USER_ID, 7L)
            .build();

    var result = augmentor.augment(identity, immediateContext()).await().indefinitely();

    assertTrue(result.isAnonymous());
  }

  private AuthenticationRequestContext immediateContext() {
    return new AuthenticationRequestContext() {
      @Override
      public Uni<SecurityIdentity> runBlocking(Supplier<SecurityIdentity> function) {
        return Uni.createFrom().item(function.get());
      }
    };
  }
}
