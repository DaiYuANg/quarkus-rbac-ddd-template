package com.github.DaiYuANg.security.auth;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.cache.PermissionSnapshotStore;
import com.github.DaiYuANg.cache.RefreshTokenStore;
import com.github.DaiYuANg.security.identity.AuthenticatedUser;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminAuthenticationLifecycle {
    private final AuthorityVersionStore authorityVersionStore;
    private final RefreshTokenStore refreshTokenStore;
    private final PermissionSnapshotStore permissionSnapshotStore;

    public String authorityVersion(String username) {
        return authorityVersionStore.versionFor(username);
    }

    public void publishSnapshot(AuthenticatedUser user) {
        var snapshot = new PermissionSnapshot(
            user.username(),
            user.displayName(),
            user.userType(),
            user.roles(),
            user.permissions(),
            authorityVersion(user.username()),
            java.util.Map.of(
                "displayName", user.displayName(),
                "userType", user.userType(),
                "roles", user.roles(),
                "permissions", user.permissions(),
                "authorityVersion", authorityVersion(user.username())
            ),
            user.userId()
        );
        permissionSnapshotStore.save(snapshot);
    }

    public void revokeRefreshToken(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenStore.delete(refreshToken);
        }
    }
}
