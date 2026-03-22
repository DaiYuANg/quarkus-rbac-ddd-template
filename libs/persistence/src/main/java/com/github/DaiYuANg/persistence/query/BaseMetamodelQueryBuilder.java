package com.github.DaiYuANg.persistence.query;

import com.blazebit.persistence.CriteriaBuilder;

public abstract class BaseMetamodelQueryBuilder<Q, S> {
    public abstract S build(Q query);

    protected void applySorts(CriteriaBuilder<?> builder, java.util.List<QuerySort> sorts) {
        if (sorts == null) {
            return;
        }
        for (var sort : sorts) {
            switch (sort.direction()) {
                case ASC -> builder.orderByAsc(sort.property());
                case DESC -> builder.orderByDesc(sort.property());
            }
        }
    }
}
