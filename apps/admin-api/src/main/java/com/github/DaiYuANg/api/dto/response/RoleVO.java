package com.github.DaiYuANg.api.dto.response;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import java.time.Instant;
import java.util.Set;

public record RoleVO(Long id, String name, String code, RoleStatus status, Integer sort, Instant createAt, Instant updateAt, Set<PermissionGroupVO> permissionGroups) {}
