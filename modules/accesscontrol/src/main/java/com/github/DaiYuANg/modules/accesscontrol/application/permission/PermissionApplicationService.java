package com.github.DaiYuANg.modules.accesscontrol.application.permission;

import com.github.DaiYuANg.accesscontrol.query.PermissionPageQuery;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.common.model.ApiPageResult;
import com.github.DaiYuANg.modules.accesscontrol.application.mapper.PermissionVOMapper;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionVO;
import com.github.DaiYuANg.security.authorization.AuthorizationService;
import com.github.DaiYuANg.security.authorization.RbacPermissionCodes.Permission;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionApplicationService {
  private final PermissionCatalogStore catalogStore;
  private final PermissionCatalogLoader catalogLoader;
  private final AuthorizationService authorizationService;
  private final PermissionVOMapper permissionVOMapper;

  public Optional<PermissionVO> getPermissionById(Long id) {
    authorizationService.check(Permission.VIEW);
    ensureCatalogLoaded();
    return catalogStore.getById(id).map(permissionVOMapper::toCatalogVO);
  }

  public Optional<PermissionVO> getPermissionByName(String name) {
    authorizationService.check(Permission.VIEW);
    ensureCatalogLoaded();
    return catalogStore.getByName(name).map(permissionVOMapper::toCatalogVO);
  }

  public List<PermissionVO> getAllPermissions() {
    authorizationService.check(Permission.VIEW);
    ensureCatalogLoaded();
    return catalogStore.getAll().stream().map(permissionVOMapper::toCatalogVO).toList();
  }

  public ApiPageResult<PermissionVO> queryPermissionPage(PermissionPageQuery query) {
    authorizationService.check(Permission.VIEW);
    ensureCatalogLoaded();
    val page =
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
    return ApiPageResult.of(
        page.total(),
        query.getPageNum(),
        query.getPageSize(),
        page.content().stream().map(permissionVOMapper::toCatalogVO).toList());
  }

  private void ensureCatalogLoaded() {
    if (catalogStore.isEmpty()) {
      catalogLoader.reload();
    }
  }
}
