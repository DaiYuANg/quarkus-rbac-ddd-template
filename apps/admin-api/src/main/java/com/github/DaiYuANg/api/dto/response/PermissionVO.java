package com.github.DaiYuANg.api.dto.response;

public record PermissionVO(
    Long id,
    String name,
    String code,
    String domain,
    String resource,
    String action,
    String groupCode,
    String description,
    String expression
) {}
