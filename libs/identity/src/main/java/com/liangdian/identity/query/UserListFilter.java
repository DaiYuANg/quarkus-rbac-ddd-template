package com.liangdian.identity.query;

import com.liangdian.identity.constant.UserStatus;

public record UserListFilter(
    String username,
    UserStatus userStatus
) {}
