package com.liangdian.identity.query;

import com.liangdian.persistence.query.QuerySort;
import java.util.List;

public record UserQuerySpec(UserListFilter filter, List<QuerySort> sorts) {}
