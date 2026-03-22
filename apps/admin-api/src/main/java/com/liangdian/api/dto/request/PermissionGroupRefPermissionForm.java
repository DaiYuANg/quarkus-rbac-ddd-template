package com.liangdian.api.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PermissionGroupRefPermissionForm(
    @NotNull Long permissionGroupId,
    List<Long> permissionIds
) {}
