package com.github.DaiYuANg.identity.repository;

import com.github.DaiYuANg.identity.entity.QSysUser;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.persistence.query.BlazeJPAQueryFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
class UserLookupQuerySupport {
  private static final QSysUser u = new QSysUser("user");

  private final BlazeJPAQueryFactory blazeQueryFactory;

  Optional<SysUser> findByUsername(String username) {
    val normalizedUsername = normalize(username);
    if (normalizedUsername == null) {
      return Optional.empty();
    }
    val rows =
        blazeQueryFactory.selectFrom(u).where(u.username.eq(normalizedUsername)).limit(1).fetch();
    return rows.stream().findFirst();
  }

  long countByUsername(String username) {
    val normalizedUsername = normalize(username);
    if (normalizedUsername == null) {
      return 0L;
    }
    return count(u.username.eq(normalizedUsername));
  }

  long countByEmail(String email) {
    val normalizedEmail = normalize(email);
    if (normalizedEmail == null) {
      return 0L;
    }
    return count(u.email.eq(normalizedEmail));
  }

  long countByMobilePhone(String mobilePhone) {
    val normalizedMobilePhone = normalize(mobilePhone);
    if (normalizedMobilePhone == null) {
      return 0L;
    }
    return count(u.mobilePhone.eq(normalizedMobilePhone));
  }

  long countByIdentifier(String identifier) {
    val normalizedIdentifier = normalize(identifier);
    if (normalizedIdentifier == null) {
      return 0L;
    }
    return count(u.identifier.eq(normalizedIdentifier));
  }

  long countUserLoginTotal() {
    Long value =
        blazeQueryFactory
            .<Long>create()
            .from(u)
            .select(u.id.count())
            .where(u.latestSignIn.isNotNull())
            .fetchOne();
    return value == null ? 0L : value;
  }

  private long count(@NonNull com.querydsl.core.types.Predicate predicate) {
    Long value =
        blazeQueryFactory.<Long>create().from(u).select(u.id.count()).where(predicate).fetchOne();
    return value == null ? 0L : value;
  }

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
