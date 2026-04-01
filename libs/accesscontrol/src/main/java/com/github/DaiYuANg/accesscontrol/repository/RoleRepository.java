package com.github.DaiYuANg.accesscontrol.repository;

import com.github.DaiYuANg.accesscontrol.entity.QSysPermissionGroup;
import com.github.DaiYuANg.accesscontrol.entity.QSysRole;
import com.github.DaiYuANg.accesscontrol.entity.SysRole;
import com.github.DaiYuANg.accesscontrol.mapper.RoleListViewMapper;
import com.github.DaiYuANg.accesscontrol.projection.RoleListProjection;
import com.github.DaiYuANg.accesscontrol.query.RolePageQuery;
import com.github.DaiYuANg.accesscontrol.query.RoleQueryRepository;
import com.github.DaiYuANg.accesscontrol.view.RoleListView;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.persistence.query.BlazeJPAQueryFactory;
import com.github.DaiYuANg.persistence.query.BlazeQueryDSLSupport;
import com.github.DaiYuANg.persistence.repository.BasePanacheCommandRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.toolkit4j.data.model.page.PageResult;

/**
 * Repository for roles.
 *
 * <p>Uses BlazeJPAQuery + QueryDSL for typed filtering/sorting and to support fetch-join use cases
 * without string JPQL.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RoleRepository extends BasePanacheCommandRepository<SysRole>
    implements RoleQueryRepository {

  private static final QSysRole r = new QSysRole("role");
  private static final QSysPermissionGroup g = new QSysPermissionGroup("permissionGroup");

  private final BlazeJPAQueryFactory blazeQueryFactory;
  private final BlazeQueryDSLSupport queryDslSupport;
  private final RoleListViewMapper mapper;

  public Optional<SysRole> findByName(String name) {
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }
    val rows = blazeQueryFactory.selectFrom(r).where(r.name.eq(name)).limit(1).fetch();
    return rows.stream().findFirst();
  }

  public Optional<SysRole> findByCode(String code) {
    if (code == null || code.isBlank()) {
      return Optional.empty();
    }
    val rows = blazeQueryFactory.selectFrom(r).where(r.code.eq(code)).limit(1).fetch();
    return rows.stream().findFirst();
  }

  public long countByName(String name) {
    if (name == null || name.isBlank()) {
      return 0L;
    }
    Long value =
        blazeQueryFactory
            .<Long>create()
            .from(r)
            .select(r.id.count())
            .where(r.name.eq(name))
            .fetchOne();
    return value == null ? 0L : value;
  }

  public long countByCode(String code) {
    if (code == null || code.isBlank()) {
      return 0L;
    }
    Long value =
        blazeQueryFactory
            .<Long>create()
            .from(r)
            .select(r.id.count())
            .where(r.code.eq(code))
            .fetchOne();
    return value == null ? 0L : value;
  }

  public List<SysRole> listAllWithPermissionGroups() {
    return blazeQueryFactory
        .selectFrom(r)
        .leftJoin(r.permissionGroups, g)
        .fetchJoin()
        .distinct()
        .fetch();
  }

  public List<SysRole> findAllByIds(Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    val normalized = ids.stream().filter(Objects::nonNull).distinct().toList();
    if (normalized.isEmpty()) {
      return List.of();
    }
    return blazeQueryFactory.selectFrom(r).where(r.id.in(normalized)).fetch();
  }

  @Override
  public PageResult<RoleListProjection> page(@NonNull RolePageQuery query) {
    val blazeQuery = blazeQueryFactory.selectFrom(r);
    query.buildCondition(r).ifPresent(blazeQuery::where);
    query.buildOrders(r).forEach(blazeQuery::orderBy);
    val page =
        queryDslSupport.executeWithEntityView(
            blazeQuery, RoleListView.class, query.getOffset(), query.getSize(), mapper::toProjection);
    return BlazeQueryDSLSupport.toPageResult(page, query);
  }

  @Override
  protected ResultCode notFoundCode() {
    return ResultCode.DATA_NOT_FOUND;
  }
}
