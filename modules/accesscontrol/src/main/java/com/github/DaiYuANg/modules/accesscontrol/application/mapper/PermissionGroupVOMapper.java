package com.github.DaiYuANg.modules.accesscontrol.application.mapper;

import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.projection.PermissionGroupListProjection;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionGroupVO;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionVO;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
    componentModel = MappingConstants.ComponentModel.CDI,
    uses = PermissionVOMapper.class)
public interface PermissionGroupVOMapper {
  @Mapping(target = "createAt", expression = "java(null)")
  @Mapping(target = "updateAt", expression = "java(null)")
  @Mapping(target = "permissions", expression = "java(new java.util.LinkedHashSet<>())")
  PermissionGroupVO toProjectionVO(PermissionGroupListProjection group);

  PermissionGroupVO toVO(SysPermissionGroup group);

  @Mapping(target = "id", source = "group.id")
  @Mapping(target = "name", source = "group.name")
  @Mapping(target = "description", source = "group.description")
  @Mapping(target = "code", source = "group.code")
  @Mapping(target = "sort", source = "group.sort")
  @Mapping(target = "createAt", source = "group.createAt")
  @Mapping(target = "updateAt", source = "group.updateAt")
  @Mapping(target = "permissions", source = "permissions")
  PermissionGroupVO toVOWithPermissions(SysPermissionGroup group, Set<PermissionVO> permissions);
}
