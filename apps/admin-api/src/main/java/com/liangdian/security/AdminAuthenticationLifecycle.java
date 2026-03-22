package com.liangdian.security;

import com.liangdian.redis.AuthorityVersionStore;
import com.liangdian.redis.PermissionSnapshotStore;
import com.liangdian.redis.RefreshTokenStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminAuthenticationLifecycle {
    private final AuthorityVersionStore authorityVersionStore;
    private final RefreshTokenStore refreshTokenStore;
    private final PermissionSnapshotStore permissionSnapshotStore;
    private final QuarkusSecurityIdentityFactory securityIdentityFactory;

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
            )
        );
        permissionSnapshotStore.save(snapshot, Duration.ofHours(12));
    }

    public void revokeRefreshToken(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenStore.delete(refreshToken);
        }
    }

    public io.quarkus.security.identity.SecurityIdentity toSecurityIdentity(AuthenticatedUser user) {
        return securityIdentityFactory.build(user);
    }
}
