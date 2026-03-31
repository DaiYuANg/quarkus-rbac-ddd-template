package com.github.DaiYuANg.identity.repository;

import com.github.DaiYuANg.accesscontrol.entity.QSysPermission;
import com.github.DaiYuANg.accesscontrol.entity.QSysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.entity.QSysRole;
import com.github.DaiYuANg.identity.entity.QSysUser;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.persistence.query.BlazeJPAQueryFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
class UserRbacQuerySupport {
  private static final QSysUser u = new QSysUser("user");
  private static final QSysRole r = new QSysRole("role");
  private static final QSysPermissionGroup g = new QSysPermissionGroup("permissionGroup");
  private static final QSysPermission p = new QSysPermission("permission");

  private final BlazeJPAQueryFactory blazeQueryFactory;

  Optional<SysUser> findByUsernameWithRbacGraph(String username) {
    val normalizedUsername = StringUtils.trimToNull(username);
    if (normalizedUsername == null) {
      return Optional.empty();
    }
    return fetchRbacGraph(u.username.eq(normalizedUsername)).stream().findFirst();
  }

  Optional<SysUser> findByIdWithRbacGraph(Long id) {
    if (id == null) {
      return Optional.empty();
    }
    return fetchRbacGraph(u.id.eq(id)).stream().findFirst();
  }

  List<SysUser> listAllWithRbacGraph() {
    return fetchRbacGraph(null);
  }

  Set<String> findRoleCodesByUsername(String username) {
    val normalizedUsername = StringUtils.trimToNull(username);
    if (normalizedUsername == null) {
      return Set.of();
    }
    val rows =
        blazeQueryFactory
            .<String>create()
            .from(u)
            .join(u.roles, r)
            .select(r.code)
            .where(u.username.eq(normalizedUsername))
            .distinct()
            .fetch();
    return normalizeCodes(rows);
  }

  Set<String> findPermissionCodesByUsername(String username) {
    val normalizedUsername = StringUtils.trimToNull(username);
    if (normalizedUsername == null) {
      return Set.of();
    }
    val rows =
        blazeQueryFactory
            .<String>create()
            .from(u)
            .join(u.roles, r)
            .join(r.permissionGroups, g)
            .join(g.permissions, p)
            .select(p.code)
            .where(u.username.eq(normalizedUsername))
            .distinct()
            .fetch();
    return normalizeCodes(rows);
  }

  private List<SysUser> fetchRbacGraph(com.querydsl.core.types.Predicate predicate) {
    val query =
        blazeQueryFactory
            .selectFrom(u)
            .leftJoin(u.roles, r)
            .fetchJoin()
            .leftJoin(r.permissionGroups, g)
            .fetchJoin()
            .leftJoin(g.permissions, p)
            .fetchJoin()
            .distinct();
    if (predicate != null) {
      query.where(predicate);
    }
    return query.fetch();
  }

  private Set<String> normalizeCodes(List<String> rows) {
    val result =
        rows.stream()
            .map(StringUtils::trimToNull)
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    return Set.copyOf(result);
  }
}
