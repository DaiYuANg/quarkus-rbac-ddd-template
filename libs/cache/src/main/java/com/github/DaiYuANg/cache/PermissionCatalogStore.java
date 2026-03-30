package com.github.DaiYuANg.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.DaiYuANg.cache.config.RBACCacheProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

  private static final TypeReference<List<PermissionCatalogEntry>> LIST_TYPE =
      new TypeReference<>() {};

  private final HashCommands<String, String, String> hashCommands;
  private final SetCommands<String, String> setCommands;
  private final ValueCommands<String, String> valueCommands;
  private final KeyCommands<String> keyCommands;
  private final RBACCacheProperties props;
  private final ObjectMapper objectMapper;
  private final Cache<String, List<PermissionCatalogEntry>> localCache;

  public PermissionCatalogStore(
      RedisDataSource ds, RBACCacheProperties props, ObjectMapper objectMapper) {
    this.hashCommands = ds.hash(String.class);
    this.setCommands = ds.set(String.class);
    this.valueCommands = ds.value(String.class);
    this.keyCommands = ds.key();
    this.props = props;
    this.objectMapper = objectMapper;
    this.localCache =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(60)).maximumSize(1).build();
  }

  /** Replace entire catalog with the given permissions. Call at startup. */
  public void loadAll(List<PermissionCatalogEntry> permissions) {
    clearCatalog();
    if (permissions == null || permissions.isEmpty()) {
      invalidateLocalCache();
      return;
    }
    val idsKey = props.permissionCatalogIdsKey();
    val listKey = props.permissionCatalogListKey();
    val codeKeysSet = props.permissionCatalogCodeKeysSetKey();
    val nameKeysSet = props.permissionCatalogNameKeysSetKey();

    val codeKeys = new ArrayList<String>();
    val nameKeys = new ArrayList<String>();
    val idStrs = new ArrayList<String>();

    permissions.forEach(
        p -> {
          val key = props.permissionCatalogByIdKey(p.id());
          hashCommands.hset(key, PermissionCatalogEntryMapper.toHashFields(p));
          idStrs.add(String.valueOf(p.id()));
          if (p.code() != null && !p.code().isBlank()) {
            val codeKey = props.permissionCatalogByCodeKey(p.code());
            valueCommands.set(codeKey, String.valueOf(p.id()));
            codeKeys.add(codeKey);
          }
          if (p.name() != null && !p.name().isBlank()) {
            val nameKey = props.permissionCatalogByNameKey(p.name());
            valueCommands.set(nameKey, String.valueOf(p.id()));
            nameKeys.add(nameKey);
          }
        });
    if (!idStrs.isEmpty()) {
      setCommands.sadd(idsKey, idStrs.toArray(new String[0]));
    }
    if (!codeKeys.isEmpty()) {
      setCommands.sadd(codeKeysSet, codeKeys.toArray(new String[0]));
    }
    if (!nameKeys.isEmpty()) {
      setCommands.sadd(nameKeysSet, nameKeys.toArray(new String[0]));
    }
    try {
      val json = objectMapper.writeValueAsString(permissions);
      valueCommands.set(listKey, json);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize permission catalog", e);
    }
    invalidateLocalCache();
  }

  private void invalidateLocalCache() {
    localCache.invalidate(cacheKey());
  }

  private static String cacheKey() {
    return "permission:catalog:all";
  }

  private void clearCatalog() {
    var ids = setCommands.smembers(props.permissionCatalogIdsKey());
    if (ids == null) {
      ids = Set.of();
    }
    ids.stream()
        .map(Long::parseLong)
        .map(props::permissionCatalogByIdKey)
        .forEach(keyCommands::del);
    keyCommands.del(props.permissionCatalogIdsKey());
    keyCommands.del(props.permissionCatalogListKey());

    val codeKeys = setCommands.smembers(props.permissionCatalogCodeKeysSetKey());
    if (codeKeys != null && !codeKeys.isEmpty()) {
      keyCommands.del(codeKeys.toArray(new String[0]));
      keyCommands.del(props.permissionCatalogCodeKeysSetKey());
    }
    val nameKeys = setCommands.smembers(props.permissionCatalogNameKeysSetKey());
    if (nameKeys != null && !nameKeys.isEmpty()) {
      keyCommands.del(nameKeys.toArray(new String[0]));
      keyCommands.del(props.permissionCatalogNameKeysSetKey());
    }
  }

  public Optional<PermissionCatalogEntry> getById(Long id) {
    val key = props.permissionCatalogByIdKey(id);
    if (!keyCommands.exists(key)) {
      return Optional.empty();
    }
    val map = hashCommands.hgetall(key);
    return Optional.of(PermissionCatalogEntryMapper.fromMap(id, map));
  }

  public Optional<PermissionCatalogEntry> getByCode(String code) {
    if (code == null || code.isBlank()) {
      return Optional.empty();
    }
    val idStr = valueCommands.get(props.permissionCatalogByCodeKey(code));
    if (idStr == null || idStr.isBlank()) {
      return Optional.empty();
    }
    return getById(Long.parseLong(idStr.trim()));
  }

  public Optional<PermissionCatalogEntry> getByName(String name) {
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }
    val idStr = valueCommands.get(props.permissionCatalogByNameKey(name));
    if (idStr == null || idStr.isBlank()) {
      return Optional.empty();
    }
    return getById(Long.parseLong(idStr.trim()));
  }

  /**
   * Returns all permissions. Single Redis GET of JSON list. Results are cached locally (Caffeine)
   * for 60s.
   */
  public List<PermissionCatalogEntry> getAll() {
    val cached = localCache.getIfPresent(cacheKey());
    if (cached != null) {
      return cached;
    }
    val list = getAllFromRedis();
    localCache.put(cacheKey(), list);
    return list;
  }

  private List<PermissionCatalogEntry> getAllFromRedis() {
    val json = valueCommands.get(props.permissionCatalogListKey());
    if (json == null || json.isBlank()) {
      return List.of();
    }
    try {
      return objectMapper.readValue(json, LIST_TYPE);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to deserialize permission catalog", e);
    }
  }

  /** Filter, sort and paginate in memory. Matches PermissionRepository.page() semantics. */
  public PermissionCatalogPage findPage(
      String keyword,
      String name,
      String code,
      String resource,
      String action,
      String groupCode,
      String sortBy,
      String sortDirection,
      int offset,
      int limit) {
    return PermissionCatalogSearchSupport.findPage(
        getAll(),
        keyword,
        name,
        code,
        resource,
        action,
        groupCode,
        sortBy,
        sortDirection,
        offset,
        limit);
  }

  /** Returns true if catalog has any entries (for empty-catalog fallback check). */
  public boolean isEmpty() {
    return setCommands.smembers(props.permissionCatalogIdsKey()).isEmpty();
  }
}
