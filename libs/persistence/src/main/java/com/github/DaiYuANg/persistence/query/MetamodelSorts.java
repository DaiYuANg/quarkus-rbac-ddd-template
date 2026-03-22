package com.github.DaiYuANg.persistence.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MetamodelSorts {
    private MetamodelSorts() {}

    public static List<QuerySort> resolve(String sortBy, String sortDirection, Map<String, MetamodelSortMapping> mappings, QuerySort... fallback) {
        var resolved = new ArrayList<QuerySort>();
        if (sortBy != null) {
            var mapping = mappings.get(sortBy);
            if (mapping != null) {
                resolved.add(mapping.toSort(sortDirection));
            }
        }
        for (var item : fallback) {
            if (resolved.stream().noneMatch(existing -> existing.property().equals(item.property()))) {
                resolved.add(item);
            }
        }
        return List.copyOf(resolved);
    }
}
