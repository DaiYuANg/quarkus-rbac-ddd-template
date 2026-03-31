package com.github.DaiYuANg.cache;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.val;

/**
 * Stores and retrieves the permission catalog (master list of sys_permission) in Redis.
 *
 * <p>Optimizations:
 *
 * <ul>
 *   <li>getAll: 1 round trip via JSON list
 *   <li>clearCatalog: uses Sets instead of KEYS (no blocking)
 *   <li>code/name index: ValueCommands (simple key→id)
 *   <li>findPage: Caffeine local cache (60s TTL); filtering/sorting in {@link
 *       PermissionCatalogSearchSupport}
 * </ul>
 */
@ApplicationScoped
public class PermissionCatalogStore {
  private final PermissionCatalogRedisStore redisStore;
  private final PermissionCatalogLocalCache localCache;

  public PermissionCatalogStore(
      @NonNull PermissionCatalogRedisStore redisStore,
      @NonNull PermissionCatalogLocalCache localCache) {
    this.redisStore = redisStore;
    this.localCache = localCache;
  }

  /** Replace entire catalog with the given permissions. Call at startup. */
  public void loadAll(List<PermissionCatalogEntry> permissions) {
    redisStore.replaceAll(permissions);
    localCache.invalidate();
  }

  public Optional<PermissionCatalogEntry> getById(Long id) {
    return redisStore.getById(id);
  }

  public Optional<PermissionCatalogEntry> getByCode(String code) {
    return redisStore.resolveIdByCode(code).flatMap(this::getById);
  }

  public Optional<PermissionCatalogEntry> getByName(String name) {
    return redisStore.resolveIdByName(name).flatMap(this::getById);
  }

  /**
   * Returns all permissions. Single Redis GET of JSON list. Results are cached locally (Caffeine)
   * for 60s.
   */
  public List<PermissionCatalogEntry> getAll() {
    val cached = localCache.getAll();
    if (cached.isPresent()) {
      return cached.get();
    }
    val list = redisStore.getAll();
    localCache.putAll(list);
    return list;
  }

  /** Filter, sort and paginate in memory. Matches PermissionRepository.page() semantics. */
  public PermissionCatalogPage findPage(@NonNull PermissionCatalogQuery query) {
    return PermissionCatalogSearchSupport.findPage(
        getAll(),
        query.keyword(),
        query.name(),
        query.code(),
        query.resource(),
        query.action(),
        query.groupCode(),
        query.sortBy(),
        query.sortDirection(),
        query.offset(),
        query.limit());
  }

  /** Returns true if catalog has any entries (for empty-catalog fallback check). */
  public boolean isEmpty() {
    return redisStore.isEmpty();
  }
}
