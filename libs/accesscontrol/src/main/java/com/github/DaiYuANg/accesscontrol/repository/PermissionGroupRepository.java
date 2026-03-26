package com.github.DaiYuANg.accesscontrol.repository;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.github.DaiYuANg.accesscontrol.entity.QSysPermission;
import com.github.DaiYuANg.accesscontrol.entity.QSysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.entity.QSysPermissionGroupRefPermission;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroupRefPermission;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;
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
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;

/**
 * Repository for permission groups with RBAC-specific helpers.
 *
 * <p>Principle: prefer typed queries (BlazeJPAQuery/QueryDSL) and avoid initializing {@code
 * @ManyToMany} collections in bulk operations.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionGroupRepository extends BasePanacheCommandRepository<SysPermissionGroup>
    implements PermissionGroupQueryRepository {

  private static final QSysPermissionGroup g = new QSysPermissionGroup("permissionGroup");
  private static final QSysPermission p = new QSysPermission("permission");
  private static final QSysPermissionGroupRefPermission gp =
      new QSysPermissionGroupRefPermission("permissionGroupRefPermission");

  private final EntityManager entityManager;
  private final CriteriaBuilderFactory criteriaBuilderFactory;
  private final BlazeQueryDSLSupport queryDslSupport;
  private final MetamodelPermissionGroupQueryBuilder queryBuilder;
  private final JPAQueryFactory jpaQueryFactory;

  public Optional<SysPermissionGroup> findByCode(String code) {
    if (code == null || code.isBlank()) {
      return Optional.empty();
    }
    var rows =
        new BlazeJPAQuery<SysPermissionGroup>(entityManager, criteriaBuilderFactory)
            .from(g)
            .select(g)
            .where(g.code.eq(code))
            .limit(1)
            .fetch();
    return rows.stream().findFirst();
  }

  public Optional<SysPermissionGroup> findByName(String name) {
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }
    var rows =
        new BlazeJPAQuery<SysPermissionGroup>(entityManager, criteriaBuilderFactory)
            .from(g)
            .select(g)
            .where(g.name.eq(name))
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
            .from(g)
            .select(g.id.count())
            .where(g.code.eq(code))
            .fetchOne();
    return value == null ? 0L : value;
  }

  public long countByName(String name) {
    if (name == null || name.isBlank()) {
      return 0L;
    }
    Long value =
        new BlazeJPAQuery<Long>(entityManager, criteriaBuilderFactory)
            .from(g)
            .select(g.id.count())
            .where(g.name.eq(name))
            .fetchOne();
    return value == null ? 0L : value;
  }

  public List<SysPermissionGroup> findAllByIds(Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    var normalized = ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
    if (normalized.isEmpty()) {
      return List.of();
    }
    return new BlazeJPAQuery<SysPermissionGroup>(entityManager, criteriaBuilderFactory)
        .from(g)
        .select(g)
        .where(g.id.in(normalized))
        .fetch();
  }

  /** Returns permission ids for a group (from join table, no SysPermission load). */
  public List<Long> findPermissionIdsByGroupId(Long groupId) {
    if (groupId == null) {
      return List.of();
    }
    return new BlazeJPAQuery<Long>(entityManager, criteriaBuilderFactory)
        .from(g)
        .join(g.permissions, p)
        .select(p.id)
        .where(g.id.eq(groupId))
        .distinct()
        .fetch();
  }

  /** Returns permission ids for groups in one query. */
  public Map<Long, Set<Long>> findPermissionIdsByGroupIds(Collection<Long> groupIds) {
    if (groupIds == null || groupIds.isEmpty()) {
      return Map.of();
    }
    var normalized = groupIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
    if (normalized.isEmpty()) {
      return Map.of();
    }
    var rows =
        new BlazeJPAQuery<com.querydsl.core.Tuple>(entityManager, criteriaBuilderFactory)
            .from(g)
            .join(g.permissions, p)
            .select(g.id, p.id)
            .where(g.id.in(normalized))
            .distinct()
            .fetch();
    var result = new LinkedHashMap<Long, Set<Long>>();
    for (var row : rows) {
      if (row == null) {
        continue;
      }
      var gid = row.get(g.id);
      var pid = row.get(p.id);
      if (gid == null || pid == null) {
        continue;
      }
      result.computeIfAbsent(gid, __ -> new LinkedHashSet<>()).add(pid);
    }
    return result;
  }

  @Transactional
  public int deletePermissionRefsByPermissionIds(List<Long> permissionIds, Long excludeGroupId) {
    if (permissionIds == null || permissionIds.isEmpty()) {
      return 0;
    }
    var normalized = permissionIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
    if (normalized.isEmpty()) {
      return 0;
    }
    var clause =
        jpaQueryFactory
            .delete(gp)
            .where(gp.id.permissionId.in(normalized));
    if (excludeGroupId != null) {
      clause.where(gp.id.permissionGroupId.ne(excludeGroupId));
    }
    return (int) clause.execute();
  }

  /**
   * Replaces a group's permission refs in the join table without initializing the JPA collection.
   *
   * <p>Implemented as typed bulk delete + batched inserts to avoid N+1 and avoid loading the
   * existing {@code g.permissions} collection.
   */
  @Transactional
  public void replacePermissionRefs(Long groupId, List<Long> permissionIds) {
    if (groupId == null) {
      return;
    }
    jpaQueryFactory
        .delete(gp)
        .where(gp.id.permissionGroupId.eq(groupId))
        .execute();

    if (permissionIds == null || permissionIds.isEmpty()) {
      return;
    }
    var normalized = permissionIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
    if (normalized.isEmpty()) {
      return;
    }
    // Insert rows via JPA; relies on JDBC batch settings when configured.
    for (Long pid : normalized) {
      entityManager.persist(new SysPermissionGroupRefPermission(groupId, pid));
    }
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
    BlazeQueryDSLSupport.applySorts(
        blazeQuery, spec.sorts(), PermissionGroupSortFieldMapper.INSTANCE);

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
        view.getId(), view.getName(), view.getDescription(), view.getCode(), view.getSort());
  }

  @Override
  protected ResultCode notFoundCode() {
    return ResultCode.DATA_NOT_FOUND;
  }
}
