package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.persistence.query.QuerySort;
import java.util.List;

public record RoleQuerySpec(RoleListFilter filter, List<QuerySort> sorts) {}
