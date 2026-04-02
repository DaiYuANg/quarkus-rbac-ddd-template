package com.github.DaiYuANg.modules.accesscontrol.application.mapper;

import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.projection.UserListProjection;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UpdateUserForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.request.UserCreationForm;
import com.github.DaiYuANg.modules.accesscontrol.application.dto.response.UserVO;
import com.github.DaiYuANg.security.auth.PasswordHasher;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = MappingConstants.ComponentModel.CDI,
    uses = RoleVOMapper.class)
public interface UserVOMapper {
  @Mapping(target = "createAt", expression = "java(null)")
  @Mapping(target = "updateAt", expression = "java(null)")
  @Mapping(target = "roles", expression = "java(new java.util.LinkedHashSet<>())")
  UserVO toProjectionVO(UserListProjection user);

  UserVO toVO(SysUser user);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "identifier", ignore = true)
  @Mapping(target = "latestSignIn", ignore = true)
  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "createAt", ignore = true)
  @Mapping(target = "updateAt", ignore = true)
  @Mapping(target = "createBy", ignore = true)
  @Mapping(target = "updateBy", ignore = true)
  @Mapping(target = "password", expression = "java(passwordHasher.hash(form.password()))")
  @Mapping(
      target = "userStatus",
      expression = "java(form.userStatus() == null ? UserStatus.ENABLED : form.userStatus())")
  SysUser toEntity(UserCreationForm form, @Context PasswordHasher passwordHasher);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "identifier", ignore = true)
  @Mapping(target = "latestSignIn", ignore = true)
  @Mapping(target = "roles", ignore = true)
  @Mapping(target = "createAt", ignore = true)
  @Mapping(target = "updateAt", ignore = true)
  @Mapping(target = "createBy", ignore = true)
  @Mapping(target = "updateBy", ignore = true)
  @Mapping(target = "userStatus", source = "status")
  void updateEntity(UpdateUserForm form, @MappingTarget SysUser user);
}
