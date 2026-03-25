package com.github.DaiYuANg.modules.accesscontrol.application.permissiongroup;

import com.github.DaiYuANg.accesscontrol.entity.SysPermission;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.parameter.PermissionGroupQuery;
import com.github.DaiYuANg.accesscontrol.repository.PermissionGroupRepository;
import com.github.DaiYuANg.cache.PermissionCatalogEntry;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.common.model.PageResult;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.PermissionGroupCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.PermissionGroupRefPermissionForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdatePermissionGroupForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionGroupVO;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionVO;
import com.github.DaiYuANg.modules.accesscontrol.application.support.AccessControlAuditSupport;
import com.github.DaiYuANg.security.authorization.AuthorizationService;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.PermissionGroup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionGroupApplicationService {
  private final PermissionGroupRepository repository;
  private final PermissionCatalogStore catalogStore;
  private final EntityManager entityManager;
  private final AccessControlAuditSupport auditSupport;
  private final AuthorizationService authorizationService;

  @Transactional
  public PermissionGroupVO createPermissionGroup(PermissionGroupCreationForm form) {
    authorizationService.check(PermissionGroup.ADD);
    if (repository.countByName(form.name()) > 0)
      throw new BizException(
          ResultCode.DATA_ALREADY_EXISTS, "permission group name already exists");
    var group = new SysPermissionGroup();
    group.name = form.name();
    group.code = form.code();
    group.sort = form.sort();
    group.description = form.description();
    repository.persist(group);
    auditSupport.bumpGlobalVersion();
    auditSupport.record("permission-group", "create", form.name(), true, "create permission group");
    return toPermissionGroupVOWithCatalog(group);
  }

  public Optional<PermissionGroupVO> getPermissionGroupById(Long id) {
    authorizationService.check(PermissionGroup.VIEW);
    return repository.findByIdOptional(id).map(this::toPermissionGroupVOWithCatalog);
  }

  @Transactional
  public PermissionGroupVO updatePermissionGroup(Long id, UpdatePermissionGroupForm form) {
    authorizationService.check(PermissionGroup.EDIT);
    var group =
        repository
            .findByIdOptional(id)
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    if (form.name() != null
        && !form.name().equals(group.name)
        && repository.countByName(form.name()) > 0)
      throw new BizException(
          ResultCode.DATA_ALREADY_EXISTS, "permission group name already exists");
    if (form.name() != null) group.name = form.name();
    if (form.code() != null) group.code = form.code();
    if (form.sort() != null) group.sort = form.sort();
    if (form.description() != null) group.description = form.description();
    auditSupport.bumpGlobalVersion();
    auditSupport.record(
        "permission-group", "update", String.valueOf(id), true, "update permission group");
    return toPermissionGroupVOWithCatalog(group);
  }

  @Transactional
  public void deletePermissionGroup(Long id) {
    authorizationService.check(PermissionGroup.DELETE);
    repository.deleteById(id);
    auditSupport.bumpGlobalVersion();
    auditSupport.record(
        "permission-group", "delete", String.valueOf(id), true, "delete permission group");
  }

  public PageResult<PermissionGroupVO> queryPermissionGroupPage(PermissionGroupQuery query) {
    authorizationService.check(PermissionGroup.VIEW);
    var slice = repository.page(query);
    return PageResult.of(
        slice.total(),
        query.getPageNum(),
        query.getPageSize(),
        slice.content().stream().map(this::toPermissionGroupVO).toList());
  }

  public Optional<PermissionGroupVO> getPermissionGroupByName(String name) {
    authorizationService.check(PermissionGroup.VIEW);
    return repository.findByName(name).map(this::toPermissionGroupVOWithCatalog);
  }

  @Transactional
  public void assignPermissions(PermissionGroupRefPermissionForm form) {
    authorizationService.checkAny(PermissionGroup.EDIT, PermissionGroup.ASSIGN_PERMISSION);
    var group =
        repository
            .findByIdOptional(form.permissionGroupId())
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    group.permissions.clear();
    if (form.permissionIds() != null) {
      form.permissionIds()
          .forEach(
              id -> {
                if (catalogStore.getById(id).isPresent()) {
                  group.permissions.add(entityManager.getReference(SysPermission.class, id));
                }
              });
    }
    auditSupport.bumpGlobalVersion();
    auditSupport.record(
        "permission-group",
        "assign-permission",
        String.valueOf(form.permissionGroupId()),
        true,
        "assign permissions");
  }

  public List<PermissionGroupVO> getAllPermissionGroups() {
    authorizationService.check(PermissionGroup.VIEW);
    return repository.listAll().stream().map(this::toPermissionGroupVOWithCatalog).toList();
  }

  @Transactional
  public void bindPermissionsToGroup(Long targetGroupId, List<Long> permissionIds) {
    authorizationService.checkAny(PermissionGroup.EDIT, PermissionGroup.ASSIGN_PERMISSION);
    if (permissionIds == null || permissionIds.isEmpty()) {
      return;
    }
    var normalizedIds =
        permissionIds.stream()
            .filter(java.util.Objects::nonNull)
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

    var refs =
        normalizedIds.stream()
            .filter(id -> catalogStore.getById(id).isPresent())
            .map(id -> entityManager.getReference(SysPermission.class, id))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    if (refs.isEmpty()) {
      return;
    }

    var allGroups = repository.listAll();
    for (var group : allGroups) {
      if (target != null && group.id.equals(target.id)) {
        continue;
      }
      group.permissions.removeIf(permission -> normalizedIds.contains(permission.id));
    }
    if (target != null) {
      target.permissions.addAll(refs);
    }
    auditSupport.bumpGlobalVersion();
    auditSupport.record(
        "permission-group",
        targetGroupId == null ? "unbind-permission" : "bind-permission",
        String.valueOf(targetGroupId),
        true,
        "bind permissions by groupId");
  }

  public long countName(String name) {
    return repository.countByName(name);
  }

  public long count() {
    return repository.count();
  }

  private PermissionGroupVO toPermissionGroupVO(
      com.github.DaiYuANg.accesscontrol.projection.PermissionGroupListProjection group) {
    return new PermissionGroupVO(
        group.id(),
        group.name(),
        group.description(),
        group.code(),
        group.sort(),
        null,
        null,
        new LinkedHashSet<>());
  }

  public PermissionGroupVO toPermissionGroupVOWithCatalog(SysPermissionGroup group) {
    var permissionIds = repository.findPermissionIdsByGroupId(group.id);
    var permissions =
        permissionIds.stream()
            .map(catalogStore::getById)
            .flatMap(Optional::stream)
            .map(this::toPermissionVO)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    return new PermissionGroupVO(
        group.id,
        group.name,
        group.description,
        group.code,
        group.sort,
        group.createAt,
        group.updateAt,
        permissions);
  }

  private PermissionVO toPermissionVO(PermissionCatalogEntry e) {
    return new PermissionVO(
        e.id(),
        e.name(),
        e.code(),
        e.resource(),
        e.action(),
        e.groupCode(),
        e.description(),
        e.expression());
  }
}
