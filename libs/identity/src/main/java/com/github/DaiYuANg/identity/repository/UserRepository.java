package com.github.DaiYuANg.identity.repository;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.identity.constant.UserStatus;
import com.github.DaiYuANg.identity.entity.QSysUser;
import com.github.DaiYuANg.identity.entity.SysUser;
import com.github.DaiYuANg.identity.entity.SysUser_;
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
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class UserRepository extends BasePanacheCommandRepository<SysUser>
    implements UserQueryRepository {

  private static final QSysUser u = new QSysUser("user");

  private final EntityManager entityManager;
  private final CriteriaBuilderFactory criteriaBuilderFactory;
  private final BlazeQueryDSLSupport queryDslSupport;
  private final MetamodelUserQueryBuilder queryBuilder;

  public Optional<SysUser> findByUsername(String username) {
    return find(SysUser_.username.getName(), username).firstResultOptional();
  }

  public long countByUsername(String username) {
    return count(SysUser_.username.getName(), username);
  }

  public long countByEmail(String email) {
    return count(SysUser_.email.getName(), email);
  }

  public long countByMobilePhone(String mobilePhone) {
    return count(SysUser_.mobilePhone.getName(), mobilePhone);
  }

  public long countByIdentifier(String identifier) {
    return count(SysUser_.identifier.getName(), identifier);
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
