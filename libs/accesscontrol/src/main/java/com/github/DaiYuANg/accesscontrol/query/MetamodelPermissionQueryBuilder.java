package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.accesscontrol.entity.SysPermission_;
import com.github.DaiYuANg.accesscontrol.parameter.PermissionQuery;
import com.github.DaiYuANg.persistence.entity.BaseEntity_;
import com.github.DaiYuANg.persistence.query.MetamodelSortMapping;
import com.github.DaiYuANg.persistence.query.MetamodelSorts;
import com.github.DaiYuANg.persistence.query.QuerySort;
import com.github.DaiYuANg.persistence.query.SortDirection;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class MetamodelPermissionQueryBuilder {
  private static final Map<String, MetamodelSortMapping> SORT_MAPPING =
      Map.of(
          "id", new MetamodelSortMapping("id", SysPermission_.id, SortDirection.DESC),
          "name", new MetamodelSortMapping("name", SysPermission_.name, SortDirection.ASC),
          "code", new MetamodelSortMapping("code", SysPermission_.code, SortDirection.ASC),
          "resource",
              new MetamodelSortMapping("resource", SysPermission_.resource, SortDirection.ASC),
          "action", new MetamodelSortMapping("action", SysPermission_.action, SortDirection.ASC),
          "groupCode",
              new MetamodelSortMapping("groupCode", SysPermission_.groupCode, SortDirection.ASC),
          "createTime",
              new MetamodelSortMapping("createTime", BaseEntity_.createAt, SortDirection.DESC),
          "updateTime",
              new MetamodelSortMapping("updateTime", BaseEntity_.updateAt, SortDirection.DESC));

  public PermissionQuerySpec build(PermissionQuery query) {
    return new PermissionQuerySpec(
        new PermissionListFilter(
            normalize(query.getName()),
            normalize(query.getCode()),
            normalize(query.getResource()),
            normalize(query.getAction()),
            normalize(query.getGroupCode())),
        MetamodelSorts.resolve(
            query.getSortBy(), query.getSortDirection(), SORT_MAPPING, QuerySort.desc("id")));
  }

  private String normalize(String value) {
    if (value == null) return null;
    var trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
