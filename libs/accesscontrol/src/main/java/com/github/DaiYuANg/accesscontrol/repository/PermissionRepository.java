package com.github.DaiYuANg.accesscontrol.repository;

import com.github.DaiYuANg.accesscontrol.entity.QSysPermission;
import com.github.DaiYuANg.accesscontrol.entity.SysPermission;
import com.github.DaiYuANg.accesscontrol.projection.PermissionListProjection;
import com.github.DaiYuANg.accesscontrol.query.PermissionPageQuery;
import com.github.DaiYuANg.accesscontrol.query.PermissionQueryRepository;
import com.github.DaiYuANg.accesscontrol.view.PermissionListView;
import com.github.DaiYuANg.common.constant.ResultCode;
import com.github.DaiYuANg.persistence.query.BlazeJPAQueryFactory;
import com.github.DaiYuANg.persistence.query.BlazeQueryDSLSupport;
import com.github.DaiYuANg.persistence.repository.BasePanacheCommandRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.toolkit4j.data.model.page.PageResult;

/**
 * Repository for permissions.
 *
 * <p>Read queries are expressed using BlazeJPAQuery/QueryDSL (typed) to avoid string-based query
 * fragments and reduce refactor risk.
 *
 * @author ddddd <dai_yuang@icloud.com>
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PermissionRepository extends BasePanacheCommandRepository<SysPermission>
    implements PermissionQueryRepository {

  private static final QSysPermission p = new QSysPermission("permission");

  private final BlazeJPAQueryFactory blazeQueryFactory;
  private final BlazeQueryDSLSupport queryDslSupport;

  public Optional<SysPermission> findByCode(String code) {
    if (code == null || code.isBlank()) {
      return Optional.empty();
    }
    val rows = blazeQueryFactory.selectFrom(p).where(p.code.eq(code)).limit(1).fetch();
    return rows.stream().findFirst();
  }

  public Optional<SysPermission> findByName(String name) {
    if (name == null || name.isBlank()) {
      return Optional.empty();
    }
    val rows = blazeQueryFactory.selectFrom(p).where(p.name.eq(name)).limit(1).fetch();
    return rows.stream().findFirst();
  }

  public long countByCode(String code) {
    if (code == null || code.isBlank()) {
      return 0L;
    }
    Long value =
        blazeQueryFactory
            .<Long>create()
            .from(p)
            .select(p.id.count())
            .where(p.code.eq(code))
            .fetchOne();
    return value == null ? 0L : value;
  }

  @Override
  public PageResult<PermissionListProjection> page(PermissionPageQuery query) {
    val blazeQuery = blazeQueryFactory.selectFrom(p);
    query.buildCondition(p).ifPresent(blazeQuery::where);
    query.buildOrders(p).forEach(blazeQuery::orderBy);
    val page =
        queryDslSupport.executeWithEntityView(
            blazeQuery,
            PermissionListView.class,
            query.offset(),
            query.getPageSize(),
            this::toProjection);
    return BlazeQueryDSLSupport.toPageResult(page, query);
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
