package com.github.DaiYuANg.persistence.query;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.querydsl.BlazeCriteriaBuilderRenderer;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.blazebit.persistence.querydsl.JPQLNextTemplates;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.val;

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
   * @param query BlazeJPAQuery with from/where/orderBy/select(root). Must select the root entity.
   * @param offset zero-based offset
   * @param limit page size
   * @param mapper maps Entity View result to projection
   */
  @SuppressWarnings("unchecked")
  public <E, V, P> PageSlice<P> executeWithEntityView(
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
      return new PageSlice<>(items, paged.getTotalSize());
    }
    val items = results.stream().map(v -> mapper.apply((V) v)).toList();
    return new PageSlice<>(items, items.size());
  }

  /**
   * Applies sorts to a BlazeJPAQuery using the given field mapper. Property names must match entity
   * attribute names (e.g. from metamodel .getName()).
   */
  public static <E> void applySorts(
      BlazeJPAQuery<E> query, List<QuerySort> sorts, SortFieldMapper fieldMapper) {
    if (sorts == null) return;
    sorts.stream()
        .map(
            sort -> {
              val expr = fieldMapper.get(sort.property());
              return expr == null
                  ? null
                  : switch (sort.direction()) {
                    case ASC -> expr.asc();
                    case DESC -> expr.desc();
                  };
            })
        .filter(java.util.Objects::nonNull)
        .forEach(query::orderBy);
  }

  /** Type-safe like for case-insensitive contains on string path. */
  public static String likePattern(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return '%' + value.toLowerCase() + '%';
  }
}
