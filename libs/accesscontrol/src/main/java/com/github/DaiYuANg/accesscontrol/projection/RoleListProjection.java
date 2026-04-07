package com.github.DaiYuANg.accesscontrol.projection;

import com.github.DaiYuANg.accesscontrol.constant.RoleStatus;
import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record RoleListProjection(Long id, String name, String code, RoleStatus status, Integer sort) {
}
