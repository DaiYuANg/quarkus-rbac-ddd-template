package com.github.DaiYuANg.cache;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.time.Instant;
import org.apache.commons.lang3.StringUtils;

/**
 * In-memory representation of a permission for the catalog cache. Used when loading from DB and
 * reading from Redis.
 */
@RecordBuilder
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
    return StringUtils.trimToEmpty(s);
  }
}
