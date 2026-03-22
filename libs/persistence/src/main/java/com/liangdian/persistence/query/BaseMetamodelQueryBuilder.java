package com.liangdian.persistence.query;

import com.blazebit.persistence.CriteriaBuilder;

public abstract class BaseMetamodelQueryBuilder<Q, S> {
    public abstract S build(Q query);

    protected void applySorts(CriteriaBuilder<?> builder, java.util.List<QuerySort> sorts) {
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
}
