package com.github.DaiYuANg.accesscontrol.repository;

import com.github.DaiYuANg.accesscontrol.entity.QSysPermission;
import com.github.DaiYuANg.accesscontrol.entity.QSysPermissionGroup;
import com.github.DaiYuANg.persistence.query.BlazeJPAQueryFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
class PermissionGroupPermissionQuerySupport {
  private static final QSysPermissionGroup g = new QSysPermissionGroup("permissionGroup");
  private static final QSysPermission p = new QSysPermission("permission");

  private final BlazeJPAQueryFactory blazeQueryFactory;

  List<Long> findPermissionIdsByGroupId(Long groupId) {
    if (groupId == null) {
      return List.of();
    }
    return blazeQueryFactory
        .<Long>create()
        .from(g)
        .join(g.permissions, p)
        .select(p.id)
        .where(g.id.eq(groupId))
        .distinct()
        .fetch();
  }

  Map<Long, Set<Long>> findPermissionIdsByGroupIds(Collection<Long> groupIds) {
    if (groupIds == null || groupIds.isEmpty()) {
      return Map.of();
    }
    val normalized = groupIds.stream().filter(Objects::nonNull).distinct().toList();
    if (normalized.isEmpty()) {
      return Map.of();
    }
    val rows =
        blazeQueryFactory
            .<com.querydsl.core.Tuple>create()
            .from(g)
            .join(g.permissions, p)
            .select(g.id, p.id)
            .where(g.id.in(normalized))
            .distinct()
            .fetch();
    return rows.stream()
        .filter(Objects::nonNull)
        .map(row -> Map.entry(Objects.requireNonNull(row.get(g.id)), Objects.requireNonNull(row.get(p.id))))
        .filter(entry -> entry.getKey() != null && entry.getValue() != null)
        .collect(
            Collectors.groupingBy(
                Map.Entry::getKey,
                LinkedHashMap::new,
                Collectors.mapping(
                    Map.Entry::getValue, Collectors.toCollection(LinkedHashSet::new))));
  }
}
