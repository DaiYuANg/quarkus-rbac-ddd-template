package com.github.DaiYuANg.accesscontrol.query.sort;

import com.github.DaiYuANg.accesscontrol.entity.QSysRole;
import com.github.DaiYuANg.accesscontrol.entity.SysRole_;
import com.github.DaiYuANg.persistence.entity.BaseEntity_;
import com.github.DaiYuANg.persistence.query.SortFieldMapper;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import java.util.Map;

/** Maps entity property names (from metamodel) to QueryDSL sort expressions for SysRole. */
public enum RoleSortFieldMapper implements SortFieldMapper {

  INSTANCE;

  private static final QSysRole r = new QSysRole("role");
  private static final Map<String, ComparableExpressionBase<?>> MAP =
      Map.ofEntries(
          Map.entry(SysRole_.id.getName(), r.id),
          Map.entry(SysRole_.name.getName(), r.name),
          Map.entry(SysRole_.code.getName(), r.code),
          Map.entry(SysRole_.sort.getName(), r.sort),
          Map.entry(SysRole_.status.getName(), r.status),
          Map.entry(BaseEntity_.createAt.getName(), r.createAt),
          Map.entry(BaseEntity_.updateAt.getName(), r.updateAt));

  @Override
  public ComparableExpressionBase<?> get(String property) {
    return MAP.get(property);
  }
}
