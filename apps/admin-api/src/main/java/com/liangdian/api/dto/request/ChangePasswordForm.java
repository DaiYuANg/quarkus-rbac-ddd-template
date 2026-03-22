package com.liangdian.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordForm(
    @NotBlank @Size(min = 6, max = 64) String newPassword
) {}
