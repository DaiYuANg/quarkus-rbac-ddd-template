package com.liangdian.accesscontrol.projection;

public record PermissionGroupListProjection(
    Long id,
    String name,
    String description,
    String code,
    Integer sort
) {}
