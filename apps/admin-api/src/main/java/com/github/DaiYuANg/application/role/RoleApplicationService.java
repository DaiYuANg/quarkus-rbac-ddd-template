package com.github.DaiYuANg.application.role;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.parameter.RoleQuery;
import com.github.DaiYuANg.application.permissiongroup.PermissionGroupApplicationService;
import com.github.DaiYuANg.accesscontrol.repository.PermissionGroupRepository;
import com.github.DaiYuANg.accesscontrol.repository.RoleRepository;
import com.github.DaiYuANg.api.dto.request.RoleCreationForm;
import com.github.DaiYuANg.api.dto.request.RoleRefPermissionGroupForm;
import com.github.DaiYuANg.api.dto.request.UpdateRoleForm;
import com.github.DaiYuANg.api.dto.response.RoleVO;
import com.github.DaiYuANg.application.audit.AuthorityVersionService;
import com.github.DaiYuANg.application.audit.OperationLogService;
import com.github.DaiYuANg.application.converter.ViewMapper;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.common.exception.BizException;
import com.github.DaiYuANg.common.model.PageResult;
import com.github.DaiYuANg.security.authorization.AuthorizationService;
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
    private final ViewMapper mapper;
    private final AuthorityVersionService authorityVersionService;
    private final OperationLogService operationLogService;
    private final AuthorizationService authorizationService;

    @Transactional
    public RoleVO createRole(RoleCreationForm form) {
        authorizationService.check("role", "add");
        if (roleRepository.countByCode(form.code()) > 0) throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "role code already exists");
        var role = new SysRole();
        role.code = form.code();
        role.name = form.name();
        role.status = form.status() == null ? RoleStatus.ENABLED : form.status();
        role.sort = form.sort();
        role.description = form.description();
        roleRepository.persist(role);
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("role", "create", form.code(), true, "create role");
        return toRoleVOWithCatalog(role);
    }

    public PageResult<RoleVO> queryRolePage(RoleQuery query) {
        authorizationService.check("role", "view");
        var slice = roleRepository.page(query);
        return PageResult.of(slice.total(), query.getPageNum(), query.getPageSize(), slice.content().stream().map(mapper::toRoleVO).toList());
    }
    public Optional<RoleVO> getRoleById(Long id) {
        authorizationService.check("role", "view");
        return roleRepository.findByIdOptional(id).map(this::toRoleVOWithCatalog);
    }

    @Transactional
    public RoleVO updateRole(Long id, UpdateRoleForm form) {
        authorizationService.check("role", "edit");
        var role = roleRepository.findByIdOptional(id).orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
        if (form.code() != null && !form.code().equals(role.code) && roleRepository.countByCode(form.code()) > 0) throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "role code already exists");
        if (form.code() != null) role.code = form.code();
        if (form.name() != null) role.name = form.name();
        if (form.status() != null) role.status = form.status();
        if (form.sort() != null) role.sort = form.sort();
        if (form.description() != null) role.description = form.description();
        if (form.permissionGroupIds() != null) {
            role.permissionGroups.clear();
            form.permissionGroupIds().forEach(pid -> permissionGroupRepository.findByIdOptional(pid).ifPresent(role.permissionGroups::add));
        }
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("role", "update", String.valueOf(id), true, "update role");
        return toRoleVOWithCatalog(role);
    }

    @Transactional
    public void deleteRole(Long id) { authorizationService.check("role", "delete"); roleRepository.deleteById(id); authorityVersionService.bumpGlobalVersion(); operationLogService.record("role", "delete", String.valueOf(id), true, "delete role"); }
    public Optional<RoleVO> getRoleByName(String name) {
        authorizationService.check("role", "view");
        return roleRepository.findByName(name).map(this::toRoleVOWithCatalog);
    }

    @Transactional
    public void assignPermissionGroups(RoleRefPermissionGroupForm form) {
        authorizationService.checkAny("role:edit", "role:assign-permission-group");
        var role = roleRepository.findByIdOptional(form.roleId()).orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
        role.permissionGroups.clear();
        if (form.permissionGroupIds() != null) {
            form.permissionGroupIds().forEach(id -> permissionGroupRepository.findByIdOptional(id).ifPresent(role.permissionGroups::add));
        }
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("role", "assign-permission-group", String.valueOf(form.roleId()), true, "assign permission groups");
    }

    public List<RoleVO> getAllRoles() {
        authorizationService.check("role", "view");
        return roleRepository.listAll().stream().map(this::toRoleVOWithCatalog).toList();
    }
    public long countCode(String code) { return roleRepository.countByCode(code); }
    public long countRole() { return roleRepository.count(); }

    private RoleVO toRoleVOWithCatalog(SysRole role) {
        var permissionGroups = role.permissionGroups.stream()
            .map(permissionGroupApplicationService::toPermissionGroupVOWithCatalog)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return new RoleVO(
            role.id,
            role.name,
            role.code,
            mapper.parseRoleStatus(role.status != null ? role.status.name() : null),
            role.sort,
            role.createAt,
            role.updateAt,
            permissionGroups);
    }
}
