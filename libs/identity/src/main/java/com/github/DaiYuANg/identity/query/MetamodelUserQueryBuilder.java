package com.github.DaiYuANg.identity.query;

import com.github.DaiYuANg.identity.entity.SysUser_;
import com.github.DaiYuANg.identity.parameter.UserQuery;
import com.github.DaiYuANg.persistence.entity.BaseEntity_;
import com.github.DaiYuANg.persistence.query.MetamodelSortMapping;
import com.github.DaiYuANg.persistence.query.MetamodelSorts;
import com.github.DaiYuANg.persistence.query.QuerySort;
import com.github.DaiYuANg.persistence.query.SortDirection;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MetamodelUserQueryBuilder {
  private static final Map<String, MetamodelSortMapping> SORT_MAPPING =
      Map.of(
          "id", new MetamodelSortMapping("id", SysUser_.id, SortDirection.DESC),
          "username", new MetamodelSortMapping("username", SysUser_.username, SortDirection.ASC),
          "nickname", new MetamodelSortMapping("nickname", SysUser_.nickname, SortDirection.ASC),
          "email", new MetamodelSortMapping("email", SysUser_.email, SortDirection.ASC),
          "mobilePhone",
              new MetamodelSortMapping("mobilePhone", SysUser_.mobilePhone, SortDirection.ASC),
          "latestSignIn",
              new MetamodelSortMapping("latestSignIn", SysUser_.latestSignIn, SortDirection.DESC),
          "createTime",
              new MetamodelSortMapping("createTime", BaseEntity_.createAt, SortDirection.DESC),
          "updateTime",
              new MetamodelSortMapping("updateTime", BaseEntity_.updateAt, SortDirection.DESC));

  public UserQuerySpec build(UserQuery query) {
    return new UserQuerySpec(
        new UserListFilter(normalize(query.getUsername()), query.getUserStatus()),
        resolveSorts(query));
  }

  private List<QuerySort> resolveSorts(UserQuery query) {
    return MetamodelSorts.resolve(
        query.getSortBy(), query.getSortDirection(), SORT_MAPPING, QuerySort.desc("id"));
  }

  private String normalize(String value) {
    if (value == null) return null;
    var trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
