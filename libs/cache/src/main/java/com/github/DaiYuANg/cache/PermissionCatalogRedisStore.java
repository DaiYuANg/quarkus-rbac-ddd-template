package com.github.DaiYuANg.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.DaiYuANg.cache.config.RBACCacheProperties;
import com.google.common.base.MoreObjects;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.set.SetCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class PermissionCatalogRedisStore {
  private static final TypeReference<List<PermissionCatalogEntry>> LIST_TYPE =
      new TypeReference<>() {};

  private final HashCommands<String, String, String> hashCommands;
  private final SetCommands<String, String> setCommands;
  private final ValueCommands<String, String> valueCommands;
  private final KeyCommands<String> keyCommands;
  private final RBACCacheProperties props;
  private final ObjectMapper objectMapper;

  public PermissionCatalogRedisStore(
      @NonNull RedisDataSource ds,
      @NonNull RBACCacheProperties props,
      @NonNull ObjectMapper objectMapper) {
    this.hashCommands = ds.hash(String.class);
    this.setCommands = ds.set(String.class);
    this.valueCommands = ds.value(String.class);
    this.keyCommands = ds.key();
    this.props = props;
    this.objectMapper = objectMapper;
  }

  public void replaceAll(List<PermissionCatalogEntry> permissions) {
    clearCatalog();
    if (permissions == null || permissions.isEmpty()) {
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
        permission -> {
          val key = props.permissionCatalogByIdKey(permission.id());
          hashCommands.hset(key, PermissionCatalogEntryMapper.toHashFields(permission));
          idStrs.add(String.valueOf(permission.id()));
          val code = normalize(permission.code());
          if (code != null) {
            val codeKey = props.permissionCatalogByCodeKey(code);
            valueCommands.set(codeKey, String.valueOf(permission.id()));
            codeKeys.add(codeKey);
          }
          val name = normalize(permission.name());
          if (name != null) {
            val nameKey = props.permissionCatalogByNameKey(name);
            valueCommands.set(nameKey, String.valueOf(permission.id()));
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
      valueCommands.set(listKey, objectMapper.writeValueAsString(permissions));
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize permission catalog", e);
    }
  }

  public Optional<PermissionCatalogEntry> getById(Long id) {
    val key = props.permissionCatalogByIdKey(id);
    if (!keyCommands.exists(key)) {
      return Optional.empty();
    }
    return Optional.of(PermissionCatalogEntryMapper.fromMap(id, hashCommands.hgetall(key)));
  }

  public Optional<Long> resolveIdByCode(String code) {
    return resolveId(props.permissionCatalogByCodeKey(normalize(code)));
  }

  public Optional<Long> resolveIdByName(String name) {
    return resolveId(props.permissionCatalogByNameKey(normalize(name)));
  }

  public List<PermissionCatalogEntry> getAll() {
    val json = normalize(valueCommands.get(props.permissionCatalogListKey()));
    if (json == null) {
      return List.of();
    }
    try {
      return objectMapper.readValue(json, LIST_TYPE);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to deserialize permission catalog", e);
    }
  }

  public boolean isEmpty() {
    return setCommands.smembers(props.permissionCatalogIdsKey()).isEmpty();
  }

  private void clearCatalog() {
    val ids =
        MoreObjects.firstNonNull(
            setCommands.smembers(props.permissionCatalogIdsKey()), Set.<String>of());
    ids.stream()
        .map(Long::parseLong)
        .map(props::permissionCatalogByIdKey)
        .forEach(keyCommands::del);
    keyCommands.del(props.permissionCatalogIdsKey());
    keyCommands.del(props.permissionCatalogListKey());
    clearIndexKeys(props.permissionCatalogCodeKeysSetKey());
    clearIndexKeys(props.permissionCatalogNameKeysSetKey());
  }

  private void clearIndexKeys(@NonNull String keysSetKey) {
    val keys = setCommands.smembers(keysSetKey);
    if (keys == null || keys.isEmpty()) {
      return;
    }
    keyCommands.del(keys.toArray(new String[0]));
    keyCommands.del(keysSetKey);
  }

  private Optional<Long> resolveId(String key) {
    val normalizedKey = normalize(key);
    if (normalizedKey == null) {
      return Optional.empty();
    }
    val idStr = normalize(valueCommands.get(normalizedKey));
    if (idStr == null) {
      return Optional.empty();
    }
    return Optional.of(Long.parseLong(idStr));
  }

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
