package com.github.DaiYuANg.modules.identity.application.mapper;

import com.github.DaiYuANg.security.identity.CurrentAuthenticatedUser;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Set;

@RecordBuilder
public record CurrentUserProfileSource(
    CurrentAuthenticatedUser user,
    String nickname,
    Set<String> permissions,
    Set<String> roleCodes,
    String authorityKey,
    Long userId) {}
