package com.github.DaiYuANg.persistence.query;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.function.Function;

public abstract class BaseBlazeQueryRepository<E, Q, V> implements BasePageQueryRepository<Q, V> {
    @Inject protected EntityManager entityManager;
    @Inject protected CriteriaBuilderFactory criteriaBuilderFactory;

    protected abstract Class<E> entityClass();

    protected CriteriaBuilder<E> newCriteriaBuilder(String alias) {
        return criteriaBuilderFactory.create(entityManager, entityClass()).from(entityClass(), alias);
    }

    protected void applySorts(CriteriaBuilder<E> builder, List<QuerySort> sorts) {
        if (sorts == null) {
            return;
        }
        for (var sort : sorts) {
            if (sort.direction() == SortDirection.ASC) {
                builder.orderByAsc(sort.property());
            } else {
                builder.orderByDesc(sort.property());
            }
        }
    }

    protected <T> PageSlice<V> page(PagedList<T> page, Function<T, V> mapper) {
        return new PageSlice<>(page.stream().map(mapper).toList(), page.getTotalSize());
    }
}
