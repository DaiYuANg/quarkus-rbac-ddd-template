package com.github.DaiYuANg.modules.identity.application.mapper;

import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.modules.identity.application.dto.response.MeResponse;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
    componentModel = MappingConstants.ComponentModel.CDI,
    uses = MeRoleItemMapper.class)
public interface MeResponseMapper {
  @Mapping(target = "id", expression = "java(String.valueOf(user.id))")
  @Mapping(target = "name", source = "displayName")
  @Mapping(target = "email", source = "user.email")
  @Mapping(target = "roles", source = "user.roles")
  @Mapping(target = "permissions", source = "permissions")
  MeResponse fromDbUser(SysUser user, String displayName, Set<String> permissions);

  @Mapping(target = "id", source = "user.username")
  @Mapping(target = "name", source = "displayName")
  @Mapping(target = "email", expression = "java(null)")
  @Mapping(target = "roles", source = "user.roles")
  @Mapping(target = "permissions", source = "permissions")
  MeResponse fromCurrentUser(
      CurrentAuthenticatedUser user, String displayName, Set<String> permissions);
}
