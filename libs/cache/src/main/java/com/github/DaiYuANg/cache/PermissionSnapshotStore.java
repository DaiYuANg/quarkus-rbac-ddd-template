package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import io.smallrye.mutiny.Uni;
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

    public Uni<Optional<PermissionSnapshot>> getAsync(String username) {
        return userAuthorityStore.resolveUserIdAsync(username)
            .chain(userIdOpt -> userIdOpt
                .map(userAuthorityStore::getAsync)
                .orElseGet(() -> Uni.createFrom().item(Optional.empty())));
    }

    /**
     * Delete snapshot by userId.
     */
    public void delete(Long userId) {
        userAuthorityStore.delete(userId);
    }

    public Uni<Void> saveAsync(PermissionSnapshot snapshot) {
        var userId = snapshot.userId();
        if (userId == null) {
            return Uni.createFrom().voidItem();
        }
        return userAuthorityStore.saveAsync(userId, snapshot);
    }

    public Uni<Void> deleteAsync(Long userId) {
        return userAuthorityStore.deleteAsync(userId);
    }
}
