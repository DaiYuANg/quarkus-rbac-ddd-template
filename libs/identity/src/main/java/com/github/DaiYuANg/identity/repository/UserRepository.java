package com.github.DaiYuANg.identity.repository;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.QSysUser;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.entity.SysUser_;
import com.github.DaiYuANg.accesscontrol.entity.QSysRole;
import com.github.DaiYuANg.accesscontrol.entity.QSysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.entity.QSysPermission;
import com.github.DaiYuANg.identity.parameter.UserQuery;
import com.github.DaiYuANg.identity.projection.UserListProjection;
import com.github.DaiYuANg.identity.query.MetamodelUserQueryBuilder;
import com.github.DaiYuANg.identity.query.UserQueryRepository;
import com.github.DaiYuANg.identity.query.sort.UserSortFieldMapper;
import com.github.DaiYuANg.identity.view.UserListView;
import com.github.DaiYuANg.persistence.query.BlazeQueryDSLSupport;
import com.github.DaiYuANg.persistence.query.PageSlice;
import com.github.DaiYuANg.persistence.repository.BasePanacheCommandRepository;
import com.querydsl.core.types.dsl.Expressions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserRepository extends BasePanacheCommandRepository<SysUser>
    implements UserQueryRepository {

  private static final QSysUser u = new QSysUser("user");
  private static final QSysRole r = new QSysRole("role");
  private static final QSysPermissionGroup g = new QSysPermissionGroup("permissionGroup");
  private static final QSysPermission p = new QSysPermission("permission");

  private final EntityManager entityManager;
  private final CriteriaBuilderFactory criteriaBuilderFactory;
  private final BlazeQueryDSLSupport queryDslSupport;
  private final MetamodelUserQueryBuilder queryBuilder;

  public Optional<SysUser> findByUsername(String username) {
    if (username == null || username.isBlank()) {
      return Optional.empty();
    }
    var rows =
        new BlazeJPAQuery<SysUser>(entityManager, criteriaBuilderFactory)
            .from(u)
            .select(u)
            .where(u.username.eq(username))
            .limit(1)
            .fetch();
    return rows.stream().findFirst();
  }

  /** Loads user with full RBAC graph (roles -> permissionGroups -> permissions) in one query. */
  public Optional<SysUser> findByUsernameWithRbacGraph(String username) {
    if (username == null || username.isBlank()) {
      return Optional.empty();
    }
    var rows =
        new BlazeJPAQuery<SysUser>(entityManager, criteriaBuilderFactory)
            .from(u)
            .leftJoin(u.roles, r).fetchJoin()
            .leftJoin(r.permissionGroups, g).fetchJoin()
            .leftJoin(g.permissions, p).fetchJoin()
            .select(u)
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
    var rows =
        new BlazeJPAQuery<SysUser>(entityManager, criteriaBuilderFactory)
            .from(u)
            .leftJoin(u.roles, r).fetchJoin()
            .leftJoin(r.permissionGroups, g).fetchJoin()
            .leftJoin(g.permissions, p).fetchJoin()
            .select(u)
            .where(u.id.eq(id))
            .distinct()
            .fetch();
    return rows.stream().findFirst();
  }

  /** Loads all users with full RBAC graph (roles -> permissionGroups -> permissions) in one query. */
  public List<SysUser> listAllWithRbacGraph() {
    return new BlazeJPAQuery<SysUser>(entityManager, criteriaBuilderFactory)
        .from(u)
        .leftJoin(u.roles, r).fetchJoin()
        .leftJoin(r.permissionGroups, g).fetchJoin()
        .leftJoin(g.permissions, p).fetchJoin()
        .select(u)
        .distinct()
        .fetch();
  }

  /** Returns role codes without loading RBAC entity graph. */
  public Set<String> findRoleCodesByUsername(String username) {
    if (username == null || username.isBlank()) {
      return Set.of();
    }
    var rows =
        new BlazeJPAQuery<String>(entityManager, criteriaBuilderFactory)
            .from(u)
            .join(u.roles, r)
            .select(r.code)
            .where(u.username.eq(username))
            .distinct()
            .fetch();
    var result = new LinkedHashSet<String>();
    for (var code : rows) {
      if (code != null && !code.isBlank()) {
        result.add(code.trim());
      }
    }
    return Set.copyOf(result);
  }

  /** Returns permission codes without loading RBAC entity graph. */
  public Set<String> findPermissionCodesByUsername(String username) {
    if (username == null || username.isBlank()) {
      return Set.of();
    }
    var rows =
        new BlazeJPAQuery<String>(entityManager, criteriaBuilderFactory)
            .from(u)
            .join(u.roles, r)
            .join(r.permissionGroups, g)
            .join(g.permissions, p)
            .select(p.code)
            .where(u.username.eq(username))
            .distinct()
            .fetch();
    var result = new LinkedHashSet<String>();
    for (var code : rows) {
      if (code != null && !code.isBlank()) {
        result.add(code.trim());
      }
    }
    return Set.copyOf(result);
  }

  public long countUserLoginTotal() {
    Long value =
        new BlazeJPAQuery<Long>(entityManager, criteriaBuilderFactory)
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
        new BlazeJPAQuery<Long>(entityManager, criteriaBuilderFactory)
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
        new BlazeJPAQuery<Long>(entityManager, criteriaBuilderFactory)
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
        new BlazeJPAQuery<Long>(entityManager, criteriaBuilderFactory)
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
        new BlazeJPAQuery<Long>(entityManager, criteriaBuilderFactory)
            .from(u)
            .select(u.id.count())
            .where(u.identifier.eq(identifier))
            .fetchOne();
    return value == null ? 0L : value;
  }

  @Override
  public PageSlice<UserListProjection> page(UserQuery query) {
    var spec = queryBuilder.build(query);
    var filter = spec.filter();

    var blazeQuery =
        new BlazeJPAQuery<SysUser>(entityManager, criteriaBuilderFactory).from(u).select(u);

    applyKeyword(blazeQuery, query.getKeyword());
    applyUsername(blazeQuery, filter.username());
    applyStatus(blazeQuery, filter.userStatus());
    BlazeQueryDSLSupport.applySorts(blazeQuery, spec.sorts(), UserSortFieldMapper.INSTANCE);

    return queryDslSupport.executeWithEntityView(
        blazeQuery, UserListView.class, query.offset(), query.getPageSize(), this::toProjection);
  }

  private void applyKeyword(BlazeJPAQuery<SysUser> q, String keyword) {
    var like = BlazeQueryDSLSupport.likePattern(keyword);
    if (like == null) return;
    q.where(
        Expressions.anyOf(
            u.username.lower().like(like),
            u.nickname.lower().like(like),
            u.email.lower().like(like)));
  }

  private void applyUsername(BlazeJPAQuery<SysUser> q, String username) {
    var like = BlazeQueryDSLSupport.likePattern(username);
    if (like == null) return;
    q.where(u.username.lower().like(like));
  }

  private void applyStatus(BlazeJPAQuery<SysUser> q, UserStatus userStatus) {
    if (userStatus == null) return;
    q.where(u.userStatus.eq(userStatus));
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
