package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.accesscontrol.entity.QSysRole;
import com.github.DaiYuANg.accesscontrol.entity.SysRole_;
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
public class RolePageQuery extends ApiPageQuery {
  private static final Map<String, String> SORT_ALIASES =
      new LinkedHashMap<>(
          Map.of(
              "createTime", BaseEntity_.CREATE_AT,
              "updateTime", BaseEntity_.UPDATE_AT));

  private String name;

  public Optional<BooleanExpression> buildCondition(QSysRole role) {
    return Stream.of(keywordCondition(role), likeIfPresent(role.name, name))
        .flatMap(Optional::stream)
        .reduce(BooleanExpression::and);
  }

  public List<OrderSpecifier<?>> buildOrders(QSysRole role) {
    return switch (resolvedSortBy(SORT_ALIASES).orElse(SysRole_.SORT)) {
      case SysRole_.ID -> List.of(role.id.desc());
      case SysRole_.NAME -> List.of(order(role.name, true), role.id.desc());
      case SysRole_.CODE -> List.of(order(role.code, true), role.id.desc());
      case SysRole_.SORT -> List.of(order(role.sort, true), role.id.desc());
      case SysRole_.STATUS -> List.of(order(role.status, true), role.id.desc());
      case BaseEntity_.CREATE_AT -> List.of(order(role.createAt, false), role.id.desc());
      case BaseEntity_.UPDATE_AT -> List.of(order(role.updateAt, false), role.id.desc());
      default -> List.of(role.sort.asc(), role.id.desc());
    };
  }

  private Optional<BooleanExpression> keywordCondition(QSysRole role) {
    return likePattern(getKeyword())
        .map(like -> Expressions.anyOf(role.name.lower().like(like), role.code.lower().like(like)));
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
