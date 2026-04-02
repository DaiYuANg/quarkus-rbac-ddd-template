package com.github.DaiYuANg.accesscontrol.projection;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;

public record RoleListProjection(Long id, String name, String code, RoleStatus status, Integer sort) {}
