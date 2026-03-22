package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.persistence.query.QuerySort;
import java.util.List;

public record PermissionGroupQuerySpec(PermissionGroupListFilter filter, List<QuerySort> sorts) {}
