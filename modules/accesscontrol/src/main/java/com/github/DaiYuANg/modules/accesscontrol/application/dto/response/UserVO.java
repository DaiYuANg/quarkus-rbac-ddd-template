package com.github.DaiYuANg.modules.accesscontrol.application.dto.response;

import com.github.DaiYuANg.identity.constant.UserStatus;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.time.Instant;
import java.util.Set;

@RecordBuilder
public record UserVO(
    Long id,
    String username,
    String identifier,
    String mobilePhone,
    String nickname,
    String email,
    Instant latestSignIn,
    Instant createAt,
    Instant updateAt,
    UserStatus userStatus,
    Set<RoleVO> roles) {}
