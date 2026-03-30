package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup_;
import com.github.DaiYuANg.persistence.entity.BaseEntity_;
import com.github.DaiYuANg.persistence.query.MetamodelSortMapping;
import com.github.DaiYuANg.persistence.query.MetamodelSorts;
import com.github.DaiYuANg.persistence.query.QuerySort;
import com.github.DaiYuANg.persistence.query.SortDirection;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import lombok.val;

@ApplicationScoped
public class MetamodelPermissionGroupQueryBuilder {
  private static final Map<String, MetamodelSortMapping> SORT_MAPPING =
      Map.of(
          "id", new MetamodelSortMapping("id", SysPermissionGroup_.id, SortDirection.DESC),
          "name", new MetamodelSortMapping("name", SysPermissionGroup_.name, SortDirection.ASC),
          "code", new MetamodelSortMapping("code", SysPermissionGroup_.code, SortDirection.ASC),
          "sort", new MetamodelSortMapping("sort", SysPermissionGroup_.sort, SortDirection.ASC),
          "createTime",
              new MetamodelSortMapping("createTime", BaseEntity_.createAt, SortDirection.DESC),
          "updateTime",
              new MetamodelSortMapping("updateTime", BaseEntity_.updateAt, SortDirection.DESC));

  public PermissionGroupQuerySpec build(PermissionGroupPageQuery query) {
    return new PermissionGroupQuerySpec(
        new PermissionGroupListFilter(normalize(query.getName()), null),
        MetamodelSorts.resolve(
            query.getSortBy(),
            query.getSortDirection(),
            SORT_MAPPING,
            QuerySort.asc("sort"),
            QuerySort.desc("id")));
  }

  private String normalize(String value) {
    if (value == null) return null;
    val trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
