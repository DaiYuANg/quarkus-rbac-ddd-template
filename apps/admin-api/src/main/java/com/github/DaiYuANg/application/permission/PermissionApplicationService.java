package com.github.DaiYuANg.application.permission;

import com.github.DaiYuANg.accesscontrol.parameter.PermissionQuery;
import com.github.DaiYuANg.accesscontrol.repository.PermissionRepository;
import com.github.DaiYuANg.api.dto.response.PermissionVO;
import com.github.DaiYuANg.application.converter.ViewMapper;
import com.github.DaiYuANg.common.model.PageResult;
import com.github.DaiYuANg.security.AuthorizationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionApplicationService {
    private final PermissionRepository repository;
    private final ViewMapper mapper;
    private final AuthorizationService authorizationService;

    public Optional<PermissionVO> getPermissionById(Long id) { authorizationService.check("permission", "view"); return repository.findByIdOptional(id).map(mapper::toPermissionVO); }
    public Optional<PermissionVO> getPermissionByName(String name) { authorizationService.check("permission", "view"); return repository.findByName(name).map(mapper::toPermissionVO); }
    public List<PermissionVO> getAllPermissions() { authorizationService.check("permission", "view"); return repository.listAll().stream().map(mapper::toPermissionVO).toList(); }
    public PageResult<PermissionVO> queryPermissionPage(PermissionQuery query) {
        authorizationService.check("permission", "view");
        var slice = repository.page(query);
        return PageResult.of(slice.total(), query.getPageNum(), query.getPageSize(), slice.content().stream().map(mapper::toPermissionVO).toList());
    }
}
