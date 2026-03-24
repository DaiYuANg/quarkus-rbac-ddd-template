package com.github.DaiYuANg.security.snapshot;

import java.time.Duration;
import io.smallrye.mutiny.Uni;

public interface PermissionSnapshotRefreshPolicy {
    boolean shouldReuse(String expectedAuthorityVersion, PermissionSnapshot cachedSnapshot);
    default Uni<Boolean> shouldReuseAsync(String expectedAuthorityVersion, PermissionSnapshot cachedSnapshot) {
        return Uni.createFrom().item(shouldReuse(expectedAuthorityVersion, cachedSnapshot));
    }
    Duration snapshotTtl();
    default String source() {
        return "policy";
    }
}
