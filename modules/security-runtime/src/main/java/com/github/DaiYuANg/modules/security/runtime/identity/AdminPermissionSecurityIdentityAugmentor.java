package com.github.DaiYuANg.modules.security.runtime.identity;

import com.github.DaiYuANg.cache.PermissionSnapshotStore;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import com.github.DaiYuANg.security.identity.QuarkusSecurityIdentityFactory;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotLoader;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotRefreshPolicy;
import com.google.common.base.Objects;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.apache.commons.lang3.StringUtils;

/**
 * Request-time permission enrichment.
 *
 * <p>This augmentor enforces a single permission pipeline for all principals:
 *
 * <ul>
 *   <li>Try to reuse Valkey snapshot if {@code authorityVersion} matches
 *   <li>Otherwise load from {@link PermissionSnapshotLoader} and persist snapshot back to Valkey
 * </ul>
 *
 * <p>JWT is treated as identity transport + version hint, not the ultimate source of truth for
 * permissions.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminPermissionSecurityIdentityAugmentor implements SecurityIdentityAugmentor {
  private final QuarkusSecurityIdentityFactory securityIdentityFactory;
  private final PermissionSnapshotStore permissionSnapshotStore;
  private final PermissionSnapshotLoader permissionSnapshotLoader;
  private final PermissionSnapshotRefreshPolicy permissionSnapshotRefreshPolicy;

  @Override
  public Uni<SecurityIdentity> augment(
      SecurityIdentity identity, AuthenticationRequestContext context) {
    if (identity == null || identity.isAnonymous()) {
      return Uni.createFrom().item(identity);
    }
    return context.runBlocking(() -> enrichBlocking(identity));
  }

  private SecurityIdentity enrichBlocking(@NonNull SecurityIdentity identity) {
    val principalName = identity.getPrincipal().getName();
    val expectedVersion = extractAuthorityVersion(identity);
    val userId = extractUserId(identity);
    val cached = cachedSnapshot(userId, principalName);
    if (isReusableSnapshot(cached, expectedVersion, principalName, userId)) {
      return securityIdentityFactory.create(cached.toAuthenticatedUser());
    }
    if (shouldRejectStaleIdentity(expectedVersion, userId) && userId == null) {
      return anonymousIdentity();
    }
    val loaded = permissionSnapshotLoader.load(userId, principalName).orElse(null);
    if (!isBoundToCurrentToken(loaded, principalName, userId)) {
      return shouldRejectStaleIdentity(expectedVersion, userId) ? anonymousIdentity() : identity;
    }
    permissionSnapshotStore.save(loaded);
    return securityIdentityFactory.create(loaded.toAuthenticatedUser());
  }

  private String extractAuthorityVersion(@NonNull SecurityIdentity identity) {
    val direct = identity.getAttribute(PrincipalAttributeKeys.AUTHORITY_VERSION);
    if (direct != null) {
      return String.valueOf(direct);
    }
    if (identity.getPrincipal() instanceof JsonWebToken jwt) {
      Object claim = jwt.getClaim(PrincipalAttributeKeys.AUTHORITY_VERSION);
      return claim == null ? null : String.valueOf(claim);
    }
    return null;
  }

  private PermissionSnapshot cachedSnapshot(Long userId, String principalName) {
    if (userId != null) {
      return permissionSnapshotStore.get(userId).orElse(null);
    }
    return permissionSnapshotStore.get(principalName).orElse(null);
  }

  private boolean isReusableSnapshot(
      PermissionSnapshot cached, String expectedVersion, String principalName, Long userId) {
    return permissionSnapshotRefreshPolicy.shouldReuse(expectedVersion, cached)
        && isBoundToCurrentToken(cached, principalName, userId);
  }

  private boolean isBoundToCurrentToken(
      PermissionSnapshot snapshot, String principalName, Long userId) {
    if (snapshot == null) {
      return false;
    }
    if (userId != null && !Objects.equal(userId, snapshot.userId())) {
      return false;
    }
    val normalizedPrincipalName = normalize(principalName);
    return normalizedPrincipalName == null || Objects.equal(normalizedPrincipalName, snapshot.username());
  }

  private boolean shouldRejectStaleIdentity(String expectedVersion, Long userId) {
    return userId != null || normalize(expectedVersion) != null;
  }

  private Long extractUserId(@NonNull SecurityIdentity identity) {
    val direct = identity.getAttribute(PrincipalAttributeKeys.USER_ID);
    if (direct != null) {
      return parseUserId(direct);
    }
    if (identity.getPrincipal() instanceof JsonWebToken jwt) {
      return parseUserId(jwt.getClaim(PrincipalAttributeKeys.USER_ID));
    }
    return null;
  }

  private Long parseUserId(Object rawValue) {
    if (rawValue == null) {
      return null;
    }
    if (rawValue instanceof Number number) {
      return number.longValue();
    }
    try {
      return Long.parseLong(String.valueOf(rawValue).trim());
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private SecurityIdentity anonymousIdentity() {
    return QuarkusSecurityIdentity.builder().setPrincipal(() -> "").setAnonymous(true).build();
  }

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
