package com.github.DaiYuANg.modules.accesscontrol.application.dto.response;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PermissionVO(
    Long id,
    String name,
    String code,
    String resource,
    String action,
    String groupCode,
    String description,
    String expression) {}
