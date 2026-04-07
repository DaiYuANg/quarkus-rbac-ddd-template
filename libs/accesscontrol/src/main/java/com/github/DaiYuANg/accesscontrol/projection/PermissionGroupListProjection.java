package com.github.DaiYuANg.accesscontrol.projection;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PermissionGroupListProjection(
    Long id, String name, String description, String code, Integer sort) {}
