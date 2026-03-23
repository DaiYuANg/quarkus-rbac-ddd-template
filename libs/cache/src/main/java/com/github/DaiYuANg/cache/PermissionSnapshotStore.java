package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.security.PermissionSnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

/**
 * Facade for user permission snapshot storage.
 * Delegates to UserAuthorityStore with hash-based deduplication and reference counting.
 */
@ApplicationScoped
public class PermissionSnapshotStore {

    private final UserAuthorityStore userAuthorityStore;

    public PermissionSnapshotStore(UserAuthorityStore userAuthorityStore) {
        this.userAuthorityStore = userAuthorityStore;
    }

    /**
     * Save snapshot. Only persists when userId is non-null (DB users). Config users are skipped.
     */
    public void save(PermissionSnapshot snapshot) {
        var userId = snapshot.userId();
        if (userId == null) {
            return; // Config user, no persistence
        }
        userAuthorityStore.save(userId, snapshot);
    }

    /**
     * Get snapshot by username. Resolves username -> userId then loads.
     */
    public Optional<PermissionSnapshot> get(String username) {
        return userAuthorityStore.resolveUserId(username).flatMap(userAuthorityStore::get);
    }

    /**
     * Delete snapshot by userId.
     */
    public void delete(Long userId) {
        userAuthorityStore.delete(userId);
    }
}
