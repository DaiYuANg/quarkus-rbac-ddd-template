package com.liangdian.persistence.query;

public interface BasePageQueryRepository<Q, V> extends BaseQueryRepository<Q, V> {
    @Override
    PageSlice<V> page(Q query);
}
