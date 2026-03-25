package com.github.DaiYuANg.accesscontrol.repository;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.github.DaiYuANg.accesscontrol.entity.QSysPermission;
import com.github.DaiYuANg.accesscontrol.entity.SysPermission;
import com.github.DaiYuANg.accesscontrol.parameter.PermissionQuery;
import com.github.DaiYuANg.accesscontrol.projection.PermissionListProjection;
import com.github.DaiYuANg.accesscontrol.query.MetamodelPermissionQueryBuilder;
import com.github.DaiYuANg.accesscontrol.query.PermissionQueryRepository;
import com.github.DaiYuANg.accesscontrol.query.sort.PermissionSortFieldMapper;
import com.github.DaiYuANg.accesscontrol.view.PermissionListView;
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
public class PermissionRepository extends BasePanacheCommandRepository<SysPermission>
    implements PermissionQueryRepository {

  private static final QSysPermission p = new QSysPermission("permission");

  private final EntityManager entityManager;
  private final CriteriaBuilderFactory criteriaBuilderFactory;
  private final BlazeQueryDSLSupport queryDslSupport;
  private final MetamodelPermissionQueryBuilder queryBuilder;

  public Optional<SysPermission> findByCode(String code) {
    if (code == null || code.isBlank()) {
      return Optional.empty();
    }
    var rows =
        new BlazeJPAQuery<SysPermission>(entityManager, criteriaBuilderFactory)
            .from(p)
            .select(p)
            .where(p.code.eq(code))
            .limit(1)
            .fetch();
    return rows.stream().findFirst();
  }

  public Optional<SysPermission> findByName(String name) {
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }
    var rows =
        new BlazeJPAQuery<SysPermission>(entityManager, criteriaBuilderFactory)
            .from(p)
            .select(p)
            .where(p.name.eq(name))
            .limit(1)
            .fetch();
    return rows.stream().findFirst();
  }

  public long countByCode(String code) {
    if (code == null || code.isBlank()) {
      return 0L;
    }
    Long value =
        new BlazeJPAQuery<Long>(entityManager, criteriaBuilderFactory)
            .from(p)
            .select(p.id.count())
            .where(p.code.eq(code))
            .fetchOne();
    return value == null ? 0L : value;
  }

  @Override
  public PageSlice<PermissionListProjection> page(PermissionQuery query) {
    var spec = queryBuilder.build(query);
    var filter = spec.filter();

    var blazeQuery =
        new BlazeJPAQuery<SysPermission>(entityManager, criteriaBuilderFactory).from(p).select(p);

    applyKeyword(blazeQuery, query.getKeyword());
    applyLike(blazeQuery, p.name, filter.name());
    applyLike(blazeQuery, p.code, filter.code());
    applyEquals(blazeQuery, p.resource, filter.resource());
    applyLike(blazeQuery, p.action, filter.action());
    applyEquals(blazeQuery, p.groupCode, filter.groupCode());
    BlazeQueryDSLSupport.applySorts(blazeQuery, spec.sorts(), PermissionSortFieldMapper.INSTANCE);

    return queryDslSupport.executeWithEntityView(
        blazeQuery,
        PermissionListView.class,
        query.offset(),
        query.getPageSize(),
        this::toProjection);
  }

  private void applyKeyword(BlazeJPAQuery<SysPermission> q, String keyword) {
    var like = BlazeQueryDSLSupport.likePattern(keyword);
    if (like == null) return;
    q.where(
        Expressions.anyOf(
            p.name.lower().like(like),
            p.code.lower().like(like),
            p.resource.lower().like(like),
            p.action.lower().like(like),
            p.groupCode.lower().like(like),
            p.description.lower().like(like)));
  }

  private void applyLike(
      BlazeJPAQuery<SysPermission> q, com.querydsl.core.types.dsl.StringPath path, String value) {
    var like = BlazeQueryDSLSupport.likePattern(value);
    if (like == null) return;
    q.where(path.lower().like(like));
  }

  private void applyEquals(
      BlazeJPAQuery<SysPermission> q, com.querydsl.core.types.dsl.StringPath path, String value) {
    if (value == null || value.isBlank()) return;
    q.where(path.eq(value));
  }

  private PermissionListProjection toProjection(PermissionListView view) {
    return new PermissionListProjection(
        view.getId(),
        view.getName(),
        view.getCode(),
        view.getResource(),
        view.getAction(),
        view.getGroupCode(),
        view.getDescription(),
        view.getExpression());
  }

  @Override
  protected ResultCode notFoundCode() {
    return ResultCode.DATA_NOT_FOUND;
  }
}
