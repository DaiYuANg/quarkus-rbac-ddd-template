package com.github.DaiYuANg.persistence.query;

import java.util.List;
import org.toolkit4j.data.model.page.PageResult;

public interface BaseQueryRepository<Q, V> {
  PageResult<V> page(Q query);

  default List<V> list(Q query) {
    return List.copyOf(page(query).normalized().getContent());
  }
}
