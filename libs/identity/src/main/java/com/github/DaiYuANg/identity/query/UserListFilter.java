package com.github.DaiYuANg.identity.query;

import com.github.DaiYuANg.identity.constant.UserStatus;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record UserListFilter(String username, UserStatus userStatus) {}
