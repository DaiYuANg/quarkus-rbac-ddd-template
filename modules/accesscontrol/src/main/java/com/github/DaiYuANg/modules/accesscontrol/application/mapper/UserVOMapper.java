package com.github.DaiYuANg.modules.accesscontrol.application.mapper;

import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.projection.UserListProjection;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(
    componentModel = MappingConstants.ComponentModel.CDI,
    uses = RoleVOMapper.class)
public interface UserVOMapper {
  @Mapping(target = "userStatus", source = "userStatus", qualifiedByName = "toUserStatus")
  @Mapping(target = "createAt", expression = "java(null)")
  @Mapping(target = "updateAt", expression = "java(null)")
  @Mapping(target = "roles", expression = "java(new java.util.LinkedHashSet<>())")
  UserVO toProjectionVO(UserListProjection user);

  UserVO toVO(SysUser user);

  @Named("toUserStatus")
  default UserStatus toUserStatus(String value) {
    if (value == null || value.isBlank()) {
      return UserStatus.ENABLED;
    }
    return UserStatus.valueOf(value);
  }
}
