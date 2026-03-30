package com.github.DaiYuANg.modules.accesscontrol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordForm(@NotBlank @Size(min = 6, max = 64) String newPassword) {}
