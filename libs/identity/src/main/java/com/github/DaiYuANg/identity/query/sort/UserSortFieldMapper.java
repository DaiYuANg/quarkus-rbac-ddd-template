package com.github.DaiYuANg.identity.query.sort;

import com.github.DaiYuANg.identity.entity.QSysUser;
import com.github.DaiYuANg.identity.entity.SysUser_;
import com.github.DaiYuANg.persistence.entity.BaseEntity_;
import com.github.DaiYuANg.persistence.query.SortFieldMapper;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import java.util.Map;

/** Maps entity property names (from metamodel) to QueryDSL sort expressions for SysUser. */
public enum UserSortFieldMapper implements SortFieldMapper {
  INSTANCE;

  private static final QSysUser u = new QSysUser("user");
  private static final Map<String, ComparableExpressionBase<?>> MAP =
      Map.ofEntries(
          Map.entry(SysUser_.id.getName(), u.id),
          Map.entry(SysUser_.username.getName(), u.username),
          Map.entry(SysUser_.nickname.getName(), u.nickname),
          Map.entry(SysUser_.email.getName(), u.email),
          Map.entry(SysUser_.mobilePhone.getName(), u.mobilePhone),
          Map.entry(SysUser_.latestSignIn.getName(), u.latestSignIn),
          Map.entry(BaseEntity_.createAt.getName(), u.createAt),
          Map.entry(BaseEntity_.updateAt.getName(), u.updateAt));

  @Override
  public ComparableExpressionBase<?> get(String property) {
    return MAP.get(property);
  }
}
