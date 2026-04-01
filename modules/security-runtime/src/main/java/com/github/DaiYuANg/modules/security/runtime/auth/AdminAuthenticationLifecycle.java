package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.cache.PermissionSnapshotStore;
import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.modules.identity.application.port.AuthenticationLifecyclePort;
import com.github.DaiYuANg.security.config.SuperAdminAuthorityVersion;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.SecurityPrincipalFactory;
import com.github.DaiYuANg.security.identity.SecurityPrincipalKinds;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

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

  public String authorityVersion(@NonNull AuthenticatedUser user) {
    return SecurityPrincipalKinds.UserType.SUPER_ADMIN.equals(user.userType())
        ? SuperAdminAuthorityVersion.VALUE
        : authorityVersionStore.versionFor(user.username());
  }

  @Override
  public void publishSnapshot(@NonNull AuthenticatedUser user) {
    val authorityVersion = authorityVersion(user);
    val snapshot = securityPrincipalFactory.snapshot(user, authorityVersion);
    permissionSnapshotStore.save(snapshot);
  }

  @Override
  public void revokeRefreshToken(String refreshToken) {
    val normalizedRefreshToken = StringUtils.trimToNull(refreshToken);
    if (normalizedRefreshToken != null) {
      refreshTokenStore.delete(normalizedRefreshToken);
    }
  }
}
