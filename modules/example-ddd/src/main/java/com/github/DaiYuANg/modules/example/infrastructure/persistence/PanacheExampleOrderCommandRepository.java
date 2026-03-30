package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.github.DaiYuANg.modules.example.application.port.driven.ExampleOrderCommandRepository;
import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrder;
import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrderLine;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.val;

@ApplicationScoped
public class PanacheExampleOrderCommandRepository implements ExampleOrderCommandRepository {

  @Override
  public ExampleOrder save(ExampleOrder order) {
    val entity = new ExampleOrderEntity();
    entity.buyerUsername = order.buyerUsername();
    entity.status = order.status();
    entity.totalMinor = order.totalMinor();
    order.lines().forEach(line -> entity.lines.add(toEntity(entity, line)));
    entity.persist();
    return order.persisted(entity.id);
  }

  private ExampleOrderLineEntity toEntity(ExampleOrderEntity order, ExampleOrderLine line) {
    val row = new ExampleOrderLineEntity();
    row.productId = line.productId();
    row.quantity = line.quantity();
    row.unitPriceMinor = line.unitPriceMinor();
    row.order = order;
    return row;
  }
}
