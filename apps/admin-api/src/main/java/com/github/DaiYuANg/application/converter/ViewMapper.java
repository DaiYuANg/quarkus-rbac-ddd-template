package com.github.DaiYuANg.application.converter;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import com.github.DaiYuANg.accesscontrol.entity.SysPermission;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.projection.PermissionGroupListProjection;
import com.github.DaiYuANg.accesscontrol.projection.PermissionListProjection;
import com.github.DaiYuANg.accesscontrol.projection.RoleListProjection;
import com.github.DaiYuANg.api.dto.response.PermissionGroupVO;
import com.github.DaiYuANg.api.dto.response.PermissionVO;
import com.github.DaiYuANg.api.dto.response.RoleVO;
import com.github.DaiYuANg.api.dto.response.UserDetailVo;
import com.github.DaiYuANg.api.dto.response.UserVO;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.projection.UserListProjection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ViewMapper {

    @Mapping(target = "userStatus", expression = "java(parseUserStatus(user.userStatus()))")
    @Mapping(target = "roles", expression = "java(new LinkedHashSet<>())")
    UserVO toUserVO(UserListProjection user);

    @Mapping(target = "status", expression = "java(parseRoleStatus(role.status()))")
    @Mapping(target = "permissionGroups", expression = "java(new LinkedHashSet<>())")
    RoleVO toRoleVO(RoleListProjection role);

    PermissionVO toPermissionVO(PermissionListProjection permission);

    @Mapping(target = "permissions", expression = "java(new LinkedHashSet<>())")
    PermissionGroupVO toPermissionGroupVO(PermissionGroupListProjection group);

    @Mapping(target = "roles", source = "roles")
    UserVO toUserVO(SysUser user);

    @Mapping(target = "permissionGroups", source = "permissionGroups")
    RoleVO toRoleVO(SysRole role);

    @Mapping(target = "permissions", source = "permissions")
    PermissionGroupVO toPermissionGroupVO(SysPermissionGroup group);

    PermissionVO toPermissionVO(SysPermission permission);

    default UserDetailVo toUserDetail(SysUser user) {
        var permissions = permissionIdentifiers(user);
        var roleCodes = roleCodes(user);
        return new UserDetailVo(
            user.id,
            user.username,
            user.nickname,
            permissions,
            roleCodes,
            UserDetailVo.encodeAuthorityKey(permissions, roleCodes)
        );
    }

    default UserStatus parseUserStatus(String value) {
        if (value == null || value.isBlank()) {
            return UserStatus.ENABLED;
        }
        return UserStatus.valueOf(value);
    }

    default RoleStatus parseRoleStatus(String value) {
        if (value == null || value.isBlank()) {
            return RoleStatus.ENABLED;
        }
        return RoleStatus.valueOf(value);
    }

    default Set<String> permissionIdentifiers(SysUser user) {
        return user.roles.stream()
            .flatMap(r -> r.permissionGroups.stream())
            .flatMap(g -> g.permissions.stream())
            .map(p -> p.code)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    default Set<String> roleCodes(SysUser user) {
        return user.roles.stream()
            .map(r -> r.code)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
