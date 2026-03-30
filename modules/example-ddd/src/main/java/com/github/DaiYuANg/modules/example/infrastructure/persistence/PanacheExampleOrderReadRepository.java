package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleOrderReadRepository;
import com.github.DaiYuANg.modules.example.application.readmodel.ExampleOrderView;
import com.github.DaiYuANg.modules.example.infrastructure.persistence.mapper.ExampleOrderReadMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Example order read adapter.
 *
 * <p>Uses typed QueryDSL/Blaze queries and fetch-joins for list operations to avoid N+1 when
 * rendering orders with line items.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PanacheExampleOrderReadRepository implements ExampleOrderReadRepository {

  private static final QExampleOrderEntity ORDER = new QExampleOrderEntity("order");
  private static final QExampleOrderLineEntity LINE = new QExampleOrderLineEntity("line");

  private final EntityManager entityManager;
  private final CriteriaBuilderFactory criteriaBuilderFactory;
  private final ExampleOrderReadMapper exampleOrderReadMapper;

  @Override
  public List<ExampleOrderView> listByBuyer(@NonNull String buyerUsername) {
    val orders =
        new BlazeJPAQuery<ExampleOrderEntity>(entityManager, criteriaBuilderFactory)
            .from(ORDER)
            .leftJoin(ORDER.lines, LINE)
            .fetchJoin()
            .select(ORDER)
            .where(ORDER.buyerUsername.eq(buyerUsername))
            .orderBy(ORDER.createAt.desc())
            .distinct()
            .fetch();
    return orders.stream().map(exampleOrderReadMapper::toView).toList();
  }
}
