package com.github.DaiYuANg.modules.accesscontrol.application.dto.request;

import com.github.DaiYuANg.identity.constant.UserStatus;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateUserForm(
    @Size(max = 128) String username,
    @Size(max = 64) String mobilePhone,
    @Size(max = 128) String nickname,
    @Size(max = 128) String email,
    UserStatus status,
    List<Long> roleIds) {}
