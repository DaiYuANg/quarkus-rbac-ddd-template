package com.liangdian.persistence.query;

import java.util.List;

public interface BaseQueryRepository<Q, V> {
    PageSlice<V> page(Q query);

    default List<V> list(Q query) {
        return page(query).content();
    }
}
