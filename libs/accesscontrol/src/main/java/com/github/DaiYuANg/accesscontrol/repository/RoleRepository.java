package com.github.DaiYuANg.accesscontrol.repository;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.github.DaiYuANg.accesscontrol.entity.QSysRole;
import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.entity.SysRole_;
import com.github.DaiYuANg.accesscontrol.parameter.RoleQuery;
import com.github.DaiYuANg.accesscontrol.projection.RoleListProjection;
import com.github.DaiYuANg.accesscontrol.query.MetamodelRoleQueryBuilder;
import com.github.DaiYuANg.accesscontrol.query.RoleQueryRepository;
import com.github.DaiYuANg.accesscontrol.query.sort.RoleSortFieldMapper;
import com.github.DaiYuANg.accesscontrol.view.RoleListView;
import com.github.DaiYuANg.common.constant.ResultCode;
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
public class RoleRepository extends BasePanacheCommandRepository<SysRole>
    implements RoleQueryRepository {

  private static final QSysRole r = new QSysRole("role");

  private final EntityManager entityManager;
  private final CriteriaBuilderFactory criteriaBuilderFactory;
  private final BlazeQueryDSLSupport queryDslSupport;
  private final MetamodelRoleQueryBuilder queryBuilder;

  public Optional<SysRole> findByName(String name) {
    return find(SysRole_.name.getName(), name).firstResultOptional();
  }

  public Optional<SysRole> findByCode(String code) {
    return find(SysRole_.code.getName(), code).firstResultOptional();
  }

  public long countByCode(String code) {
    return count(SysRole_.code.getName(), code);
  }

  @Override
  public PageSlice<RoleListProjection> page(RoleQuery query) {
    var spec = queryBuilder.build(query);
    var filter = spec.filter();

    var blazeQuery =
        new BlazeJPAQuery<SysRole>(entityManager, criteriaBuilderFactory).from(r).select(r);

    applyKeyword(blazeQuery, query.getKeyword());
    applyName(blazeQuery, filter.name());
    applyCode(blazeQuery, filter.exactCode());
    BlazeQueryDSLSupport.applySorts(blazeQuery, spec.sorts(), RoleSortFieldMapper.INSTANCE);

    return queryDslSupport.executeWithEntityView(
        blazeQuery, RoleListView.class, query.offset(), query.getPageSize(), this::toProjection);
  }

  private void applyKeyword(BlazeJPAQuery<SysRole> q, String keyword) {
    var like = BlazeQueryDSLSupport.likePattern(keyword);
    if (like == null) return;
    q.where(Expressions.anyOf(r.name.lower().like(like), r.code.lower().like(like)));
  }

  private void applyName(BlazeJPAQuery<SysRole> q, String name) {
    var like = BlazeQueryDSLSupport.likePattern(name);
    if (like == null) return;
    q.where(r.name.lower().like(like));
  }

  private void applyCode(BlazeJPAQuery<SysRole> q, String code) {
    if (code == null || code.isBlank()) return;
    q.where(r.code.eq(code));
  }

  private RoleListProjection toProjection(RoleListView view) {
    return new RoleListProjection(
        view.getId(),
        view.getName(),
        view.getCode(),
        view.getStatus() == null ? null : view.getStatus().name(),
        view.getSort());
  }

  @Override
  protected ResultCode notFoundCode() {
    return ResultCode.DATA_NOT_FOUND;
  }
}
