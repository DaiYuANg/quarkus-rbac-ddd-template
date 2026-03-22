package com.liangdian.api.dto.request;

import com.liangdian.identity.constant.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreationForm(
    @NotBlank(message = "username must not be blank")
    @Size(max = 128)
    String username,
    @NotBlank(message = "password must not be blank")
    @Size(min = 6, max = 64)
    String password,
    @Size(max = 64)
    String mobilePhone,
    @Size(max = 128)
    String nickname,
    @Size(max = 128)
    String email,
    UserStatus userStatus
) {}
