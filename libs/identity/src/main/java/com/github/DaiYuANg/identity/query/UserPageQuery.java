package com.github.DaiYuANg.identity.query;

import com.github.DaiYuANg.common.model.ApiPageQuery;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.QSysUser;
import com.github.DaiYuANg.identity.entity.SysUser_;
import com.github.DaiYuANg.persistence.entity.BaseEntity_;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
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
public class UserPageQuery extends ApiPageQuery {
  private static final Map<String, String> SORT_ALIASES =
      new LinkedHashMap<>(
          Map.of(
              "createTime", BaseEntity_.CREATE_AT,
              "updateTime", BaseEntity_.UPDATE_AT));

  private String username;

  private UserStatus userStatus;

  public Optional<BooleanExpression> buildCondition(QSysUser user) {
    return Stream.of(
            keywordCondition(user),
            likeIfPresent(user.username, username),
            eqIfPresent(user.userStatus, userStatus))
        .flatMap(Optional::stream)
        .reduce(BooleanExpression::and);
  }

  public List<OrderSpecifier<?>> buildOrders(QSysUser user) {
    return switch (resolvedSortBy(SORT_ALIASES).orElse(SysUser_.ID)) {
      case SysUser_.USERNAME -> List.of(order(user.username, true), user.id.desc());
      case SysUser_.NICKNAME -> List.of(order(user.nickname, true), user.id.desc());
      case SysUser_.EMAIL -> List.of(order(user.email, true), user.id.desc());
      case SysUser_.MOBILE_PHONE -> List.of(order(user.mobilePhone, true), user.id.desc());
      case "latestSignIn" -> List.of(order(user.latestSignIn, false), user.id.desc());
      case BaseEntity_.CREATE_AT -> List.of(order(user.createAt, false), user.id.desc());
      case BaseEntity_.UPDATE_AT -> List.of(order(user.updateAt, false), user.id.desc());
      default -> List.of(user.id.desc());
    };
  }

  private Optional<BooleanExpression> keywordCondition(QSysUser user) {
    return likePattern(getKeyword())
        .map(
            like ->
                Expressions.anyOf(
                    user.username.lower().like(like),
                    user.nickname.lower().like(like),
                    user.email.lower().like(like)));
  }

  private Optional<BooleanExpression> likeIfPresent(
      com.querydsl.core.types.dsl.StringPath path, String value) {
    return likePattern(value).map(path.lower()::like);
  }

  private <T> Optional<BooleanExpression> eqIfPresent(SimpleExpression<T> path, T value) {
    return Optional.ofNullable(value).map(path::eq);
  }

  private Optional<String> likePattern(String value) {
    return normalizedValue(value).map(v -> '%' + v.toLowerCase() + '%');
  }

  private OrderSpecifier<?> order(ComparableExpressionBase<?> expression, boolean defaultAsc) {
    val asc = isAscending(defaultAsc);
    return asc ? expression.asc() : expression.desc();
  }
}
