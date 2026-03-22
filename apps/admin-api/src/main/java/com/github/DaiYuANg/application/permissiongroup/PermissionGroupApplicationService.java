package com.github.DaiYuANg.application.permissiongroup;

import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.parameter.PermissionGroupQuery;
import com.github.DaiYuANg.accesscontrol.query.PermissionGroupQueryRepository;
import com.github.DaiYuANg.accesscontrol.repository.PermissionGroupRepository;
import com.github.DaiYuANg.accesscontrol.repository.PermissionRepository;
import com.github.DaiYuANg.api.dto.request.PermissionGroupCreationForm;
import com.github.DaiYuANg.api.dto.request.PermissionGroupRefPermissionForm;
import com.github.DaiYuANg.api.dto.request.UpdatePermissionGroupForm;
import com.github.DaiYuANg.api.dto.response.PermissionGroupVO;
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
public class PermissionGroupApplicationService {
    private final PermissionGroupRepository repository;
    private final PermissionRepository permissionRepository;
    private final PermissionGroupQueryRepository permissionGroupQueryRepository;
    private final ViewMapper mapper;
    private final AuthorityVersionService authorityVersionService;
    private final OperationLogService operationLogService;
    private final AuthorizationService authorizationService;

    @Transactional
    public PermissionGroupVO createPermissionGroup(PermissionGroupCreationForm form) {
        authorizationService.check("system", "permission-group", "add");
        if (repository.countByName(form.name()) > 0) throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "permission group name already exists");
        var group = new SysPermissionGroup();
        group.name = form.name();
        group.code = form.code();
        group.sort = form.sort();
        group.description = form.description();
        repository.persist(group);
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("permission-group", "create", form.name(), true, "create permission group");
        return mapper.toPermissionGroupVO(group);
    }

    public Optional<PermissionGroupVO> getPermissionGroupById(Long id) { authorizationService.check("system", "permission-group", "view"); return repository.findByIdOptional(id).map(mapper::toPermissionGroupVO); }

    @Transactional
    public PermissionGroupVO updatePermissionGroup(Long id, UpdatePermissionGroupForm form) {
        authorizationService.check("system", "permission-group", "edit");
        var group = repository.findByIdOptional(id).orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
        if (form.name() != null && !form.name().equals(group.name) && repository.countByName(form.name()) > 0) throw new BizException(ResultCode.DATA_ALREADY_EXISTS, "permission group name already exists");
        if (form.name() != null) group.name = form.name();
        if (form.code() != null) group.code = form.code();
        if (form.sort() != null) group.sort = form.sort();
        if (form.description() != null) group.description = form.description();
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("permission-group", "update", String.valueOf(id), true, "update permission group");
        return mapper.toPermissionGroupVO(group);
    }

    @Transactional
    public void deletePermissionGroup(Long id) { authorizationService.check("system", "permission-group", "delete"); repository.deleteById(id); authorityVersionService.bumpGlobalVersion(); operationLogService.record("permission-group", "delete", String.valueOf(id), true, "delete permission group"); }
    public PageResult<PermissionGroupVO> queryPermissionGroupPage(PermissionGroupQuery query) {
        authorizationService.check("system", "permission-group", "view");
        var slice = permissionGroupQueryRepository.page(query);
        return PageResult.of(slice.total(), query.getPageNum(), query.getPageSize(), slice.content().stream().map(mapper::toPermissionGroupVO).toList());
    }
    public Optional<PermissionGroupVO> getPermissionGroupByName(String name) { authorizationService.check("system", "permission-group", "view"); return repository.findByName(name).map(mapper::toPermissionGroupVO); }

    @Transactional
    public void assignPermissions(PermissionGroupRefPermissionForm form) {
        authorizationService.checkAny("system.permission-group:edit", "system.permission-group:assign-permission");
        var group = repository.findByIdOptional(form.permissionGroupId()).orElseThrow(() -> new BizException(ResultCode.DATA_NOT_FOUND));
        group.permissions.clear();
        if (form.permissionIds() != null) {
            form.permissionIds().forEach(id -> permissionRepository.findByIdOptional(id).ifPresent(group.permissions::add));
        }
        authorityVersionService.bumpGlobalVersion();
        operationLogService.record("permission-group", "assign-permission", String.valueOf(form.permissionGroupId()), true, "assign permissions");
    }

    public List<PermissionGroupVO> getAllPermissionGroups() { authorizationService.check("system", "permission-group", "view"); return repository.listAll().stream().map(mapper::toPermissionGroupVO).toList(); }
    public long countName(String name) { return repository.countByName(name); }
    public long count() { return repository.count(); }
}
