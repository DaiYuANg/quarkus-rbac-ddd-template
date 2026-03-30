package com.github.DaiYuANg.modules.accesscontrol.application.mapper;

import com.github.DaiYuANg.accesscontrol.entity.SysPermission;
import com.github.DaiYuANg.accesscontrol.projection.PermissionListProjection;
import com.github.DaiYuANg.cache.PermissionCatalogEntry;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface PermissionVOMapper {
  PermissionVO toVO(SysPermission permission);

  PermissionVO toCatalogVO(PermissionCatalogEntry entry);

  PermissionVO toProjectionVO(PermissionListProjection projection);
}
