package com.github.DaiYuANg.security.snapshot;

import java.time.Duration;

public interface PermissionSnapshotRefreshPolicy {
    boolean shouldReuse(String expectedAuthorityVersion, PermissionSnapshot cachedSnapshot);
    Duration snapshotTtl();
    default String source() {
        return "policy";
    }
}
