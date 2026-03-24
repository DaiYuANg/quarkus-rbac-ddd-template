package com.github.DaiYuANg.modules.security.runtime.identity;

import com.github.DaiYuANg.cache.PermissionSnapshotStore;
import com.github.DaiYuANg.security.identity.QuarkusSecurityIdentityFactory;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotLoader;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotRefreshPolicy;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AdminPermissionSecurityIdentityAugmentor implements SecurityIdentityAugmentor {
    private final QuarkusSecurityIdentityFactory securityIdentityFactory;
    private final PermissionSnapshotStore permissionSnapshotStore;
    private final PermissionSnapshotLoader permissionSnapshotLoader;
    private final PermissionSnapshotRefreshPolicy permissionSnapshotRefreshPolicy;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (identity == null || identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }
        return context.runBlocking(() -> enrichBlocking(identity));
    }

    private SecurityIdentity enrichBlocking(SecurityIdentity identity) {
        var principalName = identity.getPrincipal().getName();
        var expectedVersion = extractAuthorityVersion(identity);
        var cached = permissionSnapshotStore.get(principalName).orElse(null);
        if (permissionSnapshotRefreshPolicy.shouldReuse(expectedVersion, cached)) {
            return securityIdentityFactory.create(cached.toAuthenticatedUser());
        }
        var loaded = permissionSnapshotLoader.load(principalName).orElse(null);
        if (loaded == null) {
            return identity;
        }
        permissionSnapshotStore.save(loaded);
        return securityIdentityFactory.create(loaded.toAuthenticatedUser());
    }

    private String extractAuthorityVersion(SecurityIdentity identity) {
        Object direct = identity.getAttribute("authorityVersion");
        if (direct != null) {
            return String.valueOf(direct);
        }
        if (identity.getPrincipal() instanceof JsonWebToken jwt) {
            Object claim = jwt.getClaim("authorityVersion");
            return claim == null ? null : String.valueOf(claim);
        }
        return null;
    }
}
