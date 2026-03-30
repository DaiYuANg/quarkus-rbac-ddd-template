package com.github.DaiYuANg.cache;

import com.github.DaiYuANg.security.snapshot.PermissionSnapshot;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

/**
 * Facade for user permission snapshot storage. Delegates to UserAuthorityStore with hash-based
 * deduplication and reference counting.
 *
 * <p>Snapshots are keyed by a {@code userId} in Valkey. DB users use their row id; virtual
 * principals such as the configured super admin use a stable synthetic negative id so both follow
 * the same pipeline.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
public class PermissionSnapshotStore {

  private final UserAuthorityStore userAuthorityStore;

  public PermissionSnapshotStore(UserAuthorityStore userAuthorityStore) {
    this.userAuthorityStore = userAuthorityStore;
  }

  /**
   * Save snapshot to Valkey. Skips only when {@code userId} is null. DB users use row ids; config
   * users use stable synthetic negative ids.
   */
  public void save(PermissionSnapshot snapshot) {
    var userId = snapshot.userId();
    if (userId == null) {
      return;
    }
    userAuthorityStore.save(userId, snapshot);
  }

  /** Get snapshot by username. Resolves username -> userId then loads. */
  public Optional<PermissionSnapshot> get(String username) {
    return userAuthorityStore.resolveUserId(username).flatMap(userAuthorityStore::get);
  }

  /** Get snapshot directly by userId. */
  public Optional<PermissionSnapshot> get(Long userId) {
    if (userId == null) {
      return Optional.empty();
    }
    return userAuthorityStore.get(userId);
  }

  /** Delete snapshot by userId. */
  public void delete(Long userId) {
    userAuthorityStore.delete(userId);
  }
}
