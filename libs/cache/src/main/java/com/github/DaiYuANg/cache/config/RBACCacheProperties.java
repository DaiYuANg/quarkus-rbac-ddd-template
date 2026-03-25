package com.github.DaiYuANg.cache.config;

import jakarta.enterprise.context.ApplicationScoped;

/** Key prefix configuration for RBAC authority storage. */
@ApplicationScoped
public class RBACCacheProperties {

  private static final String PREFIX = "rbac:authority";

  public String roleKeyPrefix() {
    return PREFIX + ":role";
  }

  public String permissionKeyPrefix() {
    return PREFIX + ":permission";
  }

  public String authorityKeyPrefix() {
    return PREFIX;
  }

  public String authorityHashRefRoleKey() {
    return "roleHash";
  }

  public String authorityHashRefPermissionKey() {
    return "permissionHash";
  }

  public String roleHashKey(String hash) {
    return roleKeyPrefix() + ":" + hash;
  }

  public String permissionHashKey(String hash) {
    return permissionKeyPrefix() + ":" + hash;
  }

  public String authorityHashKey(Long userId) {
    return authorityKeyPrefix() + ":hash:" + userId;
  }

  public String authorityKey(Long userId) {
    return authorityKeyPrefix() + ":" + userId;
  }

  public String roleRefCountKey(String hash) {
    return roleKeyPrefix() + ":ref:" + hash;
  }

  public String permissionRefCountKey(String hash) {
    return permissionKeyPrefix() + ":ref:" + hash;
  }

  public String usernameToUserIdKey(String username) {
    return authorityKeyPrefix() + ":user:id:" + username;
  }

  // Permission catalog (master list of all permissions loaded at startup)
  private static final String CATALOG_PREFIX = "rbac:catalog:permission";

  public String permissionCatalogByIdKey(Long id) {
    return CATALOG_PREFIX + ":id:" + id;
  }

  public String permissionCatalogByCodeKey(String code) {
    return CATALOG_PREFIX + ":code:" + code;
  }

  public String permissionCatalogByNameKey(String name) {
    return CATALOG_PREFIX + ":name:" + name;
  }

  public String permissionCatalogIdsKey() {
    return CATALOG_PREFIX + ":ids";
  }

  /** Single JSON blob of all permissions for getAll (1 round trip). */
  public String permissionCatalogListKey() {
    return CATALOG_PREFIX + ":list";
  }

  /** Set of code index keys for clearCatalog without KEYS. */
  public String permissionCatalogCodeKeysSetKey() {
    return CATALOG_PREFIX + ":code_keys";
  }

  /** Set of name index keys for clearCatalog without KEYS. */
  public String permissionCatalogNameKeysSetKey() {
    return CATALOG_PREFIX + ":name_keys";
  }
}
