package com.github.DaiYuANg.modules.example.infrastructure.persistence;

import com.github.DaiYuANg.modules.example.application.dto.ExampleOrderLineView;
import com.github.DaiYuANg.modules.example.application.dto.ExampleOrderView;
import com.github.DaiYuANg.modules.example.application.port.ExampleOrderStore;
import com.github.DaiYuANg.modules.example.domain.model.order.ExampleOrderStatus;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class PanacheExampleOrderStore implements ExampleOrderStore {

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
        ExampleOrderEntity.<ExampleOrderEntity>find(
                "buyerUsername", Sort.descending("createAt"), buyerUsername)
            .list();
    return orders.stream().map(this::toViewWithLines).toList();
  }

  private ExampleOrderView toView(ExampleOrderEntity o) {
    return toViewWithLines(o);
  }

  private ExampleOrderView toViewWithLines(ExampleOrderEntity o) {
    o.lines.size();
    var lineViews =
        o.lines.stream()
            .map(l -> new ExampleOrderLineView(l.productId, l.quantity, l.unitPriceMinor))
            .toList();
    return new ExampleOrderView(
        o.id, o.buyerUsername, o.status.name(), o.totalMinor, lineViews);
  }
}
