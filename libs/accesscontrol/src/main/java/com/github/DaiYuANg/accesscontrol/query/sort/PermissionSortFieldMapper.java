package com.github.DaiYuANg.accesscontrol.query.sort;

import com.github.DaiYuANg.accesscontrol.entity.QSysPermission;
import com.github.DaiYuANg.accesscontrol.entity.SysPermission_;
import com.github.DaiYuANg.persistence.entity.BaseEntity_;
import com.github.DaiYuANg.persistence.query.SortFieldMapper;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import java.util.Map;

/** Maps entity property names (from metamodel) to QueryDSL sort expressions for SysPermission. */
public enum PermissionSortFieldMapper implements SortFieldMapper {

  INSTANCE;

  private static final QSysPermission p = new QSysPermission("permission");
  private static final Map<String, ComparableExpressionBase<?>> MAP =
      Map.ofEntries(
          Map.entry(SysPermission_.id.getName(), p.id),
          Map.entry(SysPermission_.name.getName(), p.name),
          Map.entry(SysPermission_.code.getName(), p.code),
          Map.entry(SysPermission_.domain.getName(), p.domain),
          Map.entry(SysPermission_.resource.getName(), p.resource),
          Map.entry(SysPermission_.action.getName(), p.action),
          Map.entry(SysPermission_.groupCode.getName(), p.groupCode),
          Map.entry(BaseEntity_.createAt.getName(), p.createAt),
          Map.entry(BaseEntity_.updateAt.getName(), p.updateAt));

  @Override
  public ComparableExpressionBase<?> get(String property) {
    return MAP.get(property);
  }
}
