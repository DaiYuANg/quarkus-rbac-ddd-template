package com.github.DaiYuANg.modules.accesscontrol.application.permission;

import com.github.DaiYuANg.accesscontrol.parameter.PermissionQuery;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.common.model.PageResult;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionVO;
import com.github.DaiYuANg.security.authorization.AuthorizationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionApplicationService {
  private final PermissionCatalogStore catalogStore;
  private final PermissionCatalogLoader catalogLoader;
  private final AuthorizationService authorizationService;

  public Optional<PermissionVO> getPermissionById(Long id) {
    authorizationService.check("permission", "view");
    ensureCatalogLoaded();
    return catalogStore.getById(id).map(this::toVO);
  }

  public Optional<PermissionVO> getPermissionByName(String name) {
    authorizationService.check("permission", "view");
    ensureCatalogLoaded();
    return catalogStore.getByName(name).map(this::toVO);
  }

  public List<PermissionVO> getAllPermissions() {
    authorizationService.check("permission", "view");
    ensureCatalogLoaded();
    return catalogStore.getAll().stream().map(this::toVO).toList();
  }

  public PageResult<PermissionVO> queryPermissionPage(PermissionQuery query) {
    authorizationService.check("permission", "view");
    ensureCatalogLoaded();
    var page =
        catalogStore.findPage(
            query.getKeyword(),
            query.getName(),
            query.getCode(),
            query.getResource(),
            query.getAction(),
            query.getGroupCode(),
            query.getSortBy(),
            query.getSortDirection(),
            query.offset(),
            query.getPageSize());
    return PageResult.of(
        page.total(),
        query.getPageNum(),
        query.getPageSize(),
        page.content().stream().map(this::toVO).toList());
  }

  private void ensureCatalogLoaded() {
    if (catalogStore.isEmpty()) {
      catalogLoader.reload();
    }
  }

  private PermissionVO toVO(com.github.DaiYuANg.cache.PermissionCatalogEntry e) {
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
