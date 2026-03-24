package com.github.DaiYuANg.cache;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

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
    var filtered =
        all.stream()
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
    return new PermissionCatalogPage(content, total);
  }

  private static boolean matchesKeyword(PermissionCatalogEntry p, String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return true;
    }
    var k = keyword.toLowerCase();
    return containsIgnoreCase(p.name(), k)
        || containsIgnoreCase(p.code(), k)
        || containsIgnoreCase(p.resource(), k)
        || containsIgnoreCase(p.action(), k)
        || containsIgnoreCase(p.groupCode(), k)
        || containsIgnoreCase(p.description(), k);
  }

  private static boolean matchesLike(String field, String pattern) {
    if (pattern == null || pattern.isBlank()) {
      return true;
    }
    return containsIgnoreCase(field, pattern.toLowerCase());
  }

  private static boolean matchesEq(String field, String value) {
    if (value == null || value.isBlank()) {
      return true;
    }
    return value.equalsIgnoreCase(field != null ? field : "");
  }

  private static boolean containsIgnoreCase(String s, String sub) {
    if (s == null) {
      return false;
    }
    return s.toLowerCase().contains(sub);
  }

  private static Comparator<PermissionCatalogEntry> sortComparator(String sortBy, String sortDir) {
    var asc = sortDir == null || !"desc".equalsIgnoreCase(sortDir.trim());
    Comparator<PermissionCatalogEntry> cmp =
        switch (sortBy != null ? sortBy.trim().toLowerCase() : "id") {
          case "name" ->
              Comparator.comparing(
                  p -> (p.name() != null ? p.name() : ""), String.CASE_INSENSITIVE_ORDER);
          case "code" ->
              Comparator.comparing(
                  p -> (p.code() != null ? p.code() : ""), String.CASE_INSENSITIVE_ORDER);
          case "resource" ->
              Comparator.comparing(
                  p -> (p.resource() != null ? p.resource() : ""), String.CASE_INSENSITIVE_ORDER);
          case "action" ->
              Comparator.comparing(
                  p -> (p.action() != null ? p.action() : ""), String.CASE_INSENSITIVE_ORDER);
          case "groupcode" ->
              Comparator.comparing(
                  p -> (p.groupCode() != null ? p.groupCode() : ""), String.CASE_INSENSITIVE_ORDER);
          case "createtime" ->
              Comparator.comparing(p -> p.createAt() != null ? p.createAt() : Instant.EPOCH);
          case "updatetime" ->
              Comparator.comparing(p -> p.updateAt() != null ? p.updateAt() : Instant.EPOCH);
          default -> Comparator.comparing(PermissionCatalogEntry::id);
        };
    return asc ? cmp : cmp.reversed();
  }
}
