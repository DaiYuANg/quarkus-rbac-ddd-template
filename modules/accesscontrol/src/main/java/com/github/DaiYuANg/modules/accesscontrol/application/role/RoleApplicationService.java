package com.github.DaiYuANg.modules.accesscontrol.application.role;

import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.query.RolePageQuery;
import com.github.DaiYuANg.accesscontrol.repository.PermissionGroupRepository;
import com.github.DaiYuANg.accesscontrol.repository.RoleRepository;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.common.model.ApiPageResult;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionGroupVO;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.RoleCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.RoleRefPermissionGroupForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdateRoleForm;
import com.github.DaiYuANg.modules.accesscontrol.application.mapper.PermissionGroupVOMapper;
import com.github.DaiYuANg.modules.accesscontrol.application.mapper.PermissionVOMapper;
import com.github.DaiYuANg.modules.accesscontrol.application.mapper.RoleVOMapper;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.RoleVO;
import com.github.DaiYuANg.modules.accesscontrol.application.support.AccessControlAuditSupport;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
 * Role management application service.
 *
 * <p>Performance notes:
 *
 * <ul>
 *   <li>List endpoints prefetch permission-group relations and bulk-resolve permission ids to avoid
 *       N+1 lazy loads.
 *   <li>Assignment operations use repository bulk loading for referenced ids.
 * </ul>
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RoleApplicationService {
  private final RoleRepository roleRepository;
  private final PermissionGroupRepository permissionGroupRepository;
  private final PermissionCatalogStore permissionCatalogStore;
  private final AccessControlAuditSupport auditSupport;
  private final RoleVOMapper roleVOMapper;
  private final PermissionGroupVOMapper permissionGroupVOMapper;
  private final PermissionVOMapper permissionVOMapper;
  private final RoleChecker roleChecker;

  @Transactional
  public RoleVO createRole(@NonNull RoleCreationForm form) {
    roleChecker.ensureCreatable(form);
    val role = roleVOMapper.toEntity(form);
    roleRepository.persist(role);
    auditSupport.bumpGlobalVersion();
    auditSupport.record("role", "create", form.code(), true, "create role");
    return toRoleVOWithCatalog(role);
  }

  public ApiPageResult<RoleVO> queryRolePage(@NonNull RolePageQuery query) {
    return ApiPageResult.map(roleRepository.page(query), roleVOMapper::toProjectionVO);
  }

  public Optional<RoleVO> getRoleById(@NonNull Long id) {
    return roleRepository.findByIdOptional(id).map(this::toRoleVOWithCatalog);
  }

  @Transactional
  public RoleVO updateRole(@NonNull Long id, @NonNull UpdateRoleForm form) {
    val role =
        roleRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    roleChecker.ensureUpdatable(role, form);
    roleVOMapper.updateEntity(form, role);
    if (form.permissionGroupIds() != null) {
      role.permissionGroups.clear();
      role.permissionGroups.addAll(permissionGroupRepository
        .findAllByIds(form.permissionGroupIds()));
    }
    auditSupport.bumpGlobalVersion();
    auditSupport.record("role", "update", String.valueOf(id), true, "update role");
    return toRoleVOWithCatalog(role);
  }

  @Transactional
  public void deleteRole(@NonNull Long id) {
    val role =
        roleRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    roleRepository.delete(role);
    auditSupport.bumpGlobalVersion();
    auditSupport.record("role", "delete", String.valueOf(id), true, "delete role");
  }

  public Optional<RoleVO> getRoleByName(@NonNull String name) {
    return roleRepository.findByName(name).map(this::toRoleVOWithCatalog);
  }

  @Transactional
  public void assignPermissionGroups(@NonNull RoleRefPermissionGroupForm form) {
    val role =
        roleRepository
            .findByIdOptional(form.roleId())
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    role.permissionGroups.clear();
    if (form.permissionGroupIds() != null) {
      role.permissionGroups.addAll(permissionGroupRepository
        .findAllByIds(form.permissionGroupIds()));
    }
    auditSupport.bumpGlobalVersion();
    auditSupport.record(
        "role",
        "assign-permission-group",
        String.valueOf(form.roleId()),
        true,
        "assign permission groups");
  }

  public List<RoleVO> getAllRoles() {
    val roles = roleRepository.listAllWithPermissionGroups();
    if (roles == null || roles.isEmpty()) {
      return List.of();
    }
    // Preload permission ids for all groups referenced by all roles (avoid N+1).
    val groupIds =
        roles.stream()
            .flatMap(
                role ->
                    role == null || role.permissionGroups == null
                        ? Stream.empty()
                        : role.permissionGroups.stream())
            .filter(Objects::nonNull)
            .map(group -> group.id)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    val permissionIdsByGroupId = permissionGroupRepository.findPermissionIdsByGroupIds(groupIds);
    return roles.stream()
        .filter(Objects::nonNull)
        .map(role -> toRoleVOWithCatalog(role, permissionIdsByGroupId))
        .toList();
  }

  public long countCode(String code) {
    return roleRepository.countByCode(code);
  }

  public long countRole() {
    return roleRepository.count();
  }

  private RoleVO toRoleVOWithCatalog(@NonNull SysRole role) {
    val groupIds =
        streamPermissionGroups(role)
            .map(group -> group.id)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    val permissionIdsByGroupId = permissionGroupRepository.findPermissionIdsByGroupIds(groupIds);
    return toRoleVOWithCatalog(role, permissionIdsByGroupId);
  }

  private RoleVO toRoleVOWithCatalog(
      @NonNull SysRole role, @NonNull java.util.Map<Long, Set<Long>> permissionIdsByGroupId) {
    val permissionGroups =
        streamPermissionGroups(role)
            .map(
                group ->
                    permissionGroupVOMapper.toVOWithPermissions(
                        group,
                        permissionIdsByGroupId.getOrDefault(group.id, java.util.Set.of()).stream()
                            .map(permissionCatalogStore::getById)
                            .flatMap(Optional::stream)
                            .map(permissionVOMapper::toCatalogVO)
                            .collect(Collectors.toCollection(LinkedHashSet::new))))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    return roleVOMapper.toVOWithPermissionGroups(role, permissionGroups);
  }

  private Stream<com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup> streamPermissionGroups(
      @NonNull SysRole role) {
    return role.permissionGroups == null
        ? Stream.empty()
        : role.permissionGroups.stream().filter(Objects::nonNull);
  }
}
