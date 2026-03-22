package com.github.DaiYuANg.security;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;

@ApplicationScoped
@DefaultBean
public class AdminPermissionSnapshotRefreshPolicy implements PermissionSnapshotRefreshPolicy {
    private static final Duration SNAPSHOT_TTL = Duration.ofHours(12);

    @Override
    public boolean shouldReuse(String expectedAuthorityVersion, PermissionSnapshot cachedSnapshot) {
        if (cachedSnapshot == null) {
            return false;
        }
        if (expectedAuthorityVersion == null || expectedAuthorityVersion.isBlank()) {
            return true;
        }
        return expectedAuthorityVersion.equals(cachedSnapshot.authorityVersion());
    }

    @Override
    public Duration snapshotTtl() {
        return SNAPSHOT_TTL;
    }

    @Override
    public String source() {
        return "redis-snapshot";
    }
}
