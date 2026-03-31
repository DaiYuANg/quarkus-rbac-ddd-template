package com.github.DaiYuANg.cache;

import com.google.common.base.Strings;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

final class PermissionCatalogSearchSupport {

  private PermissionCatalogSearchSupport() {}

  static PermissionCatalogPage findPage(
      List<PermissionCatalogEntry> all,
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
    val filtered =
        all.stream()
            .filter(p -> matchesKeyword(p, keyword))
            .filter(p -> matchesLike(p.name(), name))
            .filter(p -> matchesLike(p.code(), code))
            .filter(p -> matchesEq(p.resource(), resource))
            .filter(p -> matchesLike(p.action(), action))
            .filter(p -> matchesEq(p.groupCode(), groupCode))
            .toList();
    val total = filtered.size();
    val cmp = sortComparator(sortBy, sortDirection);
    val sorted = filtered.stream().sorted(cmp).toList();
    val content = sorted.stream().skip(offset).limit(Math.max(1, limit)).toList();
    return new PermissionCatalogPage(content, total);
  }

  private static boolean matchesKeyword(PermissionCatalogEntry p, String keyword) {
    val normalizedKeyword = normalize(keyword);
    if (normalizedKeyword == null) {
      return true;
    }
    return containsIgnoreCase(p.name(), normalizedKeyword)
        || containsIgnoreCase(p.code(), normalizedKeyword)
        || containsIgnoreCase(p.resource(), normalizedKeyword)
        || containsIgnoreCase(p.action(), normalizedKeyword)
        || containsIgnoreCase(p.groupCode(), normalizedKeyword)
        || containsIgnoreCase(p.description(), normalizedKeyword);
  }

  private static boolean matchesLike(String field, String pattern) {
    val normalizedPattern = normalize(pattern);
    if (normalizedPattern == null) {
      return true;
    }
    return containsIgnoreCase(field, normalizedPattern);
  }

  private static boolean matchesEq(String field, String value) {
    val normalizedValue = normalize(value);
    if (normalizedValue == null) {
      return true;
    }
    return normalizedValue.equalsIgnoreCase(Strings.nullToEmpty(field));
  }

  private static boolean containsIgnoreCase(String s, String sub) {
    if (s == null) {
      return false;
    }
    return s.toLowerCase().contains(sub);
  }

  private static Comparator<PermissionCatalogEntry> sortComparator(String sortBy, String sortDir) {
    val asc = !"desc".equalsIgnoreCase(Strings.nullToEmpty(sortDir).trim());
    Comparator<PermissionCatalogEntry> cmp =
        switch (Strings.nullToEmpty(sortBy).trim().toLowerCase().isEmpty()
            ? "id"
            : Strings.nullToEmpty(sortBy).trim().toLowerCase()) {
          case "name" ->
              Comparator.comparing(
                  p -> Strings.nullToEmpty(p.name()), String.CASE_INSENSITIVE_ORDER);
          case "code" ->
              Comparator.comparing(
                  p -> Strings.nullToEmpty(p.code()), String.CASE_INSENSITIVE_ORDER);
          case "resource" ->
              Comparator.comparing(
                  p -> Strings.nullToEmpty(p.resource()), String.CASE_INSENSITIVE_ORDER);
          case "action" ->
              Comparator.comparing(
                  p -> Strings.nullToEmpty(p.action()), String.CASE_INSENSITIVE_ORDER);
          case "groupcode" ->
              Comparator.comparing(
                  p -> Strings.nullToEmpty(p.groupCode()), String.CASE_INSENSITIVE_ORDER);
          case "createtime" ->
              Comparator.comparing(p -> p.createAt() != null ? p.createAt() : Instant.EPOCH);
          case "updatetime" ->
              Comparator.comparing(p -> p.updateAt() != null ? p.updateAt() : Instant.EPOCH);
          default -> Comparator.comparing(PermissionCatalogEntry::id);
        };
    return asc ? cmp : cmp.reversed();
  }

  private static String normalize(String value) {
    val normalized = StringUtils.trimToNull(value);
    return normalized == null ? null : normalized.toLowerCase();
  }
}
