package com.github.DaiYuANg.accesscontrol.query;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PermissionListFilter(
    String name, String code, String resource, String action, String groupCode) {}
