package com.liangdian.accesscontrol.query;

import com.liangdian.persistence.query.QuerySort;
import java.util.List;

public record RoleQuerySpec(RoleListFilter filter, List<QuerySort> sorts) {}
