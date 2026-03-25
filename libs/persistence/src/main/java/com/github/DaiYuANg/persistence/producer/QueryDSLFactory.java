package com.github.DaiYuANg.persistence.producer;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class QueryDSLFactory {

  @Produces
  JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
    return new JPAQueryFactory(entityManager);
  }
}
