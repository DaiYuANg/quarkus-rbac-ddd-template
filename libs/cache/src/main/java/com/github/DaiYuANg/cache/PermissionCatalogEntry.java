package com.github.DaiYuANg.cache;

import java.time.Instant;

/**
 * In-memory representation of a permission for the catalog cache. Used when loading from DB and
 * reading from Redis.
 */
public record PermissionCatalogEntry(
    Long id,
    String name,
    String code,
    String resource,
    String action,
    String groupCode,
    String description,
    String expression,
    Instant createAt,
    Instant updateAt) {
  public static String empty(String s) {
    return s == null || s.isBlank() ? "" : s;
  }
}
