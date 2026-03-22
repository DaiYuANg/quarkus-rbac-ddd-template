package com.github.DaiYuANg.api.dto.request;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import jakarta.validation.constraints.Size;

public record UpdateRoleForm(
    @Size(max = 128) String name,
    @Size(max = 128) String code,
    RoleStatus status,
    Integer sort,
    @Size(max = 255) String description
) {}
