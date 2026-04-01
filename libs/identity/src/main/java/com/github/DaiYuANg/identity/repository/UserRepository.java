package com.github.DaiYuANg.identity.repository;

import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.identity.entity.QSysUser;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.mapper.UserListViewMapper;
import com.github.DaiYuANg.identity.projection.UserListProjection;
import com.github.DaiYuANg.identity.query.UserPageQuery;
import com.github.DaiYuANg.identity.query.UserQueryRepository;
import com.github.DaiYuANg.identity.view.UserListView;
import com.github.DaiYuANg.persistence.query.BlazeJPAQueryFactory;
import com.github.DaiYuANg.persistence.query.BlazeQueryDSLSupport;
import com.github.DaiYuANg.persistence.repository.BasePanacheCommandRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

  private final BlazeQueryDSLSupport queryDslSupport;
  private final UserListViewMapper mapper;
  private final BlazeJPAQueryFactory blazeQueryFactory;
  private final UserLookupQuerySupport userLookupQuerySupport;
  private final UserRbacQuerySupport userRbacQuerySupport;

  public Optional<SysUser> findByUsername(String username) {
    return userLookupQuerySupport.findByUsername(username);
  }

  /**
   * Loads user with full RBAC graph (roles -> permissionGroups -> permissions) in one query.
   */
  public Optional<SysUser> findByUsernameWithRbacGraph(String username) {
    return userRbacQuerySupport.findByUsernameWithRbacGraph(username);
  }

  /**
   * Loads user with full RBAC graph (roles -> permissionGroups -> permissions) in one query.
   */
  public Optional<SysUser> findByIdWithRbacGraph(Long id) {
    return userRbacQuerySupport.findByIdWithRbacGraph(id);
  }

  /**
   * Loads all users with full RBAC graph (roles -> permissionGroups -> permissions) in one query.
   */
  public List<SysUser> listAllWithRbacGraph() {
    return userRbacQuerySupport.listAllWithRbacGraph();
  }

  /**
   * Returns role codes without loading RBAC entity graph.
   */
  public Set<String> findRoleCodesByUsername(String username) {
    return userRbacQuerySupport.findRoleCodesByUsername(username);
  }

  /**
   * Returns permission codes without loading RBAC entity graph.
   */
  public Set<String> findPermissionCodesByUsername(String username) {
    return userRbacQuerySupport.findPermissionCodesByUsername(username);
  }

  public long countUserLoginTotal() {
    return userLookupQuerySupport.countUserLoginTotal();
  }

  public long countByUsername(String username) {
    return userLookupQuerySupport.countByUsername(username);
  }

  public long countByEmail(String email) {
    return userLookupQuerySupport.countByEmail(email);
  }

  public long countByMobilePhone(String mobilePhone) {
    return userLookupQuerySupport.countByMobilePhone(mobilePhone);
  }

  public long countByIdentifier(String identifier) {
    return userLookupQuerySupport.countByIdentifier(identifier);
  }

  @Override
  public PageResult<UserListProjection> page(UserPageQuery query) {
    val blazeQuery = blazeQueryFactory.selectFrom(u);
    query.buildCondition(u).ifPresent(blazeQuery::where);
    query.buildOrders(u).forEach(blazeQuery::orderBy);
    val page =
      queryDslSupport.executeWithEntityView(
        blazeQuery, UserListView.class, query.getOffset(), query.getSize(), mapper::toProjection);
    return BlazeQueryDSLSupport.toPageResult(page, query);
  }

  @Override
  protected ResultCode notFoundCode() {
    return ResultCode.DATA_NOT_FOUND;
  }
}
