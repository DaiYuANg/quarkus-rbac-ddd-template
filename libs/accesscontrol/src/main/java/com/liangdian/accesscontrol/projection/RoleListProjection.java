package com.liangdian.accesscontrol.projection;

public record RoleListProjection(
    Long id,
    String name,
    String code,
    String status,
    Integer sort
) {}
