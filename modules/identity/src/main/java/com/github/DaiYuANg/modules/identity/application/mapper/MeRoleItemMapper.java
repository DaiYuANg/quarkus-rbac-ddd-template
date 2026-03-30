package com.github.DaiYuANg.modules.identity.application.mapper;

import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeRoleItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface MeRoleItemMapper {
  @Mapping(target = "id", expression = "java(String.valueOf(role.id))")
  @Mapping(target = "name", source = "name")
  MeRoleItem toRoleItem(SysRole role);

  default MeRoleItem toRoleItem(String roleCode) {
    return new MeRoleItem(roleCode, roleCode);
  }
}
