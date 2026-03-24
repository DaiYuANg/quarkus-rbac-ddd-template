package com.github.DaiYuANg.modules.accesscontrol.application.dto.request;

import jakarta.validation.constraints.Size;

public record UpdatePermissionGroupForm(
    @Size(max = 128) String name,
    @Size(max = 255) String description,
    @Size(max = 128) String code,
    Integer sort
) {}
