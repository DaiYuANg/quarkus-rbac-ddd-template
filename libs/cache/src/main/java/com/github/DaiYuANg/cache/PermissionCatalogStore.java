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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Stores and retrieves the permission catalog (master list of sys_permission) in Redis.
 * Loaded at startup; all runtime permission reads go through this store.
 *
 * <p>Optimizations:
 * <ul>
 *   <li>getAll: 1 round trip via JSON list</li>
 *   <li>clearCatalog: uses Sets instead of KEYS (no blocking)</li>
 *   <li>code/name index: ValueCommands (simple key→id)</li>
 *   <li>findPage: Caffeine local cache (60s TTL)</li>
 * </ul>
 */
@ApplicationScoped
public class PermissionCatalogStore {

    private static final String HASH_ID = "id";
    private static final String HASH_NAME = "name";
    private static final String HASH_CODE = "code";
    private static final String HASH_RESOURCE = "resource";
    private static final String HASH_ACTION = "action";
    private static final String HASH_GROUP_CODE = "groupCode";
    private static final String HASH_DESCRIPTION = "description";
    private static final String HASH_EXPRESSION = "expression";
    private static final String HASH_CREATE_AT = "createAt";
    private static final String HASH_UPDATE_AT = "updateAt";

    private static final TypeReference<List<PermissionCatalogEntry>> LIST_TYPE = new TypeReference<>() {};

    private final HashCommands<String, String, String> hashCommands;
    private final SetCommands<String, String> setCommands;
    private final ValueCommands<String, String> valueCommands;
    private final KeyCommands<String> keyCommands;
    private final RBACCacheProperties props;
    private final ObjectMapper objectMapper;
    private final Cache<String, List<PermissionCatalogEntry>> localCache;

    public PermissionCatalogStore(
            RedisDataSource ds,
            RBACCacheProperties props,
            ObjectMapper objectMapper) {
        this.hashCommands = ds.hash(String.class);
        this.setCommands = ds.set(String.class);
        this.valueCommands = ds.value(String.class);
        this.keyCommands = ds.key();
        this.props = props;
        this.objectMapper = objectMapper;
        this.localCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(60))
            .maximumSize(1)
            .build();
    }

    /**
     * Replace entire catalog with the given permissions. Call at startup.
     */
    public void loadAll(List<PermissionCatalogEntry> permissions) {
        clearCatalog();
        if (permissions == null || permissions.isEmpty()) {
            invalidateLocalCache();
            return;
        }
        var idsKey = props.permissionCatalogIdsKey();
        var listKey = props.permissionCatalogListKey();
        var codeKeysSet = props.permissionCatalogCodeKeysSetKey();
        var nameKeysSet = props.permissionCatalogNameKeysSetKey();

        var codeKeys = new ArrayList<String>();
        var nameKeys = new ArrayList<String>();
        var idStrs = new ArrayList<String>();

        for (var p : permissions) {
            var key = props.permissionCatalogByIdKey(p.id());
            hashCommands.hset(key, Map.of(
                HASH_ID, String.valueOf(p.id()),
                HASH_NAME, PermissionCatalogEntry.empty(p.name()),
                HASH_CODE, PermissionCatalogEntry.empty(p.code()),
                HASH_RESOURCE, PermissionCatalogEntry.empty(p.resource()),
                HASH_ACTION, PermissionCatalogEntry.empty(p.action()),
                HASH_GROUP_CODE, PermissionCatalogEntry.empty(p.groupCode()),
                HASH_DESCRIPTION, PermissionCatalogEntry.empty(p.description()),
                HASH_EXPRESSION, PermissionCatalogEntry.empty(p.expression()),
                HASH_CREATE_AT, p.createAt() != null ? String.valueOf(p.createAt().toEpochMilli()) : "0",
                HASH_UPDATE_AT, p.updateAt() != null ? String.valueOf(p.updateAt().toEpochMilli()) : "0"
            ));
            idStrs.add(String.valueOf(p.id()));
            if (p.code() != null && !p.code().isBlank()) {
                var codeKey = props.permissionCatalogByCodeKey(p.code());
                valueCommands.set(codeKey, String.valueOf(p.id()));
                codeKeys.add(codeKey);
            }
            if (p.name() != null && !p.name().isBlank()) {
                var nameKey = props.permissionCatalogByNameKey(p.name());
                valueCommands.set(nameKey, String.valueOf(p.id()));
                nameKeys.add(nameKey);
            }
        }
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
            var json = objectMapper.writeValueAsString(permissions);
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
        if (ids == null) ids = Set.of();
        for (var idStr : ids) {
            keyCommands.del(props.permissionCatalogByIdKey(Long.parseLong(idStr)));
        }
        keyCommands.del(props.permissionCatalogIdsKey());
        keyCommands.del(props.permissionCatalogListKey());

        var codeKeys = setCommands.smembers(props.permissionCatalogCodeKeysSetKey());
        if (codeKeys != null && !codeKeys.isEmpty()) {
            keyCommands.del(codeKeys.toArray(new String[0]));
            keyCommands.del(props.permissionCatalogCodeKeysSetKey());
        }
        var nameKeys = setCommands.smembers(props.permissionCatalogNameKeysSetKey());
        if (nameKeys != null && !nameKeys.isEmpty()) {
            keyCommands.del(nameKeys.toArray(new String[0]));
            keyCommands.del(props.permissionCatalogNameKeysSetKey());
        }
    }

    public Optional<PermissionCatalogEntry> getById(Long id) {
        var key = props.permissionCatalogByIdKey(id);
        if (!keyCommands.exists(key)) {
            return Optional.empty();
        }
        var map = hashCommands.hgetall(key);
        return Optional.of(fromMap(id, map));
    }

    public Optional<PermissionCatalogEntry> getByCode(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        var idStr = valueCommands.get(props.permissionCatalogByCodeKey(code));
        if (idStr == null || idStr.isBlank()) return Optional.empty();
        return getById(Long.parseLong(idStr.trim()));
    }

    public Optional<PermissionCatalogEntry> getByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        var idStr = valueCommands.get(props.permissionCatalogByNameKey(name));
        if (idStr == null || idStr.isBlank()) return Optional.empty();
        return getById(Long.parseLong(idStr.trim()));
    }

    /**
     * Returns all permissions. Single Redis GET of JSON list.
     * Results are cached locally (Caffeine) for 60s.
     */
    public List<PermissionCatalogEntry> getAll() {
        var cached = localCache.getIfPresent(cacheKey());
        if (cached != null) {
            return cached;
        }
        var list = getAllFromRedis();
        localCache.put(cacheKey(), list);
        return list;
    }

    private List<PermissionCatalogEntry> getAllFromRedis() {
        var json = valueCommands.get(props.permissionCatalogListKey());
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize permission catalog", e);
        }
    }

    /**
     * Filter, sort and paginate in memory. Matches PermissionRepository.page() semantics.
     */
    public CatalogPage findPage(String keyword, String name, String code, String resource,
            String action, String groupCode, String sortBy, String sortDirection, int offset, int limit) {
        var all = getAll();
        var filtered = all.stream()
            .filter(p -> matchesKeyword(p, keyword))
            .filter(p -> matchesLike(p.name(), name))
            .filter(p -> matchesLike(p.code(), code))
            .filter(p -> matchesEq(p.resource(), resource))
            .filter(p -> matchesLike(p.action(), action))
            .filter(p -> matchesEq(p.groupCode(), groupCode))
            .toList();
        var total = filtered.size();
        var cmp = sortComparator(sortBy, sortDirection);
        var sorted = filtered.stream().sorted(cmp).toList();
        var content = sorted.stream().skip(offset).limit(Math.max(1, limit)).toList();
        return new CatalogPage(content, total);
    }

    private static boolean matchesKeyword(PermissionCatalogEntry p, String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        var k = keyword.toLowerCase();
        return containsIgnoreCase(p.name(), k) || containsIgnoreCase(p.code(), k)
            || containsIgnoreCase(p.resource(), k) || containsIgnoreCase(p.action(), k)
            || containsIgnoreCase(p.groupCode(), k) || containsIgnoreCase(p.description(), k);
    }

    private static boolean matchesLike(String field, String pattern) {
        if (pattern == null || pattern.isBlank()) return true;
        return containsIgnoreCase(field, pattern.toLowerCase());
    }

    private static boolean matchesEq(String field, String value) {
        if (value == null || value.isBlank()) return true;
        return value.equalsIgnoreCase(field != null ? field : "");
    }

    private static boolean containsIgnoreCase(String s, String sub) {
        if (s == null) return false;
        return s.toLowerCase().contains(sub);
    }

    private static Comparator<PermissionCatalogEntry> sortComparator(String sortBy, String sortDir) {
        var asc = sortDir == null || !"desc".equalsIgnoreCase(sortDir.trim());
        Comparator<PermissionCatalogEntry> cmp = switch (sortBy != null ? sortBy.trim().toLowerCase() : "id") {
            case "name" -> Comparator.comparing(p -> (p.name() != null ? p.name() : ""), String.CASE_INSENSITIVE_ORDER);
            case "code" -> Comparator.comparing(p -> (p.code() != null ? p.code() : ""), String.CASE_INSENSITIVE_ORDER);
            case "resource" -> Comparator.comparing(p -> (p.resource() != null ? p.resource() : ""), String.CASE_INSENSITIVE_ORDER);
            case "action" -> Comparator.comparing(p -> (p.action() != null ? p.action() : ""), String.CASE_INSENSITIVE_ORDER);
            case "groupcode" -> Comparator.comparing(p -> (p.groupCode() != null ? p.groupCode() : ""), String.CASE_INSENSITIVE_ORDER);
            case "createtime" -> Comparator.comparing(p -> p.createAt() != null ? p.createAt() : Instant.EPOCH);
            case "updatetime" -> Comparator.comparing(p -> p.updateAt() != null ? p.updateAt() : Instant.EPOCH);
            default -> Comparator.comparing(PermissionCatalogEntry::id);
        };
        return asc ? cmp : cmp.reversed();
    }

    public record CatalogPage(List<PermissionCatalogEntry> content, long total) {}

    private static PermissionCatalogEntry fromMap(Long id, Map<String, String> map) {
        var createAt = parseInstant(map.get(HASH_CREATE_AT));
        var updateAt = parseInstant(map.get(HASH_UPDATE_AT));
        return new PermissionCatalogEntry(
            id,
            map.getOrDefault(HASH_NAME, ""),
            map.getOrDefault(HASH_CODE, ""),
            map.getOrDefault(HASH_RESOURCE, ""),
            map.getOrDefault(HASH_ACTION, ""),
            nullOrEmpty(map.get(HASH_GROUP_CODE)),
            nullOrEmpty(map.get(HASH_DESCRIPTION)),
            nullOrEmpty(map.get(HASH_EXPRESSION)),
            createAt,
            updateAt
        );
    }

    private static Instant parseInstant(String s) {
        if (s == null || s.isBlank()) return Instant.EPOCH;
        try {
            return Instant.ofEpochMilli(Long.parseLong(s.trim()));
        } catch (NumberFormatException e) {
            return Instant.EPOCH;
        }
    }

    private static String nullOrEmpty(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    /** Returns true if catalog has any entries (for empty-catalog fallback check). */
    public boolean isEmpty() {
        return setCommands.smembers(props.permissionCatalogIdsKey()).isEmpty();
    }
}
