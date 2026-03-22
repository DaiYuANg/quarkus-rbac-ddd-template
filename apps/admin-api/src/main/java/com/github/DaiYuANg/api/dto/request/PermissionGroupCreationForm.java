package com.github.DaiYuANg.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissionGroupCreationForm(
    @NotBlank @Size(max = 128) String name,
    @Size(max = 255) String description,
    @NotBlank @Size(max = 128) String code,
    Integer sort
) {}
