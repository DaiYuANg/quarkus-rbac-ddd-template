package com.github.DaiYuANg.modules.accesscontrol.application.mapper;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.projection.RoleListProjection;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionGroupVO;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.RoleVO;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(
    componentModel = MappingConstants.ComponentModel.CDI,
    uses = PermissionGroupVOMapper.class)
public interface RoleVOMapper {
  @Mapping(target = "status", source = "status", qualifiedByName = "toRoleStatus")
  @Mapping(target = "createAt", expression = "java(null)")
  @Mapping(target = "updateAt", expression = "java(null)")
  @Mapping(target = "permissionGroups", expression = "java(new java.util.LinkedHashSet<>())")
  RoleVO toProjectionVO(RoleListProjection role);

  RoleVO toVO(SysRole role);

  @Mapping(target = "id", source = "role.id")
  @Mapping(target = "name", source = "role.name")
  @Mapping(target = "code", source = "role.code")
  @Mapping(target = "status", source = "role.status")
  @Mapping(target = "sort", source = "role.sort")
  @Mapping(target = "createAt", source = "role.createAt")
  @Mapping(target = "updateAt", source = "role.updateAt")
  @Mapping(target = "permissionGroups", source = "permissionGroups")
  RoleVO toVOWithPermissionGroups(SysRole role, Set<PermissionGroupVO> permissionGroups);

  @Named("toRoleStatus")
  default RoleStatus toRoleStatus(String value) {
    if (value == null || value.isBlank()) {
      return RoleStatus.ENABLED;
    }
    return RoleStatus.valueOf(value);
  }
}
