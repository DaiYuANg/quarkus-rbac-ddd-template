package com.github.DaiYuANg.accesscontrol.projection;

public record PermissionListProjection(
    Long id,
    String name,
    String code,
    String resource,
    String action,
    String groupCode,
    String description,
    String expression
) {}
