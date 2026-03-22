package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.persistence.query.QuerySort;
import java.util.List;

public record PermissionQuerySpec(PermissionListFilter filter, List<QuerySort> sorts) {}
