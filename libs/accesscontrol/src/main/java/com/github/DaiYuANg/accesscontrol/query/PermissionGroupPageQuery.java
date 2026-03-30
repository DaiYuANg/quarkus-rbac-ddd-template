package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.accesscontrol.entity.QSysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup_;
import com.github.DaiYuANg.common.model.ApiPageQuery;
import com.github.DaiYuANg.persistence.entity.BaseEntity_;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Getter
@Setter
public class PermissionGroupPageQuery extends ApiPageQuery {
  private static final Map<String, String> SORT_ALIASES =
      new LinkedHashMap<>(
          Map.of(
              "createTime", BaseEntity_.CREATE_AT,
              "updateTime", BaseEntity_.UPDATE_AT));

  private String name;

  public Optional<BooleanExpression> buildCondition(QSysPermissionGroup group) {
    return Stream.of(keywordCondition(group), likeIfPresent(group.name, name))
        .flatMap(Optional::stream)
        .reduce(BooleanExpression::and);
  }

  public List<OrderSpecifier<?>> buildOrders(QSysPermissionGroup group) {
    return switch (resolvedSortBy(SORT_ALIASES).orElse(SysPermissionGroup_.SORT)) {
      case SysPermissionGroup_.ID -> List.of(group.id.desc());
      case SysPermissionGroup_.NAME -> List.of(order(group.name, true), group.id.desc());
      case SysPermissionGroup_.CODE -> List.of(order(group.code, true), group.id.desc());
      case SysPermissionGroup_.SORT -> List.of(order(group.sort, true), group.id.desc());
      case BaseEntity_.CREATE_AT -> List.of(order(group.createAt, false), group.id.desc());
      case BaseEntity_.UPDATE_AT -> List.of(order(group.updateAt, false), group.id.desc());
      default -> List.of(group.sort.asc(), group.id.desc());
    };
  }

  private Optional<BooleanExpression> keywordCondition(QSysPermissionGroup group) {
    return likePattern(getKeyword())
        .map(like -> Expressions.anyOf(group.name.lower().like(like), group.code.lower().like(like)));
  }

  private Optional<BooleanExpression> likeIfPresent(
      com.querydsl.core.types.dsl.StringPath path, String value) {
    return likePattern(value).map(path.lower()::like);
  }

  private Optional<String> likePattern(String value) {
    return normalizedValue(value).map(v -> '%' + v.toLowerCase() + '%');
  }

  private OrderSpecifier<?> order(ComparableExpressionBase<?> expression, boolean defaultAsc) {
    val asc = isAscending(defaultAsc);
    return asc ? expression.asc() : expression.desc();
  }
}
