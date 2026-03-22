package com.github.DaiYuANg.application.converter;

import com.github.DaiYuANg.api.dto.export.PermissionExportRow;
import com.github.DaiYuANg.api.dto.export.PermissionGroupExportRow;
import com.github.DaiYuANg.api.dto.export.RoleExportRow;
import com.github.DaiYuANg.api.dto.export.UserExportRow;
import com.github.DaiYuANg.api.dto.response.PermissionGroupVO;
import com.github.DaiYuANg.api.dto.response.PermissionVO;
import com.github.DaiYuANg.api.dto.response.RoleVO;
import com.github.DaiYuANg.api.dto.response.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExportMapper {

    @Mapping(target = "status", expression = "java(user.userStatus() == null ? null : user.userStatus().name())")
    @Mapping(target = "roleCount", expression = "java(user.roles() == null ? 0 : user.roles().size())")
    UserExportRow toUserExportRow(UserVO user);

    @Mapping(target = "status", expression = "java(role.status() == null ? null : role.status().name())")
    @Mapping(target = "permissionGroupCount", expression = "java(role.permissionGroups() == null ? 0 : role.permissionGroups().size())")
    RoleExportRow toRoleExportRow(RoleVO role);

    PermissionExportRow toPermissionExportRow(PermissionVO permission);

    @Mapping(target = "permissionCount", expression = "java(group.permissions() == null ? 0 : group.permissions().size())")
    PermissionGroupExportRow toPermissionGroupExportRow(PermissionGroupVO group);
}
