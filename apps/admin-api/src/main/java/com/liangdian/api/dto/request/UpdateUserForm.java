package com.liangdian.api.dto.request;

import com.liangdian.identity.constant.UserStatus;
import jakarta.validation.constraints.Size;

public record UpdateUserForm(
    @Size(max = 128) String username,
    @Size(max = 64) String mobilePhone,
    @Size(max = 128) String nickname,
    @Size(max = 128) String email,
    UserStatus status
) {}
