package com.github.DaiYuANg.modules.accesscontrol.application.dto.request;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleCreationForm(
    @NotBlank @Size(max = 128) String name,
    @NotBlank @Size(max = 128) String code,
    RoleStatus status,
    Integer sort,
    @Size(max = 255) String description
) {}
