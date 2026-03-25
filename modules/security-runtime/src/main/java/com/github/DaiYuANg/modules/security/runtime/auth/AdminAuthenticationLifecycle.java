package com.github.DaiYuANg.modules.security.runtime.auth;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.cache.PermissionSnapshotStore;
import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.modules.identity.application.port.AuthenticationLifecyclePort;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.identity.PrincipalAttributeKeys;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminAuthenticationLifecycle implements AuthenticationLifecyclePort {
  private final AuthorityVersionStore authorityVersionStore;
  private final RefreshTokenStore refreshTokenStore;
  private final PermissionSnapshotStore permissionSnapshotStore;

  public String authorityVersion(String username) {
    return authorityVersionStore.versionFor(username);
  }

  @Override
  public void publishSnapshot(AuthenticatedUser user) {
    var snapshot =
        new PermissionSnapshot(
            user.username(),
            user.displayName(),
            user.userType(),
            user.roles(),
            user.permissions(),
            authorityVersion(user.username()),
            java.util.Map.of(
                PrincipalAttributeKeys.DISPLAY_NAME,
                user.displayName(),
                PrincipalAttributeKeys.USER_TYPE,
                user.userType(),
                PrincipalAttributeKeys.ROLES,
                user.roles(),
                PrincipalAttributeKeys.PERMISSIONS,
                user.permissions(),
                PrincipalAttributeKeys.AUTHORITY_VERSION,
                authorityVersion(user.username())),
            user.userId());
    permissionSnapshotStore.save(snapshot);
  }

  @Override
  public void revokeRefreshToken(String refreshToken) {
    if (refreshToken != null && !refreshToken.isBlank()) {
      refreshTokenStore.delete(refreshToken);
    }
  }
}
