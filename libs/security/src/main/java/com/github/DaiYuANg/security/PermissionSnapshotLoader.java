package com.github.DaiYuANg.security;

import java.util.Optional;

public interface PermissionSnapshotLoader {
    Optional<PermissionSnapshot> load(String username);
}
