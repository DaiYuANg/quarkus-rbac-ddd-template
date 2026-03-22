package com.liangdian.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RoleRefPermissionGroupForm(
    @NotNull Long roleId,
    List<Long> permissionGroupIds
) {}
