package com.liangdian.accesscontrol.query;

import com.liangdian.persistence.query.QuerySort;
import java.util.List;

public record PermissionQuerySpec(PermissionListFilter filter, List<QuerySort> sorts) {}
