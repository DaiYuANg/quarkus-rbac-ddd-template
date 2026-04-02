package com.github.DaiYuANg.modules.accesscontrol.application.mapper;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.projection.RoleListProjection;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.RoleCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdateRoleForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionGroupVO;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.RoleVO;
import java.util.Set;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = MappingConstants.ComponentModel.CDI,
    uses = PermissionGroupVOMapper.class)
public interface RoleVOMapper {
  @Mapping(target = "createAt", expression = "java(null)")
  @Mapping(target = "updateAt", expression = "java(null)")
  @Mapping(target = "permissionGroups", expression = "java(new java.util.LinkedHashSet<>())")
  RoleVO toProjectionVO(RoleListProjection role);

  RoleVO toVO(SysRole role);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "permissionGroups", ignore = true)
  @Mapping(target = "createAt", ignore = true)
  @Mapping(target = "updateAt", ignore = true)
  @Mapping(target = "createBy", ignore = true)
  @Mapping(target = "updateBy", ignore = true)
  @Mapping(
      target = "status",
      expression = "java(form.status() == null ? RoleStatus.ENABLED : form.status())")
  SysRole toEntity(RoleCreationForm form);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "permissionGroups", ignore = true)
  @Mapping(target = "createAt", ignore = true)
  @Mapping(target = "updateAt", ignore = true)
  @Mapping(target = "createBy", ignore = true)
  @Mapping(target = "updateBy", ignore = true)
  void updateEntity(UpdateRoleForm form, @MappingTarget SysRole role);

  @Mapping(target = "id", source = "role.id")
  @Mapping(target = "name", source = "role.name")
  @Mapping(target = "code", source = "role.code")
  @Mapping(target = "status", source = "role.status")
  @Mapping(target = "sort", source = "role.sort")
  @Mapping(target = "createAt", source = "role.createAt")
  @Mapping(target = "updateAt", source = "role.updateAt")
  @Mapping(target = "permissionGroups", source = "permissionGroups")
  RoleVO toVOWithPermissionGroups(SysRole role, Set<PermissionGroupVO> permissionGroups);
}
