package com.github.DaiYuANg.modules.accesscontrol.application.role;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.query.RolePageQuery;
import com.github.DaiYuANg.accesscontrol.repository.PermissionGroupRepository;
import com.github.DaiYuANg.accesscontrol.repository.RoleRepository;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.common.model.ApiPageResult;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.RoleCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.RoleRefPermissionGroupForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdateRoleForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.RoleVO;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.RoleVOBuilder;
import com.github.DaiYuANg.modules.accesscontrol.application.permissiongroup.PermissionGroupApplicationService;
import com.github.DaiYuANg.modules.accesscontrol.application.support.AccessControlAuditSupport;
import com.github.DaiYuANg.security.authorization.AuthorizationService;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.Role;
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
  private final PermissionGroupApplicationService permissionGroupApplicationService;
  private final AccessControlAuditSupport auditSupport;
  private final AuthorizationService authorizationService;

  @Transactional
  public RoleVO createRole(@NonNull RoleCreationForm form) {
    authorizationService.check(Role.ADD);
    if (roleRepository.countByCode(form.code()) > 0)
      throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "role code already exists");
    val role = new SysRole();
    role.code = form.code();
    role.name = form.name();
    role.status = form.status() == null ? RoleStatus.ENABLED : form.status();
    role.sort = form.sort();
    role.description = form.description();
    roleRepository.persist(role);
    auditSupport.bumpGlobalVersion();
    auditSupport.record("role", "create", form.code(), true, "create role");
    return toRoleVOWithCatalog(role);
  }

  public ApiPageResult<RoleVO> queryRolePage(@NonNull RolePageQuery query) {
    authorizationService.check(Role.VIEW);
    return ApiPageResult.map(roleRepository.page(query), this::toRoleVO);
  }

  public Optional<RoleVO> getRoleById(@NonNull Long id) {
    authorizationService.check(Role.VIEW);
    return roleRepository.findByIdOptional(id).map(this::toRoleVOWithCatalog);
  }

  @Transactional
  public RoleVO updateRole(@NonNull Long id, @NonNull UpdateRoleForm form) {
    authorizationService.check(Role.EDIT);
    val role =
        roleRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    if (form.code() != null
        && !form.code().equals(role.code)
        && roleRepository.countByCode(form.code()) > 0)
      throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "role code already exists");
    if (form.code() != null) role.code = form.code();
    if (form.name() != null) role.name = form.name();
    if (form.status() != null) role.status = form.status();
    if (form.sort() != null) role.sort = form.sort();
    if (form.description() != null) role.description = form.description();
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
    authorizationService.check(Role.DELETE);
    val role =
        roleRepository
            .findByIdOptional(id)
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    roleRepository.delete(role);
    auditSupport.bumpGlobalVersion();
    auditSupport.record("role", "delete", String.valueOf(id), true, "delete role");
  }

  public Optional<RoleVO> getRoleByName(@NonNull String name) {
    authorizationService.check(Role.VIEW);
    return roleRepository.findByName(name).map(this::toRoleVOWithCatalog);
  }

  @Transactional
  public void assignPermissionGroups(@NonNull RoleRefPermissionGroupForm form) {
    authorizationService.checkAny(Role.EDIT, Role.ASSIGN_PERMISSION_GROUP);
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
    authorizationService.check(Role.VIEW);
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
    authorizationService.check(Role.VIEW);
    return roleRepository.countByCode(code);
  }

  public long countRole() {
    authorizationService.check(Role.VIEW);
    return roleRepository.count();
  }

  private RoleVO toRoleVO(
      @NonNull com.github.DaiYuANg.accesscontrol.projection.RoleListProjection role) {
    return RoleVOBuilder.builder()
        .id(role.id())
        .name(role.name())
        .code(role.code())
        .status(parseRoleStatus(role.status()))
        .sort(role.sort())
        .createAt(null)
        .updateAt(null)
        .permissionGroups(new LinkedHashSet<>())
        .build();
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
                    permissionGroupApplicationService.toPermissionGroupVOWithCatalog(
                        group, permissionIdsByGroupId.getOrDefault(group.id, java.util.Set.of()).stream().toList()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    return RoleVOBuilder.builder()
        .id(role.id)
        .name(role.name)
        .code(role.code)
        .status(parseRoleStatus(role.status != null ? role.status.name() : null))
        .sort(role.sort)
        .createAt(role.createAt)
        .updateAt(role.updateAt)
        .permissionGroups(permissionGroups)
        .build();
  }

  private RoleStatus parseRoleStatus(String value) {
    if (value == null || value.isBlank()) {
      return RoleStatus.ENABLED;
    }
    return RoleStatus.valueOf(value);
  }

  private Stream<com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup> streamPermissionGroups(
      @NonNull SysRole role) {
    return role.permissionGroups == null
        ? Stream.empty()
        : role.permissionGroups.stream().filter(Objects::nonNull);
  }
}
