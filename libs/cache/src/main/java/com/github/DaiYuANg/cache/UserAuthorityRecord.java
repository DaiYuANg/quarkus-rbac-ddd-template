package com.github.DaiYuANg.cache;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Set;

@RecordBuilder
record UserAuthorityRecord(
    Long userId,
    String username,
    String displayName,
    String userType,
    String authorityVersion,
    String roleHash,
    String permissionHash,
    Set<String> roleCodes,
    Set<String> permissionCodes) {}
