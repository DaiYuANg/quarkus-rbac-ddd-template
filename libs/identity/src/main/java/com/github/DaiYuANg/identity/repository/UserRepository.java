package com.github.DaiYuANg.identity.repository;

import com.github.DaiYuANg.accesscontrol.entity.QSysPermission;
import com.github.DaiYuANg.accesscontrol.entity.QSysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.entity.QSysRole;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.identity.entity.QSysUser;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.projection.UserListProjection;
import com.github.DaiYuANg.identity.query.UserPageQuery;
import com.github.DaiYuANg.identity.query.UserQueryRepository;
import com.github.DaiYuANg.identity.view.UserListView;
import com.github.DaiYuANg.persistence.query.BlazeJPAQueryFactory;
import com.github.DaiYuANg.persistence.query.BlazeQueryDSLSupport;
import com.github.DaiYuANg.persistence.repository.BasePanacheCommandRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.toolkit4j.data.model.page.PageResult;

/**
 * Repository for {@code SysUser}.
 *
 * <p>Contains RBAC graph fetch helpers and typed projections (via BlazeJPAQuery + QueryDSL) to
 * avoid N+1 lazy loads in hot paths such as snapshot loading, profiles, and list endpoints.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserRepository extends BasePanacheCommandRepository<SysUser>
    implements UserQueryRepository {

  private static final QSysUser u = new QSysUser("user");
  private static final QSysRole r = new QSysRole("role");
  private static final QSysPermissionGroup g = new QSysPermissionGroup("permissionGroup");
  private static final QSysPermission p = new QSysPermission("permission");

  private final BlazeJPAQueryFactory blazeQueryFactory;
  private final BlazeQueryDSLSupport queryDslSupport;

  public Optional<SysUser> findByUsername(String username) {
    if (username == null || username.isBlank()) {
      return Optional.empty();
    }
    val rows = blazeQueryFactory.selectFrom(u).where(u.username.eq(username)).limit(1).fetch();
    return rows.stream().findFirst();
  }

  /** Loads user with full RBAC graph (roles -> permissionGroups -> permissions) in one query. */
  public Optional<SysUser> findByUsernameWithRbacGraph(String username) {
    if (username == null || username.isBlank()) {
      return Optional.empty();
    }
    val rows =
        blazeQueryFactory
            .selectFrom(u)
            .leftJoin(u.roles, r)
            .fetchJoin()
            .leftJoin(r.permissionGroups, g)
            .fetchJoin()
            .leftJoin(g.permissions, p)
            .fetchJoin()
            .where(u.username.eq(username))
            .distinct()
            .fetch();
    return rows.stream().findFirst();
  }

  /** Loads user with full RBAC graph (roles -> permissionGroups -> permissions) in one query. */
  public Optional<SysUser> findByIdWithRbacGraph(Long id) {
    if (id == null) {
      return Optional.empty();
    }
    val rows =
        blazeQueryFactory
            .selectFrom(u)
            .leftJoin(u.roles, r)
            .fetchJoin()
            .leftJoin(r.permissionGroups, g)
            .fetchJoin()
            .leftJoin(g.permissions, p)
            .fetchJoin()
            .where(u.id.eq(id))
            .distinct()
            .fetch();
    return rows.stream().findFirst();
  }

  /** Loads all users with full RBAC graph (roles -> permissionGroups -> permissions) in one query. */
  public List<SysUser> listAllWithRbacGraph() {
    return blazeQueryFactory
        .selectFrom(u)
        .leftJoin(u.roles, r)
        .fetchJoin()
        .leftJoin(r.permissionGroups, g)
        .fetchJoin()
        .leftJoin(g.permissions, p)
        .fetchJoin()
        .distinct()
        .fetch();
  }

  /** Returns role codes without loading RBAC entity graph. */
  public Set<String> findRoleCodesByUsername(String username) {
    if (username == null || username.isBlank()) {
      return Set.of();
    }
    val rows =
        blazeQueryFactory
            .<String>create()
            .from(u)
            .join(u.roles, r)
            .select(r.code)
            .where(u.username.eq(username))
            .distinct()
            .fetch();
    val result =
        rows.stream()
            .filter(code -> code != null && !code.isBlank())
            .map(String::trim)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    return Set.copyOf(result);
  }

  /** Returns permission codes without loading RBAC entity graph. */
  public Set<String> findPermissionCodesByUsername(String username) {
    if (username == null || username.isBlank()) {
      return Set.of();
    }
    val rows =
        blazeQueryFactory
            .<String>create()
            .from(u)
            .join(u.roles, r)
            .join(r.permissionGroups, g)
            .join(g.permissions, p)
            .select(p.code)
            .where(u.username.eq(username))
            .distinct()
            .fetch();
    val result =
        rows.stream()
            .filter(code -> code != null && !code.isBlank())
            .map(String::trim)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    return Set.copyOf(result);
  }

  public long countUserLoginTotal() {
    Long value =
        blazeQueryFactory
            .<Long>create()
            .from(u)
            .select(u.id.count())
            .where(u.latestSignIn.isNotNull())
            .fetchOne();
    return value == null ? 0L : value;
  }

  public long countByUsername(String username) {
    if (username == null || username.isBlank()) {
      return 0L;
    }
    Long value =
        blazeQueryFactory
            .<Long>create()
            .from(u)
            .select(u.id.count())
            .where(u.username.eq(username))
            .fetchOne();
    return value == null ? 0L : value;
  }

  public long countByEmail(String email) {
    if (email == null || email.isBlank()) {
      return 0L;
    }
    Long value =
        blazeQueryFactory
            .<Long>create()
            .from(u)
            .select(u.id.count())
            .where(u.email.eq(email))
            .fetchOne();
    return value == null ? 0L : value;
  }

  public long countByMobilePhone(String mobilePhone) {
    if (mobilePhone == null || mobilePhone.isBlank()) {
      return 0L;
    }
    Long value =
        blazeQueryFactory
            .<Long>create()
            .from(u)
            .select(u.id.count())
            .where(u.mobilePhone.eq(mobilePhone))
            .fetchOne();
    return value == null ? 0L : value;
  }

  public long countByIdentifier(String identifier) {
    if (identifier == null || identifier.isBlank()) {
      return 0L;
    }
    Long value =
        blazeQueryFactory
            .<Long>create()
            .from(u)
            .select(u.id.count())
            .where(u.identifier.eq(identifier))
            .fetchOne();
    return value == null ? 0L : value;
  }

  @Override
  public PageResult<UserListProjection> page(UserPageQuery query) {
    val blazeQuery = blazeQueryFactory.selectFrom(u);
    query.buildCondition(u).ifPresent(blazeQuery::where);
    query.buildOrders(u).forEach(blazeQuery::orderBy);
    val page =
        queryDslSupport.executeWithEntityView(
            blazeQuery, UserListView.class, query.offset(), query.getPageSize(), this::toProjection);
    return BlazeQueryDSLSupport.toPageResult(page, query);
  }

  private UserListProjection toProjection(UserListView view) {
    return new UserListProjection(
        view.getId(),
        view.getUsername(),
        view.getNickname(),
        view.getEmail(),
        view.getMobilePhone(),
        view.getIdentifier(),
        view.getUserStatus() == null ? null : view.getUserStatus().name(),
        view.getLatestSignIn());
  }

  @Override
  protected ResultCode notFoundCode() {
    return ResultCode.DATA_NOT_FOUND;
  }
}
