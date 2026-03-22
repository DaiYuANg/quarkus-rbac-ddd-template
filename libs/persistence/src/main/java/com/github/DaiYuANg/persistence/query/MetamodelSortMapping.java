package com.github.DaiYuANg.persistence.query;

import jakarta.persistence.metamodel.SingularAttribute;

public record MetamodelSortMapping(String requestField, SingularAttribute<?, ?> attribute, SortDirection defaultDirection) {
    public QuerySort toSort(String requestedDirection) {
        return new QuerySort(attribute.getName(), SortDirection.fromNullable(requestedDirection, defaultDirection));
    }
}
