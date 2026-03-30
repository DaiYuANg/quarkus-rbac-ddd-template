package com.github.DaiYuANg.identity.query;

import com.github.DaiYuANg.persistence.query.QuerySort;
import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;

@RecordBuilder
public record UserQuerySpec(UserListFilter filter, List<QuerySort> sorts) {}
