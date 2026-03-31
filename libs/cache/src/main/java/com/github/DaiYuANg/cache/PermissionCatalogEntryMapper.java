package com.github.DaiYuANg.cache;

import java.time.Instant;
import java.util.Map;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

final class PermissionCatalogEntryMapper {

  static final String HASH_ID = "id";
  static final String HASH_NAME = "name";
  static final String HASH_CODE = "code";
  static final String HASH_RESOURCE = "resource";
  static final String HASH_ACTION = "action";
  static final String HASH_GROUP_CODE = "groupCode";
  static final String HASH_DESCRIPTION = "description";
  static final String HASH_EXPRESSION = "expression";
  static final String HASH_CREATE_AT = "createAt";
  static final String HASH_UPDATE_AT = "updateAt";

  private PermissionCatalogEntryMapper() {}

  static Map<String, String> toHashFields(PermissionCatalogEntry p) {
    return Map.of(
        HASH_ID, String.valueOf(p.id()),
        HASH_NAME, PermissionCatalogEntry.empty(p.name()),
        HASH_CODE, PermissionCatalogEntry.empty(p.code()),
        HASH_RESOURCE, PermissionCatalogEntry.empty(p.resource()),
        HASH_ACTION, PermissionCatalogEntry.empty(p.action()),
        HASH_GROUP_CODE, PermissionCatalogEntry.empty(p.groupCode()),
        HASH_DESCRIPTION, PermissionCatalogEntry.empty(p.description()),
        HASH_EXPRESSION, PermissionCatalogEntry.empty(p.expression()),
        HASH_CREATE_AT, p.createAt() != null ? String.valueOf(p.createAt().toEpochMilli()) : "0",
        HASH_UPDATE_AT, p.updateAt() != null ? String.valueOf(p.updateAt().toEpochMilli()) : "0");
  }

  static PermissionCatalogEntry fromMap(Long id, Map<String, String> map) {
    val createAt = parseInstant(map.get(HASH_CREATE_AT));
    val updateAt = parseInstant(map.get(HASH_UPDATE_AT));
    return PermissionCatalogEntryBuilder.builder()
        .id(id)
        .name(map.getOrDefault(HASH_NAME, ""))
        .code(map.getOrDefault(HASH_CODE, ""))
        .resource(map.getOrDefault(HASH_RESOURCE, ""))
        .action(map.getOrDefault(HASH_ACTION, ""))
        .groupCode(nullOrEmpty(map.get(HASH_GROUP_CODE)))
        .description(nullOrEmpty(map.get(HASH_DESCRIPTION)))
        .expression(nullOrEmpty(map.get(HASH_EXPRESSION)))
        .createAt(createAt)
        .updateAt(updateAt)
        .build();
  }

  private static Instant parseInstant(String s) {
    val normalized = StringUtils.trimToNull(s);
    if (normalized == null) {
      return Instant.EPOCH;
    }
    try {
      return Instant.ofEpochMilli(Long.parseLong(normalized));
    } catch (NumberFormatException e) {
      return Instant.EPOCH;
    }
  }

  private static String nullOrEmpty(String s) {
    return StringUtils.trimToNull(s);
  }
}
