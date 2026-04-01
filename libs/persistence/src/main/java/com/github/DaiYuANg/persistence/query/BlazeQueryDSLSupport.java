package com.github.DaiYuANg.persistence.query;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedArrayList;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.querydsl.BlazeCriteriaBuilderRenderer;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.blazebit.persistence.querydsl.JPQLNextTemplates;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.toolkit4j.data.model.page.PageRequest;
import org.toolkit4j.data.model.page.PageResult;

/**
 * Support for executing BlazeJPAQuery (type-safe QueryDSL) with Blaze Entity View projections.
 * Renders QueryDSL query to CriteriaBuilder and applies Entity View for DTO projection.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class BlazeQueryDSLSupport {

  private final EntityManager entityManager;
  private final CriteriaBuilderFactory criteriaBuilderFactory;
  private final EntityViewManager entityViewManager;

  /**
   * Executes a BlazeJPAQuery with Entity View projection and pagination.
   *
   * @param query  BlazeJPAQuery with from/where/orderBy/select(root). Must select the root entity.
   * @param offset zero-based offset
   * @param limit  page size
   * @param mapper maps Entity View result to projection
   */
  @Contract("_, _, _, _, _ -> new")
  @SuppressWarnings("unchecked")
  public <E, V, P> @NonNull PagedList<P> executeWithEntityView(
    BlazeJPAQuery<E> query,
    Class<V> entityViewClass,
    int offset,
    int limit,
    Function<V, P> mapper) {
    val renderer =
      new BlazeCriteriaBuilderRenderer<>(
        criteriaBuilderFactory, entityManager, JPQLNextTemplates.DEFAULT);
    val criteriaBuilder = (CriteriaBuilder<E>) renderer.render(query);
    val setting = EntityViewSetting.create(entityViewClass, offset, limit);
    val results = entityViewManager.applySetting(setting, criteriaBuilder).getResultList();
    if (results instanceof PagedList<?> paged) {
      val items = paged.stream().map(v -> mapper.apply((V) v)).toList();
      return new PagedArrayList<>(
        items, paged.getKeysetPage(), paged.getTotalSize(), paged.getFirstResult(), paged.getMaxResults());
    }
    val items = results.stream().map(mapper).toList();
    return new PagedArrayList<>(items, null, items.size(), offset, limit);
  }

  @Contract("_, _ -> new")
  public static <T> @NonNull PageResult<T> toPageResult(PagedList<T> page, @NonNull PageRequest query) {
    return new PageResult<>(
      page,
      query.getPage(),
      query.getSize(),
      page.getTotalSize(),
      (long) page.getTotalPages());
  }
}
