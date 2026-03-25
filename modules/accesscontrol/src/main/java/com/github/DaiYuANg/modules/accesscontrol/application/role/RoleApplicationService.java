package com.github.DaiYuANg.modules.accesscontrol.application.role;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.parameter.RoleQuery;
import com.github.DaiYuANg.accesscontrol.repository.PermissionGroupRepository;
import com.github.DaiYuANg.accesscontrol.repository.RoleRepository;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.common.model.PageResult;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.RoleCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.RoleRefPermissionGroupForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdateRoleForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.RoleVO;
import com.github.DaiYuANg.modules.accesscontrol.application.permissiongroup.PermissionGroupApplicationService;
import com.github.DaiYuANg.modules.accesscontrol.application.support.AccessControlAuditSupport;
import com.github.DaiYuANg.security.authorization.AuthorizationService;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.Role;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RoleApplicationService {
  private final RoleRepository roleRepository;
  private final PermissionGroupRepository permissionGroupRepository;
  private final PermissionGroupApplicationService permissionGroupApplicationService;
  private final AccessControlAuditSupport auditSupport;
  private final AuthorizationService authorizationService;

  @Transactional
  public RoleVO createRole(RoleCreationForm form) {
    authorizationService.check(Role.ADD);
    if (roleRepository.countByCode(form.code()) > 0)
      throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "role code already exists");
    var role = new SysRole();
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

  public PageResult<RoleVO> queryRolePage(RoleQuery query) {
    authorizationService.check(Role.VIEW);
    var slice = roleRepository.page(query);
    return PageResult.of(
        slice.total(),
        query.getPageNum(),
        query.getPageSize(),
        slice.content().stream().map(this::toRoleVO).toList());
  }

  public Optional<RoleVO> getRoleById(Long id) {
    authorizationService.check(Role.VIEW);
    return roleRepository.findByIdOptional(id).map(this::toRoleVOWithCatalog);
  }

  @Transactional
  public RoleVO updateRole(Long id, UpdateRoleForm form) {
    authorizationService.check(Role.EDIT);
    var role =
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
      permissionGroupRepository
          .findAllByIds(form.permissionGroupIds())
          .forEach(role.permissionGroups::add);
    }
    auditSupport.bumpGlobalVersion();
    auditSupport.record("role", "update", String.valueOf(id), true, "update role");
    return toRoleVOWithCatalog(role);
  }

  @Transactional
  public void deleteRole(Long id) {
    authorizationService.check(Role.DELETE);
    roleRepository.deleteById(id);
    auditSupport.bumpGlobalVersion();
    auditSupport.record("role", "delete", String.valueOf(id), true, "delete role");
  }

  public Optional<RoleVO> getRoleByName(String name) {
    authorizationService.check(Role.VIEW);
    return roleRepository.findByName(name).map(this::toRoleVOWithCatalog);
  }

  @Transactional
  public void assignPermissionGroups(RoleRefPermissionGroupForm form) {
    authorizationService.checkAny(Role.EDIT, Role.ASSIGN_PERMISSION_GROUP);
    var role =
        roleRepository
            .findByIdOptional(form.roleId())
            .orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
    role.permissionGroups.clear();
    if (form.permissionGroupIds() != null) {
      permissionGroupRepository
          .findAllByIds(form.permissionGroupIds())
          .forEach(role.permissionGroups::add);
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
    var roles = roleRepository.listAllWithPermissionGroups();
    if (roles == null || roles.isEmpty()) {
      return List.of();
    }
    // Preload permission ids for all groups referenced by all roles (avoid N+1).
    var groupIds =
        roles.stream()
            .flatMap(
                r ->
                    (r == null || r.permissionGroups == null)
                        ? java.util.stream.Stream.empty()
                        : r.permissionGroups.stream())
            .map(g -> g == null ? null : g.id)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .toList();
    var permissionIdsByGroupId = permissionGroupRepository.findPermissionIdsByGroupIds(groupIds);
    return roles.stream()
        .filter(java.util.Objects::nonNull)
        .map(r -> toRoleVOWithCatalog(r, permissionIdsByGroupId))
        .toList();
  }

  public long countCode(String code) {
    return roleRepository.countByCode(code);
  }

  public long countRole() {
    return roleRepository.count();
  }

  private RoleVO toRoleVO(com.github.DaiYuANg.accesscontrol.projection.RoleListProjection role) {
    return new RoleVO(
        role.id(),
        role.name(),
        role.code(),
        parseRoleStatus(role.status()),
        role.sort(),
        null,
        null,
        new LinkedHashSet<>());
  }

  private RoleVO toRoleVOWithCatalog(SysRole role) {
    if (role == null) {
      throw new BizException(ResultCode.DATA_NOT_FOUND);
    }
    var groupIds =
        (role.permissionGroups == null ? List.<com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup>of() : role.permissionGroups.stream().toList())
            .stream()
            .filter(java.util.Objects::nonNull)
            .map(g -> g.id)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .toList();
    var permissionIdsByGroupId = permissionGroupRepository.findPermissionIdsByGroupIds(groupIds);
    return toRoleVOWithCatalog(role, permissionIdsByGroupId);
  }

  private RoleVO toRoleVOWithCatalog(
      SysRole role, java.util.Map<Long, Set<Long>> permissionIdsByGroupId) {
    var permissionGroups =
        (role.permissionGroups == null ? List.<com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup>of() : role.permissionGroups.stream().toList())
            .stream()
            .filter(java.util.Objects::nonNull)
            .map(
                g ->
                    permissionGroupApplicationService.toPermissionGroupVOWithCatalog(
                        g,
                        permissionIdsByGroupId.getOrDefault(g.id, java.util.Set.of()).stream()
                            .toList()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    return new RoleVO(
        role.id,
        role.name,
        role.code,
        parseRoleStatus(role.status != null ? role.status.name() : null),
        role.sort,
        role.createAt,
        role.updateAt,
        permissionGroups);
  }

  private RoleStatus parseRoleStatus(String value) {
    if (value == null || value.isBlank()) {
      return RoleStatus.ENABLED;
    }
    return RoleStatus.valueOf(value);
  }
}
