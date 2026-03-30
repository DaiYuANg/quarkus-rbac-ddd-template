package com.github.DaiYuANg.persistence.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.val;

public final class MetamodelSorts {
  private MetamodelSorts() {}

  public static List<QuerySort> resolve(
      String sortBy,
      String sortDirection,
      Map<String, MetamodelSortMapping> mappings,
      QuerySort... fallback) {
    val resolved = new ArrayList<QuerySort>();
    if (sortBy != null) {
      val mapping = mappings.get(sortBy);
      if (mapping != null) {
        resolved.add(mapping.toSort(sortDirection));
      }
    }
    Arrays.stream(fallback)
        .filter(item -> resolved.stream().noneMatch(existing -> existing.property().equals(item.property())))
        .forEach(resolved::add);
    return List.copyOf(resolved);
  }
}
