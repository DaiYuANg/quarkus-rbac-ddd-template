package com.github.DaiYuANg.identity.query;

import com.github.DaiYuANg.persistence.query.QuerySort;
import java.util.List;

public record UserQuerySpec(UserListFilter filter, List<QuerySort> sorts) {}
