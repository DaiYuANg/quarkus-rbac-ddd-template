package com.github.DaiYuANg.cache;

import java.util.List;

public record PermissionCatalogPage(List<PermissionCatalogEntry> content, long total) {}
