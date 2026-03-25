package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.querydsl.BlazeJPAQuery;
import com.github.DaiYuANg.modules.example.application.dto.ExampleOrderLineView;
import com.github.DaiYuANg.modules.example.application.dto.ExampleOrderView;
import com.github.DaiYuANg.modules.example.application.port.driven.ExampleOrderStore;
import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PanacheExampleOrderStore implements ExampleOrderStore {

  private static final QExampleOrderEntity o = new QExampleOrderEntity("order");
  private static final QExampleOrderLineEntity l = new QExampleOrderLineEntity("line");

  private final EntityManager entityManager;
  private final CriteriaBuilderFactory criteriaBuilderFactory;

  @Override
  public ExampleOrderView create(
      String buyerUsername, List<ExampleOrderLineView> lines, long totalMinor) {
    var order = new ExampleOrderEntity();
    order.buyerUsername = buyerUsername;
    order.status = ExampleOrderStatus.CREATED;
    order.totalMinor = totalMinor;
    for (var line : lines) {
      var row = new ExampleOrderLineEntity();
      row.productId = line.productId();
      row.quantity = line.quantity();
      row.unitPriceMinor = line.unitPriceMinor();
      row.order = order;
      order.lines.add(row);
    }
    order.persist();
    return toView(order);
  }

  @Override
  public List<ExampleOrderView> listByBuyer(String buyerUsername) {
    var orders =
        new BlazeJPAQuery<ExampleOrderEntity>(entityManager, criteriaBuilderFactory)
            .from(o)
            .leftJoin(o.lines, l).fetchJoin()
            .select(o)
            .where(o.buyerUsername.eq(buyerUsername))
            .orderBy(o.createAt.desc())
            .distinct()
            .fetch();
    return orders.stream().map(this::toViewWithLines).toList();
  }

  private ExampleOrderView toView(ExampleOrderEntity o) {
    return toViewWithLines(o);
  }

  private ExampleOrderView toViewWithLines(ExampleOrderEntity o) {
    var lineViews =
        o.lines.stream()
            .map(l -> new ExampleOrderLineView(l.productId, l.quantity, l.unitPriceMinor))
            .toList();
    return new ExampleOrderView(o.id, o.buyerUsername, o.status.name(), o.totalMinor, lineViews);
  }
}
