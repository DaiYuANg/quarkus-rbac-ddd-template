package com.github.DaiYuANg.security.identity;

import com.github.DaiYuANg.cache.AuthorityVersionStore;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import com.github.DaiYuANg.security.snapshot.PermissionSnapshotRefreshPolicy;
import io.quarkus.arc.DefaultBean;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;

@ApplicationScoped
@DefaultBean
public class AdminPermissionSnapshotRefreshPolicy implements PermissionSnapshotRefreshPolicy {
    private static final Duration SNAPSHOT_TTL = Duration.ofHours(12);

    private final AuthorityVersionStore authorityVersionStore;

    @Inject
    public AdminPermissionSnapshotRefreshPolicy(AuthorityVersionStore authorityVersionStore) {
        this.authorityVersionStore = authorityVersionStore;
    }

    @Override
    public boolean shouldReuse(String expectedAuthorityVersion, PermissionSnapshot cachedSnapshot) {
        if (cachedSnapshot == null) {
            return false;
        }
        // Reuse only if JWT version matches cached version AND cache is not stale (global bump)
        var cachedVersion = cachedSnapshot.authorityVersion();
        if (cachedVersion == null || cachedVersion.isBlank()) {
            return false;
        }
        if (expectedAuthorityVersion == null || expectedAuthorityVersion.isBlank()) {
            return true;
        }
        if (!expectedAuthorityVersion.equals(cachedVersion)) {
            return false;
        }
        // Cached snapshot must match current global version (invalidated when admin bumps)
        return cachedVersion.equals(authorityVersionStore.currentVersion());
    }

    @Override
    public Uni<Boolean> shouldReuseAsync(String expectedAuthorityVersion, PermissionSnapshot cachedSnapshot) {
        if (cachedSnapshot == null) {
            return Uni.createFrom().item(false);
        }
        var cachedVersion = cachedSnapshot.authorityVersion();
        if (cachedVersion == null || cachedVersion.isBlank()) {
            return Uni.createFrom().item(false);
        }
        if (expectedAuthorityVersion == null || expectedAuthorityVersion.isBlank()) {
            return Uni.createFrom().item(true);
        }
        if (!expectedAuthorityVersion.equals(cachedVersion)) {
            return Uni.createFrom().item(false);
        }
        return authorityVersionStore.currentVersionAsync()
            .onItem().transform(cachedVersion::equals);
    }

    @Override
    public Duration snapshotTtl() {
        return SNAPSHOT_TTL;
    }

    @Override
    public String source() {
        return "valkey-snapshot";
    }
}
