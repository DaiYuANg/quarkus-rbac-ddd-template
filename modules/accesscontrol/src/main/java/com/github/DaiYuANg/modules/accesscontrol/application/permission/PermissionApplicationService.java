package com.github.DaiYuANg.modules.accesscontrol.application.permission;

import com.github.DaiYuANg.accesscontrol.query.PermissionPageQuery;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.common.model.ApiPageResult;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionVO;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionVOBuilder;
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

  public Optional<PermissionVO> getPermissionById(Long id) {
    authorizationService.check(Permission.VIEW);
    ensureCatalogLoaded();
    return catalogStore.getById(id).map(this::toVO);
  }

  public Optional<PermissionVO> getPermissionByName(String name) {
    authorizationService.check(Permission.VIEW);
    ensureCatalogLoaded();
    return catalogStore.getByName(name).map(this::toVO);
  }

  public List<PermissionVO> getAllPermissions() {
    authorizationService.check(Permission.VIEW);
    ensureCatalogLoaded();
    return catalogStore.getAll().stream().map(this::toVO).toList();
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
        page.content().stream().map(this::toVO).toList());
  }

  private void ensureCatalogLoaded() {
    if (catalogStore.isEmpty()) {
      catalogLoader.reload();
    }
  }

  private PermissionVO toVO(com.github.DaiYuANg.cache.PermissionCatalogEntry e) {
    return PermissionVOBuilder.builder()
        .id(e.id())
        .name(e.name())
        .code(e.code())
        .resource(e.resource())
        .action(e.action())
        .groupCode(e.groupCode())
        .description(e.description())
        .expression(e.expression())
        .build();
  }
}
