package com.liangdian.accesscontrol.query;

import com.liangdian.accesscontrol.entity.SysRole_;
import com.liangdian.accesscontrol.parameter.RoleQuery;
import com.liangdian.persistence.entity.BaseEntity_;
import com.liangdian.persistence.query.MetamodelSortMapping;
import com.liangdian.persistence.query.MetamodelSorts;
import com.liangdian.persistence.query.QuerySort;
import com.liangdian.persistence.query.SortDirection;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MetamodelRoleQueryBuilder {
    private static final Map<String, MetamodelSortMapping> SORT_MAPPING = Map.of(
        "id", new MetamodelSortMapping("id", SysRole_.id, SortDirection.DESC),
        "name", new MetamodelSortMapping("name", SysRole_.name, SortDirection.ASC),
        "code", new MetamodelSortMapping("code", SysRole_.code, SortDirection.ASC),
        "sort", new MetamodelSortMapping("sort", SysRole_.sort, SortDirection.ASC),
        "status", new MetamodelSortMapping("status", SysRole_.status, SortDirection.ASC),
        "createTime", new MetamodelSortMapping("createTime", BaseEntity_.createAt, SortDirection.DESC),
        "updateTime", new MetamodelSortMapping("updateTime", BaseEntity_.updateAt, SortDirection.DESC)
    );

    public RoleQuerySpec build(RoleQuery query) {
        return new RoleQuerySpec(
            new RoleListFilter(normalize(query.getName()), null),
            resolveSorts(query)
        );
    }

    private List<com.liangdian.persistence.query.QuerySort> resolveSorts(RoleQuery query) {
        return MetamodelSorts.resolve(query.getSortBy(), query.getSortDirection(), SORT_MAPPING, QuerySort.asc("sort"), QuerySort.desc("id"));
    }

    private String normalize(String value) {
        if (value == null) return null;
        var trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
