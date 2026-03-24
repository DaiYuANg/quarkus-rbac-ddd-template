package com.github.DaiYuANg.security.snapshot;

import java.util.Optional;

public interface PermissionSnapshotLoader {
    Optional<PermissionSnapshot> load(String username);
}
