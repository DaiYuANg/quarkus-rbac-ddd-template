package com.github.DaiYuANg.modules.accesscontrol.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserRefRoleForm(@NotNull Long userId, List<Long> roleIds) {}
