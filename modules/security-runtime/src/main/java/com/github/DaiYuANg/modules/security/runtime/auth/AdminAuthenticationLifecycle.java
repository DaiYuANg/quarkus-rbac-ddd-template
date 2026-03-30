package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.cache.PermissionSnapshotStore;
import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.modules.identity.application.port.AuthenticationLifecyclePort;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.SecurityPrincipalFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Authentication lifecycle hooks for admin flows.
 *
 * <p>On successful login/refresh we publish a permission snapshot to Valkey so request-time
 * augmentation can reuse it across requests until authority version invalidation.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminAuthenticationLifecycle implements AuthenticationLifecyclePort {
  private final AuthorityVersionStore authorityVersionStore;
  private final RefreshTokenStore refreshTokenStore;
  private final PermissionSnapshotStore permissionSnapshotStore;
  private final SecurityPrincipalFactory securityPrincipalFactory;

  public String authorityVersion(String username) {
    return authorityVersionStore.versionFor(username);
  }

  @Override
  public void publishSnapshot(@NonNull AuthenticatedUser user) {
    val authorityVersion = authorityVersion(user.username());
    val snapshot = securityPrincipalFactory.snapshot(user, authorityVersion);
    permissionSnapshotStore.save(snapshot);
  }

  @Override
  public void revokeRefreshToken(String refreshToken) {
    if (refreshToken != null && !refreshToken.isBlank()) {
      refreshTokenStore.delete(refreshToken);
    }
  }
}
