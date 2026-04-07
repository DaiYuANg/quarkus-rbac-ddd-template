package com.github.DaiYuANg.accesscontrol.projection;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PermissionListProjection(
    Long id,
    String name,
    String code,
    String resource,
    String action,
    String groupCode,
    String description,
    String expression) {}
