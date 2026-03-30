package com.github.DaiYuANg.modules.accesscontrol.application.dto.response;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.time.Instant;
import java.util.Set;

@RecordBuilder
public record RoleVO(
    Long id,
    String name,
    String code,
    RoleStatus status,
    Integer sort,
    Instant createAt,
    Instant updateAt,
    Set<PermissionGroupVO> permissionGroups) {}
