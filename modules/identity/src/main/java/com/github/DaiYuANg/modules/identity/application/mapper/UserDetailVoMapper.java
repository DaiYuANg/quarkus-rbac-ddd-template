package com.github.DaiYuANg.modules.identity.application.mapper;

import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.modules.identity.application.dto.response.UserDetailVo;
import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
public interface UserDetailVoMapper {
  @Mapping(target = "userid", source = "user.id")
  @Mapping(target = "username", source = "user.username")
  @Mapping(target = "nickname", source = "user.nickname")
  @Mapping(target = "permissions", source = "permissions")
  @Mapping(target = "roleCodes", source = "roleCodes")
  @Mapping(target = "authorityKey", source = "authorityKey")
  UserDetailVo fromDbUser(
      SysUser user, Set<String> permissions, Set<String> roleCodes, String authorityKey);

  @Mapping(target = "userid", source = "userId")
  @Mapping(target = "username", source = "source.user.username")
  @Mapping(target = "nickname", source = "source.nickname")
  @Mapping(target = "permissions", source = "source.permissions")
  @Mapping(target = "roleCodes", source = "source.roleCodes")
  @Mapping(target = "authorityKey", source = "source.authorityKey")
  UserDetailVo fromCurrentUser(CurrentUserProfileSource source);
}
