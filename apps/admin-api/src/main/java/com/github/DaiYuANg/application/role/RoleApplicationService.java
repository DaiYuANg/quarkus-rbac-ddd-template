package com.github.DaiYuANg.application.role;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.parameter.RoleQuery;
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
import com.github.DaiYuANg.security.AuthorizationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RoleApplicationService {
    private final RoleRepository roleRepository;
    private final PermissionGroupRepository permissionGroupRepository;
    private final ViewMapper mapper;
    private final AuthorityVersionService authorityVersionService;
    private final OperationLogService operationLogService;
    private final AuthorizationService authorizationService;

    @Transactional
    public RoleVO createRole(RoleCreationForm form) {
        authorizationService.check("system", "role", "add");
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
        return mapper.toRoleVO(role);
    }

    public PageResult<RoleVO> queryRolePage(RoleQuery query) {
        authorizationService.check("system", "role", "view");
        var slice = roleRepository.page(query);
        return PageResult.of(slice.total(), query.getPageNum(), query.getPageSize(), slice.content().stream().map(mapper::toRoleVO).toList());
    }
    public Optional<RoleVO> getRoleById(Long id) { authorizationService.check("system", "role", "view"); return roleRepository.findByIdOptional(id).map(mapper::toRoleVO); }

    @Transactional
    public RoleVO updateRole(Long id, UpdateRoleForm form) {
        authorizationService.check("system", "role", "edit");
        var role = roleRepository.findByIdOptional(id).orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
        if (form.code() != null && !form.code().equals(role.code) && roleRepository.countByCode(form.code()) > 0) throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "role code already exists");
        if (form.code() != null) role.code = form.code();
        if (form.name() != null) role.name = form.name();
        if (form.status() != null) role.status = form.status();
        if (form.sort() != null) role.sort = form.sort();
        if (form.description() != null) role.description = form.description();
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("role", "update", String.valueOf(id), true, "update role");
        return mapper.toRoleVO(role);
    }

    @Transactional
    public void deleteRole(Long id) { authorizationService.check("system", "role", "delete"); roleRepository.deleteById(id); authorityVersionService.bumpGlobalVersion(); operationLogService.record("role", "delete", String.valueOf(id), true, "delete role"); }
    public Optional<RoleVO> getRoleByName(String name) { authorizationService.check("system", "role", "view"); return roleRepository.findByName(name).map(mapper::toRoleVO); }

    @Transactional
    public void assignPermissionGroups(RoleRefPermissionGroupForm form) {
        authorizationService.checkAny("system.role:edit", "system.role:assign-permission-group");
        var role = roleRepository.findByIdOptional(form.roleId()).orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
        role.permissionGroups.clear();
        if (form.permissionGroupIds() != null) {
            form.permissionGroupIds().forEach(id -> permissionGroupRepository.findByIdOptional(id).ifPresent(role.permissionGroups::add));
        }
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("role", "assign-permission-group", String.valueOf(form.roleId()), true, "assign permission groups");
    }

    public List<RoleVO> getAllRoles() { authorizationService.check("system", "role", "view"); return roleRepository.listAll().stream().map(mapper::toRoleVO).toList(); }
    public long countCode(String code) { return roleRepository.countByCode(code); }
    public long countRole() { return roleRepository.count(); }
}
