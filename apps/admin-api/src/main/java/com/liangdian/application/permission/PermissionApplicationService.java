package com.liangdian.application.permission;

import com.liangdian.accesscontrol.parameter.PermissionQuery;
import com.liangdian.accesscontrol.query.PermissionQueryRepository;
import com.liangdian.accesscontrol.repository.PermissionRepository;
import com.liangdian.api.dto.response.PermissionVO;
import com.liangdian.application.converter.ViewMapper;
import com.liangdian.common.model.PageResult;
import com.liangdian.security.AuthorizationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionApplicationService {
    private final PermissionRepository repository;
    private final PermissionQueryRepository permissionQueryRepository;
    private final ViewMapper mapper;
    private final AuthorizationService authorizationService;

    public Optional<PermissionVO> getPermissionById(Long id) { authorizationService.check("system", "permission", "view"); return repository.findByIdOptional(id).map(mapper::toPermissionVO); }
    public Optional<PermissionVO> getPermissionByName(String name) { authorizationService.check("system", "permission", "view"); return repository.findByName(name).map(mapper::toPermissionVO); }
    public List<PermissionVO> getAllPermissions() { authorizationService.check("system", "permission", "view"); return repository.listAll().stream().map(mapper::toPermissionVO).toList(); }
    public PageResult<PermissionVO> queryPermissionPage(PermissionQuery query) {
        authorizationService.check("system", "permission", "view");
        var slice = permissionQueryRepository.page(query);
        return PageResult.of(slice.total(), query.getPageNum(), query.getPageSize(), slice.content().stream().map(mapper::toPermissionVO).toList());
    }
}
