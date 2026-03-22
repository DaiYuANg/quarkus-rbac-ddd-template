package com.github.DaiYuANg.persistence.query;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import java.util.Map;

/**
 * Maps entity property names (from JPA metamodel) to QueryDSL sort expressions.
 * Use Hibernate Processor metamodel {@code Entity_.field.getName()} for keys to avoid string typos.
 */
@FunctionalInterface
public interface SortFieldMapper {

  ComparableExpressionBase<?> get(String property);

  static SortFieldMapper of(Map<String, ? extends ComparableExpressionBase<?>> map) {
    return map::get;
  }
}
