package com.github.DaiYuANg.cache;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record PermissionCatalogQuery(
    String keyword,
    String name,
    String code,
    String resource,
    String action,
    String groupCode,
    String sortBy,
    String sortDirection,
    int offset,
    int limit) {}
