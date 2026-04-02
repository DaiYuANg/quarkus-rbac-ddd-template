package com.github.DaiYuANg.identity.projection;

import com.github.DaiYuANg.identity.constant.UserStatus;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.time.Instant;

@RecordBuilder
public record UserListProjection(
    Long id,
    String username,
    String nickname,
    String email,
    String mobilePhone,
    String identifier,
    UserStatus userStatus,
    Instant latestSignIn) {}
