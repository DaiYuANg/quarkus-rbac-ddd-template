package com.github.DaiYuANg.cache.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/** Key prefix configuration for RBAC authority storage. */
@ConfigMapping(prefix = "app.cache.rbac")
public interface RBACCacheProperties {
  @WithName("authority-prefix")
  @WithDefault("rbac:authority")
  String authorityPrefix();

  @WithName("permission-catalog-prefix")
  @WithDefault("rbac:catalog:permission")
  String permissionCatalogPrefix();

  @WithName("authority-hash-ref-role-key")
  @WithDefault("roleHash")
  String authorityHashRefRoleKey();

  @WithName("authority-hash-ref-permission-key")
  @WithDefault("permissionHash")
  String authorityHashRefPermissionKey();

  default String roleKeyPrefix() {
    return authorityPrefix() + ":role";
  }

  default String permissionKeyPrefix() {
    return authorityPrefix() + ":permission";
  }

  default String authorityKeyPrefix() {
    return authorityPrefix();
  }

  default String roleHashKey(String hash) {
    return roleKeyPrefix() + ":" + hash;
  }

  default String permissionHashKey(String hash) {
    return permissionKeyPrefix() + ":" + hash;
  }

  default String authorityHashKey(Long userId) {
    return authorityKeyPrefix() + ":hash:" + userId;
  }

  default String authorityKey(Long userId) {
    return authorityKeyPrefix() + ":" + userId;
  }

  default String roleRefCountKey(String hash) {
    return roleKeyPrefix() + ":ref:" + hash;
  }

  default String permissionRefCountKey(String hash) {
    return permissionKeyPrefix() + ":ref:" + hash;
  }

  default String usernameToUserIdKey(String username) {
    return authorityKeyPrefix() + ":user:id:" + username;
  }

  default String permissionCatalogByIdKey(Long id) {
    return permissionCatalogPrefix() + ":id:" + id;
  }

  default String permissionCatalogByCodeKey(String code) {
    return permissionCatalogPrefix() + ":code:" + code;
  }

  default String permissionCatalogByNameKey(String name) {
    return permissionCatalogPrefix() + ":name:" + name;
  }

  default String permissionCatalogIdsKey() {
    return permissionCatalogPrefix() + ":ids";
  }

  /** Single JSON blob of all permissions for getAll (1 round trip). */
  default String permissionCatalogListKey() {
    return permissionCatalogPrefix() + ":list";
  }

  /** Set of code index keys for clearCatalog without KEYS. */
  default String permissionCatalogCodeKeysSetKey() {
    return permissionCatalogPrefix() + ":code_keys";
  }

  /** Set of name index keys for clearCatalog without KEYS. */
  default String permissionCatalogNameKeysSetKey() {
    return permissionCatalogPrefix() + ":name_keys";
  }
}
