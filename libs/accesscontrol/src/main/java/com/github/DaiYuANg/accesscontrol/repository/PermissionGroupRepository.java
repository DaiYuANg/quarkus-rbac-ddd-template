package com.github.DaiYuANg.accesscontrol.repository;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.github.DaiYuANg.accesscontrol.entity.QSysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup_;
import com.github.DaiYuANg.accesscontrol.parameter.PermissionGroupQuery;
import com.github.DaiYuANg.accesscontrol.projection.PermissionGroupListProjection;
import com.github.DaiYuANg.accesscontrol.query.MetamodelPermissionGroupQueryBuilder;
import com.github.DaiYuANg.accesscontrol.query.PermissionGroupQueryRepository;
import com.github.DaiYuANg.accesscontrol.query.sort.PermissionGroupSortFieldMapper;
import com.github.DaiYuANg.accesscontrol.view.PermissionGroupListView;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.persistence.query.BlazeQueryDSLSupport;
import com.github.DaiYuANg.persistence.query.PageSlice;
import com.github.DaiYuANg.persistence.repository.BasePanacheCommandRepository;
import com.querydsl.core.types.dsl.Expressions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionGroupRepository extends BasePanacheCommandRepository<SysPermissionGroup>
    implements PermissionGroupQueryRepository {

  private static final QSysPermissionGroup g = new QSysPermissionGroup("permissionGroup");

  private final EntityManager entityManager;
  private final CriteriaBuilderFactory criteriaBuilderFactory;
  private final BlazeQueryDSLSupport queryDslSupport;
  private final MetamodelPermissionGroupQueryBuilder queryBuilder;

  public Optional<SysPermissionGroup> findByCode(String code) {
    return find(SysPermissionGroup_.code.getName(), code).firstResultOptional();
  }

  public Optional<SysPermissionGroup> findByName(String name) {
    return find(SysPermissionGroup_.name.getName(), name).firstResultOptional();
  }

  public long countByCode(String code) {
    return count(SysPermissionGroup_.code.getName(), code);
  }

  public long countByName(String name) {
    return count(SysPermissionGroup_.name.getName(), name);
  }

  /** Returns permission ids for a group (from join table, no SysPermission load). */
  public List<Long> findPermissionIdsByGroupId(Long groupId) {
    return entityManager.createQuery(
        "SELECT p.id FROM SysPermissionGroup g JOIN g.permissions p WHERE g.id = :groupId",
        Long.class)
        .setParameter("groupId", groupId)
        .getResultList();
  }

  @Override
  public PageSlice<PermissionGroupListProjection> page(PermissionGroupQuery query) {
    var spec = queryBuilder.build(query);
    var filter = spec.filter();

    var blazeQuery =
        new BlazeJPAQuery<SysPermissionGroup>(entityManager, criteriaBuilderFactory)
            .from(g)
            .select(g);

    applyKeyword(blazeQuery, query.getKeyword());
    applyName(blazeQuery, filter.name());
    BlazeQueryDSLSupport.applySorts(blazeQuery, spec.sorts(), PermissionGroupSortFieldMapper.INSTANCE);

    return queryDslSupport.executeWithEntityView(
        blazeQuery,
        PermissionGroupListView.class,
        query.offset(),
        query.getPageSize(),
        this::toProjection);
  }

  private void applyKeyword(BlazeJPAQuery<SysPermissionGroup> q, String keyword) {
    var like = BlazeQueryDSLSupport.likePattern(keyword);
    if (like == null) return;
    q.where(Expressions.anyOf(g.name.lower().like(like), g.code.lower().like(like)));
  }

  private void applyName(BlazeJPAQuery<SysPermissionGroup> q, String name) {
    var like = BlazeQueryDSLSupport.likePattern(name);
    if (like == null) return;
    q.where(g.name.lower().like(like));
  }

  private PermissionGroupListProjection toProjection(PermissionGroupListView view) {
    return new PermissionGroupListProjection(
        view.getId(),
        view.getName(),
        view.getDescription(),
        view.getCode(),
        view.getSort());
  }

  @Override
  protected ResultCode notFoundCode() {
    return ResultCode.DATA_NOT_FOUND;
  }
}
