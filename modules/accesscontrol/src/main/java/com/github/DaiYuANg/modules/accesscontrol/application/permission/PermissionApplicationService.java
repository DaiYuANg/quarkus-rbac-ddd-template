package com.github.DaiYuANg.modules.accesscontrol.application.permission;

import com.github.DaiYuANg.accesscontrol.query.PermissionPageQuery;
import com.github.DaiYuANg.cache.PermissionCatalogQueryBuilder;
import com.github.DaiYuANg.cache.PermissionCatalogStore;
import com.github.DaiYuANg.modules.accesscontrol.application.mapper.PermissionVOMapper;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionVO;
import com.github.DaiYuANg.persistence.query.PageResults;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.toolkit4j.data.model.page.PageResult;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionApplicationService {
  private final PermissionCatalogStore catalogStore;
  private final PermissionCatalogLoader catalogLoader;
  private final PermissionVOMapper permissionVOMapper;

  public Optional<PermissionVO> getPermissionById(Long id) {
    ensureCatalogLoaded();
    return catalogStore.getById(id).map(permissionVOMapper::toCatalogVO);
  }

  public Optional<PermissionVO> getPermissionByName(String name) {
    ensureCatalogLoaded();
    return catalogStore.getByName(name).map(permissionVOMapper::toCatalogVO);
  }

  public List<PermissionVO> getAllPermissions() {
    ensureCatalogLoaded();
    return catalogStore.getAll().stream().map(permissionVOMapper::toCatalogVO).toList();
  }

  public PageResult<PermissionVO> queryPermissionPage(PermissionPageQuery query) {
    ensureCatalogLoaded();
    val page =
      catalogStore.findPage(
        PermissionCatalogQueryBuilder.builder()
          .keyword(query.getKeyword())
          .name(query.getName())
          .code(query.getCode())
          .resource(query.getResource())
          .action(query.getAction())
          .groupCode(query.getGroupCode())
          .sortBy(query.getSortBy())
          .sortDirection(query.getSortDirection())
          .offset(query.getOffset())
          .limit(query.getSize())
          .build());
    return PageResults.from(
        page.content().stream().map(permissionVOMapper::toCatalogVO).toList(),
        query,
        page.total());
  }

  private void ensureCatalogLoaded() {
    if (catalogStore.isEmpty()) {
      catalogLoader.reload();
    }
  }
}
