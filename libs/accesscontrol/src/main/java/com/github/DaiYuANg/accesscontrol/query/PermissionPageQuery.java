package com.github.DaiYuANg.accesscontrol.query;

import com.github.DaiYuANg.accesscontrol.entity.QSysPermission;
import com.github.DaiYuANg.accesscontrol.entity.SysPermission_;
import com.github.DaiYuANg.common.model.PageReq;
import com.github.DaiYuANg.persistence.entity.BaseEntity_;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
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
public class PermissionPageQuery extends PageReq {
  private static final Map<String, String> SORT_ALIASES =
      new LinkedHashMap<>(
          Map.of(
              "createTime", BaseEntity_.CREATE_AT,
              "updateTime", BaseEntity_.UPDATE_AT));

  private String name;

  private String code;

  private String resource;

  private String action;

  private String groupCode;

  public Optional<BooleanExpression> buildCondition(QSysPermission permission) {
    return Stream.of(
            keywordCondition(permission),
            likeIfPresent(permission.name, name),
            likeIfPresent(permission.code, code),
            eqIfPresent(permission.resource, resource),
            likeIfPresent(permission.action, action),
            eqIfPresent(permission.groupCode, groupCode))
        .flatMap(Optional::stream)
        .reduce(BooleanExpression::and);
  }

  public List<OrderSpecifier<?>> buildOrders(QSysPermission permission) {
    return switch (resolvedSortBy(SORT_ALIASES).orElse(SysPermission_.ID)) {
      case SysPermission_.NAME -> List.of(order(permission.name, true), permission.id.desc());
      case SysPermission_.CODE -> List.of(order(permission.code, true), permission.id.desc());
      case SysPermission_.RESOURCE -> List.of(order(permission.resource, true), permission.id.desc());
      case SysPermission_.ACTION -> List.of(order(permission.action, true), permission.id.desc());
      case SysPermission_.GROUP_CODE -> List.of(order(permission.groupCode, true), permission.id.desc());
      case BaseEntity_.CREATE_AT -> List.of(order(permission.createAt, false), permission.id.desc());
      case BaseEntity_.UPDATE_AT -> List.of(order(permission.updateAt, false), permission.id.desc());
      default -> List.of(permission.id.desc());
    };
  }

  private Optional<BooleanExpression> keywordCondition(QSysPermission permission) {
    return likePattern(getKeyword())
        .map(
            like ->
                Expressions.anyOf(
                    permission.name.lower().like(like),
                    permission.code.lower().like(like),
                    permission.resource.lower().like(like),
                    permission.action.lower().like(like),
                    permission.groupCode.lower().like(like),
                    permission.description.lower().like(like)));
  }

  private Optional<BooleanExpression> likeIfPresent(StringPath path, String value) {
    return likePattern(value).map(path.lower()::like);
  }

  private Optional<BooleanExpression> eqIfPresent(StringPath path, String value) {
    return normalizedValue(value).map(path::eq);
  }

  private Optional<String> likePattern(String value) {
    return normalizedValue(value).map(v -> '%' + v.toLowerCase() + '%');
  }

  private OrderSpecifier<?> order(ComparableExpressionBase<?> expression, boolean defaultAsc) {
    val asc = isAscending(defaultAsc);
    return asc ? expression.asc() : expression.desc();
  }
}
