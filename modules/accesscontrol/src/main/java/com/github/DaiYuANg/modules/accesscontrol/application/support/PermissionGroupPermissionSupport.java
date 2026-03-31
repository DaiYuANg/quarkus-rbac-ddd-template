package com.github.DaiYuANg.modules.accesscontrol.application.support;

import com.github.DaiYuANg.accesscontrol.entity.SysPermission;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.repository.PermissionGroupRepository;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionGroupPermissionSupport {
  private final PermissionGroupRepository repository;
  private final PermissionCatalogStore catalogStore;
  private final EntityManager entityManager;

  public void replacePermissions(@NonNull Long groupId, List<Long> permissionIds) {
    repository.replacePermissionRefs(groupId, normalizeCatalogPermissionIds(permissionIds));
  }

  public void bindPermissionsToGroup(Long targetGroupId, List<Long> permissionIds) {
    if (permissionIds == null || permissionIds.isEmpty()) {
      return;
    }

    val normalizedIds =
        permissionIds.stream()
            .filter(Objects::nonNull)
            .filter(id -> catalogStore.getById(id).isPresent())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    if (normalizedIds.isEmpty()) {
      return;
    }

    SysPermissionGroup target = null;
    if (targetGroupId != null) {
      target =
          repository
              .findByIdOptional(targetGroupId)
              .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    }

    val refs =
        normalizedIds.stream()
            .map(id -> entityManager.getReference(SysPermission.class, id))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    if (refs.isEmpty()) {
      return;
    }

    repository.deletePermissionRefsByPermissionIds(
        normalizedIds.stream().toList(), target == null ? null : target.id);
    if (target != null) {
      target.permissions.addAll(refs);
    }
  }

  private List<Long> normalizeCatalogPermissionIds(List<Long> permissionIds) {
    if (permissionIds == null) {
      return List.of();
    }
    return permissionIds.stream()
        .filter(Objects::nonNull)
        .filter(id -> catalogStore.getById(id).isPresent())
        .distinct()
        .toList();
  }
}
