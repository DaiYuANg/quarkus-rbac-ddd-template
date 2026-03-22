package com.liangdian.accesscontrol.query;

import com.liangdian.accesscontrol.entity.SysPermission_;
import com.liangdian.accesscontrol.parameter.PermissionQuery;
import com.liangdian.persistence.entity.BaseEntity_;
import com.liangdian.persistence.query.MetamodelSortMapping;
import com.liangdian.persistence.query.MetamodelSorts;
import com.liangdian.persistence.query.QuerySort;
import com.liangdian.persistence.query.SortDirection;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class MetamodelPermissionQueryBuilder {
    private static final Map<String, MetamodelSortMapping> SORT_MAPPING = Map.of(
        "id", new MetamodelSortMapping("id", SysPermission_.id, SortDirection.DESC),
        "name", new MetamodelSortMapping("name", SysPermission_.name, SortDirection.ASC),
        "code", new MetamodelSortMapping("code", SysPermission_.code, SortDirection.ASC),
        "domain", new MetamodelSortMapping("domain", SysPermission_.domain, SortDirection.ASC),
        "resource", new MetamodelSortMapping("resource", SysPermission_.resource, SortDirection.ASC),
        "action", new MetamodelSortMapping("action", SysPermission_.action, SortDirection.ASC),
        "groupCode", new MetamodelSortMapping("groupCode", SysPermission_.groupCode, SortDirection.ASC),
        "createTime", new MetamodelSortMapping("createTime", BaseEntity_.createAt, SortDirection.DESC),
        "updateTime", new MetamodelSortMapping("updateTime", BaseEntity_.updateAt, SortDirection.DESC)
    );

    public PermissionQuerySpec build(PermissionQuery query) {
        return new PermissionQuerySpec(
            new PermissionListFilter(
                normalize(query.getName()),
                normalize(query.getCode()),
                normalize(query.getDomain()),
                normalize(query.getResource()),
                normalize(query.getAction()),
                normalize(query.getGroupCode())
            ),
            MetamodelSorts.resolve(query.getSortBy(), query.getSortDirection(), SORT_MAPPING, QuerySort.desc("id"))
        );
    }

    private String normalize(String value) {
        if (value == null) return null;
        var trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
