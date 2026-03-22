package com.liangdian.api.dto.response;

import com.liangdian.accesscontrol.constant.RoleStatus;
import java.time.Instant;
import java.util.Set;

public record RoleVO(Long id, String name, String code, RoleStatus status, Integer sort, Instant createAt, Instant updateAt, Set<PermissionGroupVO> permissionGroups) {}
