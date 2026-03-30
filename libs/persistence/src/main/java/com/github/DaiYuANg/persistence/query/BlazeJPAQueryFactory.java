package com.github.DaiYuANg.persistence.query;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.querydsl.core.types.EntityPath;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class BlazeJPAQueryFactory {
  private final EntityManager entityManager;
  private final CriteriaBuilderFactory criteriaBuilderFactory;

  public <T> BlazeJPAQuery<T> create() {
    return new BlazeJPAQuery<>(entityManager, criteriaBuilderFactory);
  }

  public <T> BlazeJPAQuery<T> selectFrom(EntityPath<T> root) {
    return create(root).from(root).select(root);
  }

  public <T> BlazeJPAQuery<T> create(EntityPath<T> root) {
    return new BlazeJPAQuery<>(entityManager, criteriaBuilderFactory);
  }
}
