package com.liangdian.accesscontrol.query;

import com.liangdian.accesscontrol.entity.SysPermissionGroup_;
import com.liangdian.accesscontrol.parameter.PermissionGroupQuery;
import com.liangdian.persistence.entity.BaseEntity_;
import com.liangdian.persistence.query.MetamodelSortMapping;
import com.liangdian.persistence.query.MetamodelSorts;
import com.liangdian.persistence.query.QuerySort;
import com.liangdian.persistence.query.SortDirection;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class MetamodelPermissionGroupQueryBuilder {
    private static final Map<String, MetamodelSortMapping> SORT_MAPPING = Map.of(
        "id", new MetamodelSortMapping("id", SysPermissionGroup_.id, SortDirection.DESC),
        "name", new MetamodelSortMapping("name", SysPermissionGroup_.name, SortDirection.ASC),
        "code", new MetamodelSortMapping("code", SysPermissionGroup_.code, SortDirection.ASC),
        "sort", new MetamodelSortMapping("sort", SysPermissionGroup_.sort, SortDirection.ASC),
        "createTime", new MetamodelSortMapping("createTime", BaseEntity_.createAt, SortDirection.DESC),
        "updateTime", new MetamodelSortMapping("updateTime", BaseEntity_.updateAt, SortDirection.DESC)
    );

    public PermissionGroupQuerySpec build(PermissionGroupQuery query) {
        return new PermissionGroupQuerySpec(
            new PermissionGroupListFilter(normalize(query.getName()), null),
            MetamodelSorts.resolve(query.getSortBy(), query.getSortDirection(), SORT_MAPPING, QuerySort.asc("sort"), QuerySort.desc("id"))
        );
    }

    private String normalize(String value) {
        if (value == null) return null;
        var trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
