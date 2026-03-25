package com.github.DaiYuANg.identity.projection;

import java.time.Instant;

public record UserListProjection(
    Long id,
    String username,
    String nickname,
    String email,
    String mobilePhone,
    String identifier,
    String userStatus,
    Instant latestSignIn) {}
