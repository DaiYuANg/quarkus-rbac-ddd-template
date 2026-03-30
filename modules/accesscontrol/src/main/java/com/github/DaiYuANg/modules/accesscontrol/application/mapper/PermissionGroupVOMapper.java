package com.github.DaiYuANg.modules.accesscontrol.application.mapper;

import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.projection.PermissionGroupListProjection;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.PermissionGroupCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdatePermissionGroupForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionGroupVO;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.PermissionVO;
import java.util.Set;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = MappingConstants.ComponentModel.CDI,
    uses = PermissionVOMapper.class)
public interface PermissionGroupVOMapper {
  @Mapping(target = "createAt", expression = "java(null)")
  @Mapping(target = "updateAt", expression = "java(null)")
  @Mapping(target = "permissions", expression = "java(new java.util.LinkedHashSet<>())")
  PermissionGroupVO toProjectionVO(PermissionGroupListProjection group);

  PermissionGroupVO toVO(SysPermissionGroup group);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "permissions", ignore = true)
  @Mapping(target = "createAt", ignore = true)
  @Mapping(target = "updateAt", ignore = true)
  @Mapping(target = "createBy", ignore = true)
  @Mapping(target = "updateBy", ignore = true)
  SysPermissionGroup toEntity(PermissionGroupCreationForm form);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "permissions", ignore = true)
  @Mapping(target = "createAt", ignore = true)
  @Mapping(target = "updateAt", ignore = true)
  @Mapping(target = "createBy", ignore = true)
  @Mapping(target = "updateBy", ignore = true)
  void updateEntity(UpdatePermissionGroupForm form, @MappingTarget SysPermissionGroup group);

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
