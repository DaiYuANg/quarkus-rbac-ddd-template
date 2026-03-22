package com.github.DaiYuANg.accesscontrol.query.sort;

import com.github.DaiYuANg.accesscontrol.entity.QSysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup_;
import com.github.DaiYuANg.persistence.entity.BaseEntity_;
import com.github.DaiYuANg.persistence.query.SortFieldMapper;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import java.util.Map;

/** Maps entity property names (from metamodel) to QueryDSL sort expressions for SysPermissionGroup. */
public enum PermissionGroupSortFieldMapper implements SortFieldMapper {

  INSTANCE;

  private static final QSysPermissionGroup g = new QSysPermissionGroup("permissionGroup");
  private static final Map<String, ComparableExpressionBase<?>> MAP =
      Map.ofEntries(
          Map.entry(SysPermissionGroup_.id.getName(), g.id),
          Map.entry(SysPermissionGroup_.name.getName(), g.name),
          Map.entry(SysPermissionGroup_.code.getName(), g.code),
          Map.entry(SysPermissionGroup_.sort.getName(), g.sort),
          Map.entry(BaseEntity_.createAt.getName(), g.createAt),
          Map.entry(BaseEntity_.updateAt.getName(), g.updateAt));

  @Override
  public ComparableExpressionBase<?> get(String property) {
    return MAP.get(property);
  }
}
