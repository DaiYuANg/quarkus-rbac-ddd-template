package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.accesscontrol.entity.SysRole_;
import com.github.DaiYuANg.persistence.entity.BaseEntity_;
import com.github.DaiYuANg.persistence.query.MetamodelSortMapping;
import com.github.DaiYuANg.persistence.query.MetamodelSorts;
import com.github.DaiYuANg.persistence.query.QuerySort;
import com.github.DaiYuANg.persistence.query.SortDirection;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MetamodelRoleQueryBuilder {
  private static final Map<String, MetamodelSortMapping> SORT_MAPPING =
      Map.of(
          "id", new MetamodelSortMapping("id", SysRole_.id, SortDirection.DESC),
          "name", new MetamodelSortMapping("name", SysRole_.name, SortDirection.ASC),
          "code", new MetamodelSortMapping("code", SysRole_.code, SortDirection.ASC),
          "sort", new MetamodelSortMapping("sort", SysRole_.sort, SortDirection.ASC),
          "status", new MetamodelSortMapping("status", SysRole_.status, SortDirection.ASC),
          "createTime",
              new MetamodelSortMapping("createTime", BaseEntity_.createAt, SortDirection.DESC),
          "updateTime",
              new MetamodelSortMapping("updateTime", BaseEntity_.updateAt, SortDirection.DESC));

  public RoleQuerySpec build(RolePageQuery query) {
    return new RoleQuerySpec(
        new RoleListFilter(normalize(query.getName()), null), resolveSorts(query));
  }

  private List<com.github.DaiYuANg.persistence.query.QuerySort> resolveSorts(
      RolePageQuery query) {
    return MetamodelSorts.resolve(
        query.getSortBy(),
        query.getSortDirection(),
        SORT_MAPPING,
        QuerySort.asc("sort"),
        QuerySort.desc("id"));
  }

  private String normalize(String value) {
    if (value == null) return null;
    var trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
