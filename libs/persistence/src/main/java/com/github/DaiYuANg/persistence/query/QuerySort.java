package com.github.DaiYuANg.persistence.query;

public record QuerySort(String property, SortDirection direction) {
    public static QuerySort asc(String property) {
        return new QuerySort(property, SortDirection.ASC);
    }

    public static QuerySort desc(String property) {
        return new QuerySort(property, SortDirection.DESC);
    }
}
