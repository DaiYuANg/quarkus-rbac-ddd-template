package com.github.DaiYuANg.accesscontrol.repository;

import com.github.DaiYuANg.accesscontrol.entity.QSysPermissionGroupRefPermission;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroupRefPermission;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
class PermissionGroupPermissionMutationSupport {
  private static final QSysPermissionGroupRefPermission gp =
      new QSysPermissionGroupRefPermission("permissionGroupRefPermission");

  private final JPAQueryFactory jpaQueryFactory;
  private final EntityManager entityManager;

  @Transactional
  int deletePermissionRefsByPermissionIds(List<Long> permissionIds, Long excludeGroupId) {
    if (permissionIds == null || permissionIds.isEmpty()) {
      return 0;
    }
    val normalized = permissionIds.stream().filter(Objects::nonNull).distinct().toList();
    if (normalized.isEmpty()) {
      return 0;
    }
    val clause = jpaQueryFactory.delete(gp).where(gp.id.permissionId.in(normalized));
    if (excludeGroupId != null) {
      clause.where(gp.id.permissionGroupId.ne(excludeGroupId));
    }
    return (int) clause.execute();
  }

  @Transactional
  void replacePermissionRefs(Long groupId, List<Long> permissionIds) {
    if (groupId == null) {
      return;
    }
    jpaQueryFactory.delete(gp).where(gp.id.permissionGroupId.eq(groupId)).execute();

    if (permissionIds == null || permissionIds.isEmpty()) {
      return;
    }
    val normalized = permissionIds.stream().filter(Objects::nonNull).distinct().toList();
    if (normalized.isEmpty()) {
      return;
    }
    normalized.stream()
        .map(pid -> new SysPermissionGroupRefPermission(groupId, pid))
        .forEach(entityManager::persist);
  }
}
