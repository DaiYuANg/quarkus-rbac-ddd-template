package com.github.DaiYuANg.identity.query;

import com.github.DaiYuANg.identity.constant.UserStatus;

public record UserListFilter(String username, UserStatus userStatus) {}
