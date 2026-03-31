package com.github.DaiYuANg.modules.accesscontrol.application.permissiongroup;

import com.github.DaiYuANg.accesscontrol.entity.SysPermission;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.query.PermissionGroupPageQuery;
import com.github.DaiYuANg.accesscontrol.repository.PermissionGroupRepository;
import com.github.DaiYuANg.cache.PermissionCatalogEntry;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.common.model.ApiPageResult;
import com.github.DaiYuANg.modules.accesscontrol.application.mapper.PermissionGroupVOMapper;
import com.github.DaiYuANg.modules.accesscontrol.application.mapper.PermissionVOMapper;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.PermissionGroupCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.PermissionGroupRefPermissionForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdatePermissionGroupForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionGroupVO;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionVO;
import com.github.DaiYuANg.modules.accesscontrol.application.support.AccessControlAuditSupport;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Permission-group management application service.
 *
 * <p>Performance notes:
 *
 * <ul>
 *   <li>For list endpoints, permission ids are bulk-resolved to avoid N+1 queries.
 *   <li>For bulk (un)bind operations, join-table updates are performed without initializing
 *       {@code @ManyToMany} collections.
 * </ul>
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionGroupApplicationService {
  private final PermissionGroupRepository repository;
  private final PermissionCatalogStore catalogStore;
  private final EntityManager entityManager;
  private final AccessControlAuditSupport auditSupport;
  private final PermissionGroupVOMapper permissionGroupVOMapper;
  private final PermissionVOMapper permissionVOMapper;
  private final PermissionGroupChecker permissionGroupChecker;

  @Transactional
  public PermissionGroupVO createPermissionGroup(@NonNull PermissionGroupCreationForm form) {
    permissionGroupChecker.ensureCreatable(form);
    val group = permissionGroupVOMapper.toEntity(form);
    repository.persist(group);
    auditSupport.bumpGlobalVersion();
    auditSupport.record("permission-group", "create", form.name(), true, "create permission group");
    return toPermissionGroupVOWithCatalog(group);
  }

  public Optional<PermissionGroupVO> getPermissionGroupById(@NonNull Long id) {
    return repository.findByIdOptional(id).map(this::toPermissionGroupVOWithCatalog);
  }

  @Transactional
  public PermissionGroupVO updatePermissionGroup(
      @NonNull Long id, @NonNull UpdatePermissionGroupForm form) {
    val group =
        repository
            .findByIdOptional(id)
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    permissionGroupChecker.ensureUpdatable(group, form);
    permissionGroupVOMapper.updateEntity(form, group);
    auditSupport.bumpGlobalVersion();
    auditSupport.record(
        "permission-group", "update", String.valueOf(id), true, "update permission group");
    return toPermissionGroupVOWithCatalog(group);
  }

  @Transactional
  public void deletePermissionGroup(@NonNull Long id) {
    val group =
        repository
            .findByIdOptional(id)
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    repository.delete(group);
    auditSupport.bumpGlobalVersion();
    auditSupport.record(
        "permission-group", "delete", String.valueOf(id), true, "delete permission group");
  }

  public ApiPageResult<PermissionGroupVO> queryPermissionGroupPage(
      @NonNull PermissionGroupPageQuery query) {
    return ApiPageResult.map(repository.page(query), permissionGroupVOMapper::toProjectionVO);
  }

  public Optional<PermissionGroupVO> getPermissionGroupByName(@NonNull String name) {
    return repository.findByName(name).map(this::toPermissionGroupVOWithCatalog);
  }

  @Transactional
  public void assignPermissions(@NonNull PermissionGroupRefPermissionForm form) {
    val group =
        repository
            .findByIdOptional(form.permissionGroupId())
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    val ids =
        form.permissionIds() == null
            ? List.<Long>of()
            : form.permissionIds().stream()
                .filter(Objects::nonNull)
                .filter(id -> catalogStore.getById(id).isPresent())
                .distinct()
                .toList();
    repository.replacePermissionRefs(group.id, ids);
    auditSupport.bumpGlobalVersion();
    auditSupport.record(
        "permission-group",
        "assign-permission",
        String.valueOf(form.permissionGroupId()),
        true,
        "assign permissions");
  }

  public List<PermissionGroupVO> getAllPermissionGroups() {
    val groups = repository.listAll();
    if (groups == null || groups.isEmpty()) {
      return List.of();
    }
    val groupIds =
        groups.stream().filter(Objects::nonNull).map(group -> group.id).toList();
    val permissionIdsByGroupId = repository.findPermissionIdsByGroupIds(groupIds);
    return groups.stream()
        .filter(Objects::nonNull)
        .map(
            group ->
                toPermissionGroupVOWithCatalog(
                    group, permissionIdsByGroupId.getOrDefault(group.id, Set.of()).stream().toList()))
        .toList();
  }

  @Transactional
  public void bindPermissionsToGroup(Long targetGroupId, List<Long> permissionIds) {
    if (permissionIds == null || permissionIds.isEmpty()) {
      return;
    }
    val normalizedIds =
        permissionIds.stream()
            .filter(Objects::nonNull)
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
            .filter(id -> catalogStore.getById(id).isPresent())
            .map(id -> entityManager.getReference(SysPermission.class, id))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    if (refs.isEmpty()) {
      return;
    }

    // Avoid N+1 lazy loads on group.permissions by deleting join rows in bulk.
    repository.deletePermissionRefsByPermissionIds(
        normalizedIds.stream().toList(), target == null ? null : target.id);
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

  public PermissionGroupVO toPermissionGroupVOWithCatalog(@NonNull SysPermissionGroup group) {
    val permissionIds = repository.findPermissionIdsByGroupId(group.id);
    return toPermissionGroupVOWithCatalog(group, permissionIds);
  }

  public PermissionGroupVO toPermissionGroupVOWithCatalog(
      @NonNull SysPermissionGroup group, List<Long> permissionIds) {
    return permissionGroupVOMapper.toVOWithPermissions(group, resolvePermissions(permissionIds));
  }

  private Stream<Long> streamPermissionIds(List<Long> permissionIds) {
    return permissionIds == null ? Stream.empty() : permissionIds.stream().filter(Objects::nonNull);
  }

  private LinkedHashSet<PermissionVO> resolvePermissions(List<Long> permissionIds) {
    return streamPermissionIds(permissionIds)
        .map(catalogStore::getById)
        .flatMap(Optional::stream)
        .map(permissionVOMapper::toCatalogVO)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
