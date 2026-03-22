package com.github.DaiYuANg.accesscontrol.projection;

public record RoleListProjection(
    Long id,
    String name,
    String code,
    String status,
    Integer sort
) {}
