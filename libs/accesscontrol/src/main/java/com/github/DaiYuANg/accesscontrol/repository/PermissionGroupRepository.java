package com.github.DaiYuANg.accesscontrol.repository;

import com.github.DaiYuANg.accesscontrol.entity.QSysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.entity.SysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.projection.PermissionGroupListProjection;
import com.github.DaiYuANg.accesscontrol.query.PermissionGroupPageQuery;
import com.github.DaiYuANg.accesscontrol.query.PermissionGroupQueryRepository;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.persistence.query.BlazeJPAQueryFactory;
import com.github.DaiYuANg.persistence.query.PageResults;
import com.github.DaiYuANg.persistence.repository.BasePanacheCommandRepository;
import com.querydsl.core.types.Projections;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.toolkit4j.data.model.page.PageResult;

/**
 * Repository for permission groups with RBAC-specific helpers.
 *
 * <p>Principle: prefer typed queries (BlazeJPAQuery/QueryDSL) and avoid initializing
 * {@code @ManyToMany} collections in bulk operations.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionGroupRepository extends BasePanacheCommandRepository<SysPermissionGroup>
    implements PermissionGroupQueryRepository {

  private static final QSysPermissionGroup g = new QSysPermissionGroup("permissionGroup");

  private final BlazeJPAQueryFactory blazeQueryFactory;
  private final PermissionGroupPermissionQuerySupport permissionQuerySupport;
  private final PermissionGroupPermissionMutationSupport permissionMutationSupport;

  public Optional<SysPermissionGroup> findByCode(String code) {
    if (code == null || code.isBlank()) {
      return Optional.empty();
    }
    val rows = blazeQueryFactory.selectFrom(g).where(g.code.eq(code)).limit(1).fetch();
    return rows.stream().findFirst();
  }

  public Optional<SysPermissionGroup> findByName(String name) {
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }
    val rows = blazeQueryFactory.selectFrom(g).where(g.name.eq(name)).limit(1).fetch();
    return rows.stream().findFirst();
  }

  public long countByCode(String code) {
    if (code == null || code.isBlank()) {
      return 0L;
    }
    Long value =
        blazeQueryFactory
            .<Long>create()
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
        blazeQueryFactory
            .<Long>create()
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
    val normalized = ids.stream().filter(Objects::nonNull).distinct().toList();
    if (normalized.isEmpty()) {
      return List.of();
    }
    return blazeQueryFactory.selectFrom(g).where(g.id.in(normalized)).fetch();
  }

  /** Returns permission ids for a group (from join table, no SysPermission load). */
  public List<Long> findPermissionIdsByGroupId(Long groupId) {
    return permissionQuerySupport.findPermissionIdsByGroupId(groupId);
  }

  /** Returns permission ids for groups in one query. */
  public Map<Long, Set<Long>> findPermissionIdsByGroupIds(Collection<Long> groupIds) {
    return permissionQuerySupport.findPermissionIdsByGroupIds(groupIds);
  }

  public int deletePermissionRefsByPermissionIds(List<Long> permissionIds, Long excludeGroupId) {
    return permissionMutationSupport.deletePermissionRefsByPermissionIds(permissionIds, excludeGroupId);
  }

  /**
   * Replaces a group's permission refs in the join table without initializing the JPA collection.
   *
   * <p>Implemented as typed bulk delete + batched inserts to avoid N+1 and avoid loading the
   * existing {@code g.permissions} collection.
   */
  public void replacePermissionRefs(Long groupId, List<Long> permissionIds) {
    permissionMutationSupport.replacePermissionRefs(groupId, permissionIds);
  }

  @Override
  public PageResult<PermissionGroupListProjection> page(@NonNull PermissionGroupPageQuery query) {
    val blazeQuery =
        blazeQueryFactory
            .<PermissionGroupListProjection>create()
            .from(g)
            .select(
                Projections.constructor(
                    PermissionGroupListProjection.class,
                    g.id,
                    g.name,
                    g.description,
                    g.code,
                    g.sort));
    query.buildCondition(g).ifPresent(blazeQuery::where);
    query.buildOrders(g).forEach(blazeQuery::orderBy);
    val page = blazeQuery.fetchPage(query.offset(), query.getSize());
    return PageResults.from(page, query);
  }

  @Override
  protected ResultCode notFoundCode() {
    return ResultCode.DATA_NOT_FOUND;
  }
}
