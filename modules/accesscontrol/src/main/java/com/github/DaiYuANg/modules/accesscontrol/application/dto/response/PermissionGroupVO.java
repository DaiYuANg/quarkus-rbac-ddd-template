package com.github.DaiYuANg.modules.accesscontrol.application.dto.response;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.time.Instant;
import java.util.Set;

@RecordBuilder
public record PermissionGroupVO(
    Long id,
    String name,
    String description,
    String code,
    Integer sort,
    Instant createAt,
    Instant updateAt,
    Set<PermissionVO> permissions) {}
